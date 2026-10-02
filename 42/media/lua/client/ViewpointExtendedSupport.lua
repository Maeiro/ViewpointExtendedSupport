require "PZAPI/ModOptions"

local Support = ViewpointExtendedSupport

local function keyOrFallback(name, fallback)
    if Keyboard and Keyboard[name] then return Keyboard[name] end
    return fallback
end

local options = {}
if PZAPI and PZAPI.ModOptions then
    local modOptions = PZAPI.ModOptions:create("ViewpointExtendedSupport", "Viewpoint Extended Support")
    options.viewModeKey = modOptions:addKeyBind("ViewModeToggleKey", "Toggle first/third-person mode", keyOrFallback("KEY_NONE", 0))
    options.holdCursor = modOptions:addTickBox("HoldFreeCursor", "Hold a key to use free mouse", false)
    options.cursorKey = modOptions:addKeyBind("FreeCursorKey", "Free mouse hold key", keyOrFallback("KEY_LALT", 56))
    options.autoUi = modOptions:addTickBox("AutoFreeCursorInUI", "Automatically use free mouse in inventories and menus", true)
    options.ergonomicUi = modOptions:addTickBox("ErgonomicUIIntegration", "Integrate with Ergonomic UI", true)
    options.vehicleCamera = modOptions:addTickBox("ThirdPersonInVehicles", "Use third person in vehicles", true)
    options.thirdPersonScrollZoom = modOptions:addTickBox("ThirdPersonScrollZoom", "Zoom third-person camera with mouse wheel", true)
    options.thirdPersonZoomOutKey = modOptions:addKeyBind("ThirdPersonZoomOutKey", "Zoom third-person camera out", keyOrFallback("KEY_MINUS", 12))
    options.thirdPersonZoomInKey = modOptions:addKeyBind("ThirdPersonZoomInKey", "Zoom third-person camera in", keyOrFallback("KEY_EQUALS", 13))
    options.groupContextMenuActions = modOptions:addTickBox("GroupContextMenuActions", "Use cascading Viewpoint interaction menus", true)
    options.skipSetup = modOptions:addTickBox("SkipSetupWizard", "Skip Viewpoint startup setup screen", true)
    options.startViewpoint = modOptions:addTickBox("StartViewpointOnGameStart", "Start Viewpoint automatically when entering a game", true)
    options.cursorDiagnostics = modOptions:addTickBox("CursorDiagnostics", "Log cursor and reticle diagnostics", true)
end

local function optionValue(option, fallback)
    if not option or not option.getValue then return fallback end
    local ok, value = pcall(function() return option:getValue() end)
    if not ok or value == nil then return fallback end
    return value
end

local function syncConfiguration()
    if not Support or not Support.configure then return end
    Support.configure(
        24,
        24,
        true,
        optionValue(options.holdCursor, false),
        optionValue(options.cursorKey, 56),
        optionValue(options.autoUi, true),
        optionValue(options.viewModeKey, 0),
        optionValue(options.cursorDiagnostics, true),
        optionValue(options.thirdPersonScrollZoom, true),
        optionValue(options.skipSetup, true),
        optionValue(options.thirdPersonZoomOutKey, 12),
        optionValue(options.thirdPersonZoomInKey, 13),
        optionValue(options.groupContextMenuActions, true)
    )
end

local function isVisible(ui)
    if not ui or not ui.isVisible then return false end
    local ok, value = pcall(function() return ui:isVisible() end)
    return ok and value == true
end

local function hasClassName(ui, names)
    if not ui then return false end
    if instanceof then
        for _, name in ipairs(names) do
            local ok, value = pcall(function() return instanceof(ui, name) end)
            if ok and value then return true end
        end
    end
    local ok, javaClass = pcall(function() return ui:getClass() end)
    if ok and javaClass and javaClass.getSimpleName then
        local okName, simpleName = pcall(function() return javaClass:getSimpleName() end)
        if okName then
            for _, name in ipairs(names) do
                if simpleName == name then return true end
            end
        end
    end
    return false
end

local UI_CLASSES = {
    "ISInventoryPage",
    "ISCraftingUI",
    "ISHandcraftWindow",
    "ISCharacterInfoWindow",
    "ISHealthPanel",
    "ISVehicleMechanics",
    "ISLootWindow",
    "ISModalDialog",
    "ISModalRichText",
    "ISEntityUI",
    "ISTradingUI",
}

local function visibleInventory(getter)
    if type(getter) ~= "function" then return false end
    local ok, ui = pcall(getter, 0)
    return ok and isVisible(ui)
end

local function visibleGameWindow()
    if visibleInventory(getPlayerInventory) or visibleInventory(getPlayerLoot) then
        return true
    end

    if not UIManager or not UIManager.getUI then return false end
    local ok, uiList = pcall(function() return UIManager.getUI() end)
    if not ok or not uiList or not uiList.size then return false end
    local okSize, size = pcall(function() return uiList:size() end)
    if not okSize then return false end
    for i = 0, size - 1 do
        local okUi, ui = pcall(function() return uiList:get(i) end)
        if okUi and isVisible(ui) and hasClassName(ui, UI_CLASSES) then
            return true
        end
    end
    return false
