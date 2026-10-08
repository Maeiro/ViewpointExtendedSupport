local function alifeRecord(zombie)
    local alife = ProjectALife
    if type(alife) ~= "table" then return nil end

    local ok, modData = pcall(function() return zombie:getModData() end)
    if not ok or type(modData) ~= "table" then return nil end

    local uid = modData.ProjectALifeUID
    if uid == nil or uid == "" then return nil end
    uid = tostring(uid)

    local executor = alife.Executor
    local records = executor and executor.mirror and executor.mirror.records
    local record = type(records) == "table" and records[uid] or nil
    if type(record) == "table" then return record end

    local registry = alife.ActorRegistry
    if type(registry) ~= "table" or type(registry.read) ~= "function" then return nil end
    local okRecord, registered = pcall(registry.read, uid)
    return okRecord and type(registered) == "table" and registered or nil
end

local function isFriendlyOrAlliedAPlayer(zombie, player)
    local alife = ProjectALife
    local relations = alife and alife.Relations
    if type(relations) ~= "table" or type(relations.playerStance) ~= "function" then return false end

    local record = alifeRecord(zombie)
    if not record then return false end

    if type(relations.hostileToPlayer) == "function" then
        local ok, hostile = pcall(relations.hostileToPlayer, record, player)
        if ok and hostile == true then return false end
    end

    local ok, stance = pcall(relations.playerStance, record, player)
    return ok and (stance == "friendly" or stance == "allied")
end

local function installCompanionDogsFilter()
    local companionDogs = CompanionDogs
    if type(companionDogs) ~= "table" or type(companionDogs.zsnap) ~= "function"
            or type(companionDogs.getOwnerPlayer) ~= "function" then
        return
    end

    if companionDogs._viewpointSupportALifeZsnap == companionDogs.zsnap then return end

    local originalZsnap = companionDogs.zsnap
    local wrapper = function(animal, ...)
        local zombies = originalZsnap(animal, ...)
        if type(zombies) ~= "table" or #zombies == 0 then return zombies end

        local okOwner, owner = pcall(companionDogs.getOwnerPlayer, animal)
        if not okOwner or not owner then return zombies end

        local filtered
        for index = 1, #zombies do
            local zombie = zombies[index]
            if isFriendlyOrAlliedAPlayer(zombie, owner) then
                if not filtered then
                    filtered = {}
                    for previous = 1, index - 1 do
                        filtered[#filtered + 1] = zombies[previous]
                    end
                end
            elseif filtered then
                filtered[#filtered + 1] = zombie
            end
        end

        return filtered or zombies
    end

    companionDogs.zsnap = wrapper
    companionDogs._viewpointSupportALifeZsnap = wrapper
    print("[Viewpoint Extended Support] Companion Dogs ignores Friendly/Allied A-Life NPCs as threats")
end

installCompanionDogsFilter()
if Events and Events.OnGameStart then
    Events.OnGameStart.Add(installCompanionDogsFilter)
end
