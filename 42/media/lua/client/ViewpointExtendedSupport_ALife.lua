local Integration = ViewpointExtendedSupportALife
if type(Integration) ~= "table" then
    Integration = {}
    ViewpointExtendedSupportALife = Integration
end

local state = Integration.state
if type(state) ~= "table" then
    state = {}
    Integration.state = state
end

local function uidOf(shell)
    if not shell then return nil end
    local speech = ProjectALife and ProjectALife.Speech
    if speech and type(speech.variable) == "function" then
        local ok, uid = pcall(speech.variable, shell, "ALifeUID")
        if ok and uid ~= nil and uid ~= "" then return tostring(uid) end
    end
    if type(shell.getModData) == "function" then
        local ok, data = pcall(shell.getModData, shell)
        if ok and data and data.ProjectALifeUID ~= nil and data.ProjectALifeUID ~= "" then
            return tostring(data.ProjectALifeUID)
        end
    end
end

local function viewEnabled()
    local bridge = ViewpointExtendedSupport
    if not bridge or type(bridge.isViewEnabled) ~= "function" then return false end
    local ok, enabled = pcall(bridge.isViewEnabled)
    return ok and enabled == true
end

local function containsObject(objects, target)
    if type(objects) == "table" then
        for _, object in pairs(objects) do
            if object == target then return true end
        end
        return false
    end
    if objects and type(objects.size) == "function" and type(objects.get) == "function" then
        local ok, size = pcall(function() return objects:size() end)
        if not ok then return false end
        for index = 0, size - 1 do
            local itemOk, object = pcall(function() return objects:get(index) end)
            if itemOk and object == target then return true end
        end
    end
    return false
end

local function installGiftOption()
    local jimmy = ProjectALifeJimmy
    local gifts = jimmy and jimmy.GiftWindow
    if not gifts or type(gifts.fill) ~= "function" or gifts._viewpointExtendedSupportFill == gifts.fill
            or type(gifts.open) ~= "function" or type(gifts.isTrader) ~= "function"
            or not jimmy.Menu or type(jimmy.Menu.addPersonOption) ~= "function" then return end

    local original = gifts.fill
    local wrapper = function(playerNum, context, worldObjects)
        local target = state.activeTarget
        if not target then return original(playerNum, context, worldObjects) end
        if not containsObject(worldObjects, target) then return end
        if type(jimmy.enabled) ~= "function" then return end
        local enabledOk, enabled = pcall(jimmy.enabled, "hand_gifts")
        if not enabledOk or enabled ~= true then return end

        local playerOk, player = pcall(getSpecificPlayer, playerNum)
        if not playerOk or not player or type(target.getSquare) ~= "function" then return end
        local squareOk, square = pcall(target.getSquare, target)
        if not squareOk or not square then return end

        local actors = ProjectALife and ProjectALife.ActorRegistry
        local uid = uidOf(target)
        if not uid or not actors or type(actors.read) ~= "function" then return end
        local recordOk, record = pcall(actors.read, uid)
        if not recordOk or not record or record.lifecycle ~= "active" then return end

        local relations = ProjectALife and ProjectALife.Relations
        if relations and type(relations.playerStance) == "function" then
            local stanceOk, stance = pcall(relations.playerStance, record, player)
            if stanceOk and stance == "hostile" then return end
        end

        local name = "Someone"
        if type(jimmy.nameOf) == "function" then
            local nameOk, resolved = pcall(jimmy.nameOf, record, name)
            if nameOk and type(resolved) == "string" and resolved ~= "" then name = resolved end
        end
        local npc = { uid = uid, shell = target, record = record, name = name }
        local traderOk, trader = pcall(gifts.isTrader, record)
        local key = traderOk and trader
            and "ContextMenu_ALJ_Trade_OpenTrader" or "ContextMenu_ALJ_Trade_Open"
        local label = key
        if type(jimmy.t) == "function" then
            local labelOk, resolved = pcall(jimmy.t, key, name)
            if labelOk and type(resolved) == "string" then label = resolved end
        end
        return jimmy.Menu.addPersonOption(context, label, player,
            function(selectedPlayer) return gifts.open(selectedPlayer, npc) end)
    end

    gifts.fill = wrapper
    gifts._viewpointExtendedSupportFill = wrapper
end