end

local function installErgonomicUIIntegration()
    if not optionValue(options.ergonomicUi, true) then return end
    if not ErgUIPanel then return end

    if ErgUIPanel._viewpointSupportInstalled then return end
    ErgUIPanel._viewpointSupportInstalled = true

    if ErgUIPanel.togglePanelManual then
        local originalToggle = ErgUIPanel.togglePanelManual
        ErgUIPanel.togglePanelManual = function(self, ...)
            local result = originalToggle(self, ...)
            local visible = self:isVisible() and self:isBodyVisible()
            self._viewpointSupportCursorRequested = visible
            return result
        end
    end

    if ErgUIPanel.revealFromContainer then
        local originalReveal = ErgUIPanel.revealFromContainer
        ErgUIPanel.revealFromContainer = function(self, ...)
            local result = originalReveal(self, ...)
            self._viewpointSupportCursorRequested = true
            return result
        end
    end

    if ErgUIPanel.openSettings then
        local originalSettings = ErgUIPanel.openSettings
        ErgUIPanel.openSettings = function(self, ...)
            local result = originalSettings(self, ...)
            self._viewpointSupportCursorRequested = true
            return result
        end
    end
end

local function ergonomicUiRequestsCursor()
    if not optionValue(options.ergonomicUi, true) then return false end
    local panel = ErgonomicUI and ErgonomicUI.panel
    if not panel then return false end
    if not panel:isVisible() then
        panel._viewpointSupportCursorRequested = false
        return false
    end
    return panel._viewpointSupportCursorRequested == true
end

local function updateCursorRequest()
    if not Support or not Support.setAutoCursorRequested then return end
    if not optionValue(options.autoUi, true) then
        Support.setAutoCursorRequested(false)
        return
    end
    Support.setAutoCursorRequested(visibleGameWindow() or ergonomicUiRequestsCursor())
end

local vehicleCameraForced = false

local function onEnterVehicle(player)
    if player ~= getSpecificPlayer(0) then return end
    if not optionValue(options.vehicleCamera, true) then return end
    if not Support or not Support.isViewEnabled or not Support.setThirdPerson then return end
    if not Support.isViewEnabled() then return end
    vehicleCameraForced = true
    Support.setThirdPerson(true)
end

local function onExitVehicle(player)
    if player and player ~= getSpecificPlayer(0) then return end
    if vehicleCameraForced and Support and Support.setThirdPerson then
        Support.setThirdPerson(false)
    end
    vehicleCameraForced = false
end

Events.OnEnterVehicle.Add(onEnterVehicle)
Events.OnExitVehicle.Add(onExitVehicle)

local contextMenuIconsHooked = false
local contextMenuIconsInitialized = false
local activeContextMenuIconPackName
local activeContextMenuIconPack

local function registerContextMenuIcons(optionsTable, packName, pack)
    for localizationKey, details in pairs(optionsTable or {}) do
        local textureName = type(details) == "string" and details or nil
        if type(details) == "table" then
            textureName = details.iconTextureName
        end

        if textureName and type(localizationKey) == "string" then
            local folder = pack.settings and pack.settings.textureFolderName or packName
            local texturePath = "media/ui/cmi/" .. folder .. "/" .. textureName
            local cmi = ContextMenuIcons
            if cmi.IconHandler and cmi.IconHandler.getIconPath then
                local ok, path = pcall(cmi.IconHandler.getIconPath, packName, textureName)
                if ok and path then texturePath = path end
            end

            local ok, texture = pcall(getTexture, texturePath)
            if ok and texture then
                Support.setContextMenuIcon(getText(localizationKey), texture)
                Support.setContextMenuIcon(localizationKey, texture)
            end
        end

        if type(details) == "table" and details.subOptions then
            registerContextMenuIcons(details.subOptions, packName, pack)
        end
    end
end

local function refreshContextMenuIcons()
    if not Support or not Support.clearContextMenuIcons then return end
    Support.clearContextMenuIcons()

    local cmi = ContextMenuIcons
    local preferences = cmi and cmi.preferences
    local packName = preferences and preferences.iconPackName
    local pack = packName and cmi.iconPacks and cmi.iconPacks[packName]
    if pack and not cmi.isNoneIconPackSelected then
        local options = pack.options and pack.options.world
        registerContextMenuIcons(options, packName, pack)
    end

    if Support.refreshContextMenuIcons then
        Support.refreshContextMenuIcons()
    end
    activeContextMenuIconPackName = packName
    activeContextMenuIconPack = pack
    contextMenuIconsInitialized = true
end

local function installContextMenuIconsIntegration()
    local cmi = ContextMenuIcons
    if type(cmi) ~= "table" or not cmi.Events or not cmi.Events.OnPreferencesApplied then return end
    if not Support or not Support.clearContextMenuIcons then return end

    if not contextMenuIconsHooked then
        cmi.Events.OnPreferencesApplied(refreshContextMenuIcons)
        contextMenuIconsHooked = true
    end

    local preferences = cmi.preferences
    local packName = preferences and preferences.iconPackName
    local pack = packName and cmi.iconPacks and cmi.iconPacks[packName]
    if not contextMenuIconsInitialized
            or packName ~= activeContextMenuIconPackName
            or pack ~= activeContextMenuIconPack then
        refreshContextMenuIcons()
    end
end

local function captureVanillaMenuIcons(menu, captured, visited)
    if not menu or type(menu.options) ~= "table" or visited[menu] then return end
    visited[menu] = true

    local optionCount = tonumber(menu.numOptions) or (#menu.options + 1)
    for index = 1, math.min(optionCount - 1, #menu.options) do
        local option = menu.options[index]
        if option then
            local icon = option.iconTexture
            if not icon and option.itemForTexture then
                local ok, texture = pcall(function() return option.itemForTexture:getTex() end)
                if ok then icon = texture end
            end
            if icon and type(option.name) == "string" then
                table.insert(captured, { option = option, icon = icon })
            end
            if option.subOption and menu.getSubMenu then
                local ok, submenu = pcall(function() return menu:getSubMenu(option.subOption) end)
                if ok then captureVanillaMenuIcons(submenu, captured, visited) end
            end
        end
    end
end

local function optionMatchesAction(option, action)
    if not action or action.fn ~= option.onSelect or type(action.args) ~= "table" then return false end
    local args = { option.target, option.param1, option.param2, option.param3, option.param4,
        option.param5, option.param6, option.param7, option.param8, option.param9, option.param10 }
    for index = 1, 11 do
        if action.args[index] ~= args[index] then return false end
    end
    return true
end

local function publishVanillaMenuIcons(captured)
    if not Support or not Support.clearVanillaContextMenuIcons
            or not Support.setVanillaContextMenuIcon then return end

    Support.clearVanillaContextMenuIcons()
    for _, entry in ipairs(captured) do
        local option = entry.option
        if option.subOption then
            Support.setVanillaContextMenuIcon(option.name, entry.icon)
        end
    end

    local actions = ViewpointInteract and ViewpointInteract.actions or {}
    local matched = {}
    for _, action in ipairs(actions) do
        for _, entry in ipairs(captured) do
            local option = entry.option
            if not option.subOption and not matched[entry] and optionMatchesAction(option, action) then
                Support.setVanillaContextMenuIcon(action.name, entry.icon)
                matched[entry] = true
                break
            end
        end
    end
end

local function installViewpointVanillaMenuIconsIntegration()
    local interactions = ViewpointInteract
    if type(interactions) ~= "table" or type(interactions.harvest) ~= "function"
            or type(ISWorldObjectContextMenu) ~= "table"
            or type(ISWorldObjectContextMenu.createMenu) ~= "function" then return end

    if interactions._extendedSupportVanillaIconHarvest == interactions.harvest then return end
    local originalHarvest = interactions.harvest
    local worldMenu = ISWorldObjectContextMenu
    local wrapper = function(...)
        local captured = {}
        local originalCreateMenu = worldMenu.createMenu
        local interceptCreateMenu = function(...)
            local menu = originalCreateMenu(...)
            captureVanillaMenuIcons(menu, captured, {})
            return menu
        end

        worldMenu.createMenu = interceptCreateMenu
        local ok, result = pcall(originalHarvest, ...)
        if worldMenu.createMenu == interceptCreateMenu then
            worldMenu.createMenu = originalCreateMenu
        end

        if ok and result and type(result.labels) == "table" then
            publishVanillaMenuIcons(captured)
        elseif Support and Support.clearVanillaContextMenuIcons then
            Support.clearVanillaContextMenuIcons()
        end
        if not ok then error(result, 0) end
        return result
    end
    interactions.harvest = wrapper
    interactions._extendedSupportVanillaIconHarvest = wrapper

    if type(interactions.harvestVehicle) == "function"
            and interactions._extendedSupportVanillaIconVehicle ~= interactions.harvestVehicle then
        local originalHarvestVehicle = interactions.harvestVehicle
        local vehicleWrapper = function(...)
            if Support and Support.clearVanillaContextMenuIcons then
                Support.clearVanillaContextMenuIcons()
            end
            return originalHarvestVehicle(...)
        end
        interactions.harvestVehicle = vehicleWrapper
        interactions._extendedSupportVanillaIconVehicle = vehicleWrapper
    end
end

local tickCounter = 0
Events.OnTick.Add(function()
    tickCounter = tickCounter + 1
    if Support and Support.diagnosticTick then
        Support.diagnosticTick()
    end
    if Support and Support.pollThirdPersonZoom then
        Support.pollThirdPersonZoom()
    end
    if tickCounter % 10 == 0 then
        syncConfiguration()
        installErgonomicUIIntegration()
        installContextMenuIconsIntegration()
        installViewpointVanillaMenuIconsIntegration()
    end
    updateCursorRequest()
end)

Events.OnGameStart.Add(function()
    syncConfiguration()
    installErgonomicUIIntegration()
    installContextMenuIconsIntegration()
    installViewpointVanillaMenuIconsIntegration()
    if optionValue(options.startViewpoint, true) and Support and Support.enableViewpoint then
        Support.enableViewpoint()
    end
end)

syncConfiguration()