local function filterTargetedTalkActions(result, target, player, uid)
    local interactions = ViewpointInteract
    local menu = ProjectALife and ProjectALife.TalkMenu
    if not menu or type(menu.open) ~= "function" or type(result) ~= "table"
            or type(result.labels) ~= "table" or not interactions
            or type(interactions.actions) ~= "table" then return result end

    local actions, labels, enabled = {}, {}, {}
    for index, action in ipairs(interactions.actions) do
        local args = action.args or {}
        local uidIndex
        if action.fn == menu.open then
            uidIndex = 2
        elseif args[2] == menu.open then
            uidIndex = 4
        end

        if not uidIndex or tostring(args[uidIndex]) == uid then
            local name = result.labels[index]
            if uidIndex and type(menu.recordFor) == "function" and type(menu.describe) == "function"
                    and type(menu.shownLabel) == "function" then
                local ok, record = pcall(menu.recordFor, uid)
                if ok and record then
                    local speech = ProjectALife.Speech
                    local playerKey
                    if speech and type(speech.playerKey) == "function" then
                        local keyOk, key = pcall(speech.playerKey, player)
                        if keyOk then playerKey = key end
                    end
                    local describeOk, details = pcall(function()
                        return { menu.describe(record, playerKey, 1) }
                    end)
                    if describeOk then
                        local label, plain = details[1], details[6]
                        local shownOk, shown = pcall(menu.shownLabel, label, plain)
                        if shownOk then name = shown end
                        local talkUi = ProjectALife.TalkUi
                        if talkUi and type(talkUi.visitStamp) == "function" then
                            local visitOk, stamp = pcall(talkUi.visitStamp, record)
                            if visitOk and stamp ~= nil then
                                local text = ProjectALife.Text
                                if text and type(text.ui) == "function" then
                                    local visitorOk, visitor = pcall(text.ui, "Visitor: {name}", { name = name })
                                    if visitorOk then name = visitor end
                                end
                            end
                        end
                        action.name = name
                    end
                end
            end

            actions[#actions + 1] = action
            labels[#labels + 1] = name
            local available
            if type(result.enabled) == "table" then available = result.enabled[index] end
            if available == nil then available = action.enabled end
            enabled[#enabled + 1] = available
        end
    end

    interactions.actions = actions
    result.labels = labels
    result.enabled = enabled
    return result
end

function Integration.harvest(originalHarvest, player, target)
    if type(originalHarvest) ~= "function" then return end
    local uid = uidOf(target)
    if not uid or not viewEnabled() then
        return originalHarvest(player, target)
    end

    installGiftOption()
    local previousTarget = state.activeTarget
    state.activeTarget = target
    local ok, result = pcall(originalHarvest, player, target)
    state.activeTarget = previousTarget
    if not ok then error(result, 0) end
    return filterTargetedTalkActions(result, target, player, uid)
end

function Integration.isConversationOpen()
    local menu = ProjectALife and ProjectALife.TalkMenu
    local window = menu and menu.instance
    if not viewEnabled() or not window or (window.playerNum ~= nil and window.playerNum ~= 0)
            or type(window.isVisible) ~= "function" then return false end
    local ok, visible = pcall(window.isVisible, window)
    return ok and visible == true
end

local function project(scope)
    if scope.resolved then return scope.valid end
    scope.resolved = true
    if not scope.layer or not scope.entry then return false end
    local entry = scope.entry
    local layout = scope.layout
    if entry.lx == nil or entry.ly == nil or entry.lz == nil
            or type(layout.settings.headRise) ~= "number" then return false end

    local ok, point = pcall(scope.bridge.projectCompanionDogTag, scope.layer.playerNum,
        entry.lx, entry.ly, entry.lz + layout.settings.headRise)
    if not ok or not point or type(point.size) ~= "function" or type(point.get) ~= "function" then
        return false
    end
    local pointOk, size, screenX, screenY = pcall(function()
        return point:size(), point:get(0), point:get(1)
    end)
    if not pointOk or size < 2 or type(screenX) ~= "number" or type(screenY) ~= "number"
            or screenX ~= screenX or screenY ~= screenY
            or math.abs(screenX) == math.huge or math.abs(screenY) == math.huge then return false end
    if type(scope.layer.left) ~= "number" or type(scope.layer.top) ~= "number" then return false end
    scope.x, scope.y = screenX - scope.layer.left, screenY - scope.layer.top
    scope.valid = true
    return true
end

local function installSpeechProjection()
    local alife = ProjectALife
    local bubbles = alife and alife.SpeechBubbles
    local layout = alife and alife.SpeechBubbleLayout
    local bridge = ViewpointExtendedSupport
    if type(bubbles) ~= "table" or type(layout) ~= "table"
            or type(layout.settings) ~= "table" or type(layout.settings.headRise) ~= "number"
            or type(bridge) ~= "table" or type(bridge.projectCompanionDogTag) ~= "function"
            or type(bridge.isViewEnabled) ~= "function" then return end

    if type(layout.onScreen) == "function" and layout._viewpointExtendedSupportOnScreen ~= layout.onScreen then
        local original = layout.onScreen
        local wrapper = function(view, x, y, pad)
            local scope = state.activeBubbleScope
            if scope and view == scope.layer.view then
                if not project(scope) then return false end
                x, y = scope.x, scope.y
            end
            return original(view, x, y, pad)
        end
        layout.onScreen = wrapper
        layout._viewpointExtendedSupportOnScreen = wrapper
    end

    if type(layout.placeHead) == "function" and layout._viewpointExtendedSupportPlaceHead ~= layout.placeHead then
        local original = layout.placeHead
        local wrapper = function(entry, view, x, y)
            local scope = state.activeBubbleScope
            if scope and entry == scope.entry then
                if not project(scope) then entry.mode = layout.HIDDEN; return end
                x, y = scope.x, scope.y
            end
            return original(entry, view, x, y)
        end
        layout.placeHead = wrapper
        layout._viewpointExtendedSupportPlaceHead = wrapper
    end

    if type(layout.pin) == "function" and layout._viewpointExtendedSupportPin ~= layout.pin then
        local original = layout.pin
        local wrapper = function(entry, view, x, y)
            local scope = state.activeBubbleScope
            if scope and entry == scope.entry then
                if not project(scope) then entry.mode = layout.HIDDEN; return end
                x, y = scope.x, scope.y
            end
            return original(entry, view, x, y)
        end
        layout.pin = wrapper
        layout._viewpointExtendedSupportPin = wrapper
    end

    if type(bubbles.locate) == "function" and bubbles._viewpointExtendedSupportLocate ~= bubbles.locate then
        local original = bubbles.locate
        local wrapper = function(layer, entry, now, player)
            if not viewEnabled() then return original(layer, entry, now, player) end
            local previous = state.activeBubbleScope
            state.activeBubbleScope = { layer = layer, entry = entry, bridge = bridge, layout = layout }
            local ok, result = pcall(original, layer, entry, now, player)
            state.activeBubbleScope = previous
            if not ok then error(result, 0) end
            return result
        end
        bubbles.locate = wrapper
        bubbles._viewpointExtendedSupportLocate = wrapper
    end
end

local function installTalkPromptProjection()
    local alife = ProjectALife
    local prompt = alife and alife.TalkPrompt
    local bridge = ViewpointExtendedSupport
    if not prompt or type(prompt.place) ~= "function"
            or type(prompt.aim) ~= "function" or type(prompt.screenRect) ~= "function"
            or type(bridge) ~= "table" or type(bridge.projectCompanionDogTag) ~= "function"
            or prompt._viewpointExtendedSupportPlace == prompt.place then return end

    local original = prompt.place
    local wrapper = function(button, ...)
        local result = original(button, ...)
        local slot = button and button.slot
        if not viewEnabled() or not slot or slot.kind ~= "question" then return result end

        local x, y, z = slot.x, slot.y, slot.z
        local shell = slot.shell
        if shell and type(shell.getX) == "function" and type(shell.getY) == "function"
                and type(shell.getZ) == "function" then
            local ok, worldX, worldY, worldZ = pcall(function()
                return shell:getX(), shell:getY(), shell:getZ()
            end)
            if ok then x, y, z = worldX, worldY, worldZ end
        end
        if type(x) ~= "number" or type(y) ~= "number" then return result end

        local layout = alife.SpeechBubbleLayout
        local rise = layout and layout.settings and layout.settings.headRise or 0.66
        if type(rise) ~= "number" then rise = 0.66 end
        local projectedOk, point = pcall(bridge.projectCompanionDogTag, slot.num, x, y, (z or 0) + rise)
        if not projectedOk or not point or type(point.size) ~= "function" or type(point.get) ~= "function" then
            if type(button.setVisible) == "function" then pcall(button.setVisible, button, false) end
            return result
        end

        local pointOk, count, screenX, screenY = pcall(function()
            return point:size(), point:get(0), point:get(1)
        end)
        if not pointOk or type(count) ~= "number" or count < 2
                or type(screenX) ~= "number" or type(screenY) ~= "number" then
            if type(button.setVisible) == "function" then pcall(button.setVisible, button, false) end
            return result
        end

        local rectOk, left, top, width, height = pcall(prompt.screenRect, slot.num)
        if not rectOk or type(left) ~= "number" or type(top) ~= "number"
                or type(width) ~= "number" or type(height) ~= "number" then return result end
        local settings = prompt.settings
        if type(settings) ~= "table" or type(settings.margin) ~= "number"
                or type(settings.gap) ~= "number" then return result end

        local metrics = slot.m
        local margin, gap = settings.margin, settings.gap
        if metrics and prompt.plain ~= true then
            if type(metrics.below) == "number" then gap = metrics.below end
            if type(metrics.tail) == "number" then margin = math.max(margin, metrics.tail + 2) end
        end
        local bx = math.max(left + margin, math.min(screenX - button.width / 2,
            math.max(left + margin, left + width - button.width - margin)))
        local by = math.max(top + margin, math.min(screenY - button.height - gap,
            math.max(top + margin, top + height - button.height - margin)))
        bx, by = math.floor(bx), math.floor(by)
        local positionOk = pcall(function()
            button:setX(bx)
            button:setY(by)
            prompt.aim(button, screenX, screenY, bx, by)
        end)
        if not positionOk and type(button.setVisible) == "function" then
            pcall(button.setVisible, button, false)
        end
        return result
    end

    prompt.place = wrapper
    prompt._viewpointExtendedSupportPlace = wrapper
end

function Integration.install()
    installGiftOption()
    installSpeechProjection()
    installTalkPromptProjection()
end

Integration.install()
if Events and Events.OnGameStart and Events.OnTick and not state.eventsRegistered then
    state.eventsRegistered = true
    Events.OnGameStart.Add(Integration.install)
    Events.OnTick.Add(function()
        state.installTicks = (state.installTicks or 0) + 1
        if state.installTicks % 60 == 0 then Integration.install() end
    end)
end
