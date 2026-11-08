require "PZAPI/ModOptions"
local PlayerHudVisibility = require "ViewpointExtendedSupport/PlayerHudVisibility"

local Support = ViewpointExtendedSupport
local DEFAULT_PREVIOUS_INTERACTION_KEY = (Keyboard and Keyboard.KEY_LEFT) or 203
local DEFAULT_NEXT_INTERACTION_KEY = (Keyboard and Keyboard.KEY_RIGHT) or 205

local function mouseButtonOrFallback(button, fallback)
    if Mouse and Mouse.BTN_OFFSET then return Mouse.BTN_OFFSET + button end
    return fallback
end

local function addModifierOption(modOptions, id, name, selected)
    local option = modOptions:addComboBox(id, name)
    option:addItem("None", selected == 1)
    option:addItem("Shift", selected == 2)
    option:addItem("Ctrl", selected == 3)
    option:addItem("Alt", selected == 4)
    option:addItem("Shift + Ctrl", selected == 5)
    option:addItem("Shift + Alt", selected == 6)
    option:addItem("Ctrl + Alt", selected == 7)
    option:addItem("Shift + Ctrl + Alt", selected == 8)
    return option
end

local options = {}
if PZAPI and PZAPI.ModOptions then
    local modOptions = PZAPI.ModOptions:create("ViewpointExtendedSupport", "Viewpoint Extended Support")
    modOptions:addTitle("Cursor and Interface")
    options.holdCursor = modOptions:addTickBox("HoldFreeCursor", "Hold a key to use free mouse", true)
    options.cursorKey = modOptions:addKeyBind("FreeCursorKey", "Free mouse hold key", mouseButtonOrFallback(3, 1003))
    options.cursorModifier = addModifierOption(modOptions, "FreeCursorModifier", "Free mouse modifiers", 1)
    options.autoUi = modOptions:addTickBox("AutoFreeCursorInUI", "Automatically use free mouse in inventories and menus", true)
    options.ergonomicUi = modOptions:addTickBox("ErgonomicUIIntegration", "Integrate with Ergonomic UI", true)
    options.hideInteractionMenuWhenInventoryOpen = modOptions:addTickBox("HideInteractionMenuWhenInventoryOpen", "Hide Viewpoint menu and reserve scroll for inventory", true)

    options.hidePlayerHud = modOptions:addTickBox("HidePDAPlayerHudInViewpointInventory",
        "UI_ViewpointExtendedSupport_HidePlayerHud", true, "UI_ViewpointExtendedSupport_HidePlayerHudTooltip")

    modOptions:addTitle("Vehicle Camera")
    options.vehicleCameraMode = modOptions:addComboBox("VehicleCameraMode", "Vehicle camera mode")
    options.vehicleCameraMode:addItem("Off", false)
    options.vehicleCameraMode:addItem("Third person in vehicle, first person on exit", true)
    options.vehicleCameraMode:addItem("Keep previous view in vehicle", false)

    modOptions:addTitle("Interaction Menu")
    options.groupContextMenuActions = modOptions:addTickBox("GroupContextMenuActions", "Use cascading Viewpoint interaction menus", true)
    options.cascadeContainerContents = modOptions:addTickBox("CascadeContainerContents", "Show container contents in a submenu", true)
    options.lazyInteractionOptions = modOptions:addTickBox("LazyInteractionOptions", "Show View options before building interaction menus", true)
    options.disableTargetOutline = modOptions:addTickBox("DisableTargetOutline", "Disable Viewpoint target outline", false)
    options.interactionPreviousKey = modOptions:addKeyBind("InteractionPreviousKey", "Previous nearby interaction target", DEFAULT_PREVIOUS_INTERACTION_KEY)
    options.interactionNextKey = modOptions:addKeyBind("InteractionNextKey", "Next nearby interaction target", DEFAULT_NEXT_INTERACTION_KEY)
    options.interactionCycleScrollModifier = addModifierOption(modOptions, "InteractionCycleScrollModifier", "Cycle nearby targets with scroll modifier", 4)

    modOptions:addTitle("Startup")
    options.closeExtractionTrackerOnLoad = modOptions:addTickBox("CloseExtractionTrackerOnLoad", "Start Wolf Extraction Quest tracker closed", true)
    options.skipSetup = modOptions:addTickBox("SkipSetupWizard", "Skip Viewpoint startup setup screen", false)
    options.startViewpoint = modOptions:addTickBox("StartViewpointOnGameStart", "Start Viewpoint automatically when entering a game", false)

    modOptions:addTitle("Diagnostics")
    options.diagnostics = modOptions:addTickBox("CursorDiagnostics", "Enable diagnostic logging", false)
end

local function optionValue(option, fallback)
    if not option or not option.getValue then return fallback end
    local ok, value = pcall(function() return option:getValue() end)
    if not ok or value == nil then return fallback end
    return value
end

local VEHICLE_CAMERA_OFF = 1
local VEHICLE_CAMERA_FORCE_THIRD_PERSON = 2
local VEHICLE_CAMERA_KEEP_PREVIOUS_VIEW = 3

local function vehicleCameraMode()
    local mode = optionValue(options.vehicleCameraMode, VEHICLE_CAMERA_FORCE_THIRD_PERSON)
    if mode == VEHICLE_CAMERA_OFF or mode == VEHICLE_CAMERA_FORCE_THIRD_PERSON
            or mode == VEHICLE_CAMERA_KEEP_PREVIOUS_VIEW then return mode end
    return VEHICLE_CAMERA_FORCE_THIRD_PERSON
end

local function modifierMask(option, fallback)
    local masks = { 0, 1, 2, 4, 3, 5, 6, 7 }
    return masks[optionValue(option, fallback or 1)] or 0
end

local function syncConfiguration()
    if not Support or not Support.configure then return end
    Support.configure(
        optionValue(options.holdCursor, true),
        optionValue(options.cursorKey, 1003),
        optionValue(options.autoUi, true),
        optionValue(options.diagnostics, false),
        optionValue(options.skipSetup, false),
        optionValue(options.groupContextMenuActions, true),
        modifierMask(options.cursorModifier, 1)
    )
    if Support.configureInteractionCycling then
        Support.configureInteractionCycling(
            optionValue(options.interactionPreviousKey, DEFAULT_PREVIOUS_INTERACTION_KEY),
            optionValue(options.interactionNextKey, DEFAULT_NEXT_INTERACTION_KEY),
            modifierMask(options.interactionCycleScrollModifier, 4)
        )
    end
    if Support.configureContainerContentsCascade then
        Support.configureContainerContentsCascade(optionValue(options.cascadeContainerContents, true))
    end
    if Support.setTargetOutlineDisabled then
        Support.setTargetOutlineDisabled(optionValue(options.disableTargetOutline, false))
    end
end

local function isVisible(ui)
    if not ui or not ui.isVisible then return false end
    local ok, value = pcall(function() return ui:isVisible() end)
    return ok and value == true
end

local ON_FOOT_CURSOR_WINDOWS = {
    ISRadialMenu = true,
    ISWorldMap = true,
    ISMapWrapper = true,
    ISCraftingUI = true,
    ISHandcraftWindow = true,
    ISBuildWindow = true,
    ISCharacterInfoWindow = true,
}

local CURSOR_WINDOW_CLASS_NAMES = {
    "ISRadialMenu",
    "ISWorldMap",
    "ISMapWrapper",
    "ISCraftingUI",
    "ISHandcraftWindow",
    "ISBuildWindow",
    "ISCharacterInfoWindow",
}
local activeCursorWindows = {}
local cursorRequestAbsentTicks = 0
local cursorRequestLastAbsentTick = -1
local CURSOR_REQUEST_RELEASE_DELAY = 5

local function visibleInventory(getter)
    if type(getter) ~= "function" then return false end
    local ok, ui = pcall(getter, 0)
    if not ok or not isVisible(ui) then return false end

    local collapsed = ui.isCollapsed
    if type(collapsed) == "function" then
        local collapsedOk, value = pcall(collapsed, ui)
        if collapsedOk then collapsed = value end
    elseif collapsed == nil then
        collapsed = ui.collapsed
    end
    return collapsed ~= true
end

local function playerIsInVehicle()
    if type(getSpecificPlayer) ~= "function" then return false end
    local ok, player = pcall(getSpecificPlayer, 0)
    if not ok or not player or not player.getVehicle then return false end
    local okVehicle, vehicle = pcall(function() return player:getVehicle() end)
    return okVehicle and vehicle ~= nil
end

local function visibleErgonomicInventory()
    local panel = ErgonomicUI and ErgonomicUI.panel
    if not panel then return false end
    if not isVisible(panel) then
        panel._viewpointSupportInventoryOpen = false
        return false
    end
    local bodyVisible
    if type(panel.isBodyVisible) == "function" then
        local ok, value = pcall(function() return panel:isBodyVisible() end)
        if ok then bodyVisible = value == true end
    end
    if bodyVisible == false then
        panel._viewpointSupportInventoryOpen = false
        panel._viewpointSupportCursorRequested = false
        return false
    end
    return bodyVisible == true or panel._viewpointSupportInventoryOpen == true
end

local function updateInteractionMenuSuppression()
    if not Support then return end
    local inventoryOpen = visibleInventory(getPlayerInventory)
        or visibleInventory(getPlayerLoot)
        or visibleErgonomicInventory()
    local hideInteractionMenu = optionValue(options.hideInteractionMenuWhenInventoryOpen, true)
    if Support.setInventoryOpen then
        Support.setInventoryOpen(hideInteractionMenu and inventoryOpen)
    end
    if Support.setInteractionMenuSuppressed then
        Support.setInteractionMenuSuppressed(
            hideInteractionMenu and inventoryOpen
        )
    end
end

local function visibleGameWindow()
    if visibleInventory(getPlayerInventory) or visibleInventory(getPlayerLoot) then
        return true
    end

    for window, className in pairs(activeCursorWindows) do
        if ON_FOOT_CURSOR_WINDOWS[className] and isVisible(window) then
            return true
        end
    end

    local manager = UIManager
    if not manager or type(manager.getUI) ~= "function" then return false end
    local ok, windows = pcall(manager.getUI)
    if not ok or not windows or type(windows.size) ~= "function" or type(windows.get) ~= "function" then
        return false
    end
    local sizeOk, size = pcall(function() return windows:size() end)
    if not sizeOk or type(size) ~= "number" then return false end
    for index = 0, size - 1 do
        local windowOk, window = pcall(function() return windows:get(index) end)
        if windowOk and window then
            if type(window.getTable) == "function" then
                local tableOk, uiTable = pcall(function() return window:getTable() end)
                if tableOk and uiTable then window = uiTable end
            end
            if ON_FOOT_CURSOR_WINDOWS[window.Type] and isVisible(window) then return true end
        end
    end
    return false
end

local updateCursorRequest
local tickCounter = 0

local function installCursorWindowHook(className)
    local windowClass = _G[className]
    if type(windowClass) ~= "table" or windowClass._viewpointSupportCursorHooks then return end

    local originalAdd = windowClass.addToUIManager
    local originalRemove = windowClass.removeFromUIManager
    if type(originalAdd) ~= "function" or type(originalRemove) ~= "function" then return end

    windowClass.addToUIManager = function(self, ...)
        local result = originalAdd(self, ...)
        activeCursorWindows[self] = className
        if updateCursorRequest then updateCursorRequest() end
        return result
    end
    windowClass.removeFromUIManager = function(self, ...)
        local result = originalRemove(self, ...)
        activeCursorWindows[self] = nil
        if updateCursorRequest then updateCursorRequest() end
        return result
    end
    windowClass._viewpointSupportCursorHooks = true
end

local function installCursorWindowHooks()
    for _, className in ipairs(CURSOR_WINDOW_CLASS_NAMES) do
        installCursorWindowHook(className)
    end
end

local function installVanillaInventoryCursorIntegration()
    local inventoryClass = ISInventoryPage
    if type(inventoryClass) ~= "table" or type(inventoryClass.setVisible) ~= "function" then return end
    if inventoryClass._viewpointSupportSetVisible == inventoryClass.setVisible then return end

    local originalSetVisible = inventoryClass.setVisible
    local wrapper = function(self, visible, ...)
        local result = originalSetVisible(self, visible, ...)
        local isPlayerInventory = false
        if type(getPlayerInventory) == "function" then
            local ok, inventory = pcall(getPlayerInventory, 0)
            isPlayerInventory = ok and self == inventory
        end
        if not isPlayerInventory and type(getPlayerLoot) == "function" then
            local ok, loot = pcall(getPlayerLoot, 0)
            isPlayerInventory = ok and self == loot
        end
        if isPlayerInventory then
            updateInteractionMenuSuppression()
            updateCursorRequest()
        end
        return result
    end
    inventoryClass.setVisible = wrapper
    inventoryClass._viewpointSupportSetVisible = wrapper

    if type(inventoryClass.onToggleVisible) == "function"
        and inventoryClass._viewpointSupportToggleVisible ~= inventoryClass.onToggleVisible then
        local originalOnToggleVisible = inventoryClass.onToggleVisible
        local toggleWrapper = function(self, ...)
            local result = originalOnToggleVisible(self, ...)
            updateInteractionMenuSuppression()
            updateCursorRequest()
            return result
        end
        inventoryClass.onToggleVisible = toggleWrapper
        inventoryClass._viewpointSupportToggleVisible = toggleWrapper
    end
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
            self._viewpointSupportInventoryOpen = visible
            self._viewpointSupportCursorRequested = visible
            updateInteractionMenuSuppression()
            updateCursorRequest()
            return result
        end
    end

    if ErgUIPanel.revealFromContainer then
        local originalReveal = ErgUIPanel.revealFromContainer
        ErgUIPanel.revealFromContainer = function(self, ...)
            local result = originalReveal(self, ...)
            self._viewpointSupportInventoryOpen = true
            self._viewpointSupportCursorRequested = true
            updateInteractionMenuSuppression()
            updateCursorRequest()
            return result
        end
    end

    if ErgUIPanel.openSettings then
        local originalSettings = ErgUIPanel.openSettings
        ErgUIPanel.openSettings = function(self, ...)
            local result = originalSettings(self, ...)
            self._viewpointSupportCursorRequested = true
            updateCursorRequest()
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

updateCursorRequest = function()
    if not Support or not Support.setAutoCursorRequested then return end
    local hasOtherUi = visibleGameWindow() or ergonomicUiRequestsCursor()
    local requested = hasOtherUi and optionValue(options.autoUi, true)

    if requested then
        cursorRequestAbsentTicks = 0
        cursorRequestLastAbsentTick = -1
        Support.setAutoCursorRequested(true)
    elseif cursorRequestLastAbsentTick ~= tickCounter then
        cursorRequestAbsentTicks = cursorRequestAbsentTicks + 1
        cursorRequestLastAbsentTick = tickCounter
        if cursorRequestAbsentTicks >= CURSOR_REQUEST_RELEASE_DELAY then
            Support.setAutoCursorRequested(false)
        end
    elseif cursorRequestAbsentTicks >= CURSOR_REQUEST_RELEASE_DELAY then
        Support.setAutoCursorRequested(false)
    end
end

local vehicleCameraForced = false
local vehicleCameraModeOnEntry
local previousThirdPersonView
local lastOnFootThirdPersonView

local function rememberOnFootCamera()
    if playerIsInVehicle()
            or not Support or type(Support.isViewEnabled) ~= "function"
            or type(Support.isThirdPerson) ~= "function" then return end
    local enabledOk, enabled = pcall(Support.isViewEnabled)
    if not enabledOk or enabled ~= true then return end
    local viewOk, thirdPerson = pcall(Support.isThirdPerson)
    if viewOk and type(thirdPerson) == "boolean" then
        lastOnFootThirdPersonView = thirdPerson
    end
end

local function onEnterVehicle(player)
    if player ~= getSpecificPlayer(0) then return end
    local mode = vehicleCameraMode()
    if mode == VEHICLE_CAMERA_OFF then return end
    if not Support or not Support.isViewEnabled or not Support.setThirdPerson then return end
    if not Support.isViewEnabled() then return end
    if vehicleCameraForced then return end
    vehicleCameraModeOnEntry = mode
    previousThirdPersonView = nil
    if mode == VEHICLE_CAMERA_KEEP_PREVIOUS_VIEW then
        previousThirdPersonView = lastOnFootThirdPersonView
        if not playerIsInVehicle() or previousThirdPersonView == nil then
            local ok, thirdPerson = pcall(Support.isThirdPerson)
            if ok and type(thirdPerson) == "boolean" then
                previousThirdPersonView = thirdPerson
            end
        end
    end
    vehicleCameraForced = true
    if mode == VEHICLE_CAMERA_FORCE_THIRD_PERSON then
        Support.setThirdPerson(true)
    elseif previousThirdPersonView ~= nil then
        Support.setThirdPerson(previousThirdPersonView)
    end
end

local function onExitVehicle(player)
    if player and player ~= getSpecificPlayer(0) then return end
    if vehicleCameraForced and Support and Support.setThirdPerson then
        if vehicleCameraModeOnEntry == VEHICLE_CAMERA_FORCE_THIRD_PERSON then
            Support.setThirdPerson(false)
        elseif vehicleCameraModeOnEntry == VEHICLE_CAMERA_KEEP_PREVIOUS_VIEW
                and previousThirdPersonView ~= nil then
            Support.setThirdPerson(previousThirdPersonView)
        end
    end
    vehicleCameraForced = false
    vehicleCameraModeOnEntry = nil
    previousThirdPersonView = nil
end

local function maintainVehicleCamera()
    if not vehicleCameraForced then return end
    if vehicleCameraMode() == VEHICLE_CAMERA_OFF then
        onExitVehicle(nil)
        return
    end
    if not playerIsInVehicle()
            or not Support or type(Support.isViewEnabled) ~= "function"
            or type(Support.isThirdPerson) ~= "function"
            or type(Support.setThirdPerson) ~= "function" then return end
    local enabledOk, enabled = pcall(Support.isViewEnabled)
    if not enabledOk or enabled ~= true then return end

    local target
    if vehicleCameraModeOnEntry == VEHICLE_CAMERA_FORCE_THIRD_PERSON then
        target = true
    elseif vehicleCameraModeOnEntry == VEHICLE_CAMERA_KEEP_PREVIOUS_VIEW then
        target = previousThirdPersonView
    end
    if target == nil then return end

    local viewOk, thirdPerson = pcall(Support.isThirdPerson)
    if viewOk and thirdPerson ~= target then
        pcall(Support.setThirdPerson, target)
    end
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

local nilActionArgument = {}

local function actionIndexChild(node, key, identityKeys, create)
    local keyType = type(key)
    if keyType ~= "string" and keyType ~= "number" and keyType ~= "boolean" then
        local identity
        for index, candidate in ipairs(identityKeys) do
            if rawequal(candidate, key) then
                identity = index
                break
            end
        end
        if identity == nil then
            if not create then return nil end
            identityKeys[#identityKeys + 1] = key
            identity = #identityKeys
        end
        key = identity
        keyType = "identity:" .. keyType
    end

    local typedNode = node[keyType]
    if not typedNode then
        if not create then return nil end
        typedNode = {}
        node[keyType] = typedNode
    end

    local child = typedNode[key]
    if not child then
        if not create then return nil end
        child = {}
        typedNode[key] = child
    end
    return child
end

local function actionIndexNode(index, fn, args, create, identityKeys)
    if fn == nil or type(args) ~= "table" then return nil end
    local node = actionIndexChild(index, fn, identityKeys, create)
    if not node then return nil end

    for argument = 1, 11 do
        local key = args[argument]
        if type(key) == "number" and key ~= key then return nil end
        if key == nil then key = nilActionArgument end
        node = actionIndexChild(node, key, identityKeys, create)
        if not node then return nil end
    end
    return node
end

local function optionArguments(option)
    return { option.target, option.param1, option.param2, option.param3, option.param4,
        option.param5, option.param6, option.param7, option.param8, option.param9, option.param10 }
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
    local iconIndex = {}
    local identityKeys = {}
    for _, entry in ipairs(captured) do
        local option = entry.option
        if not option.subOption then
            local node = actionIndexNode(iconIndex, option.onSelect, optionArguments(option), true,
                identityKeys)
            if node then
                node.entries = node.entries or {}
                table.insert(node.entries, entry)
            end
        end
    end

    for _, action in ipairs(actions) do
        local node = actionIndexNode(iconIndex, action.fn, action.args, false, identityKeys)
        if node and node.entries then
            local nextEntry = node.nextEntry or 1
            local entry = node.entries[nextEntry]
            if entry then
                node.nextEntry = nextEntry + 1
                Support.setVanillaContextMenuIcon(action.name, entry.icon)
            end
        end
    end
end

local interactionMenuDiagnosticStates = {}
local vanillaMenuIconFailureLogged = false

local function diagnosticLog(message)
    if optionValue(options.diagnostics, false) then
        print("[Viewpoint Extended Support] " .. message)
    end
end

local function logInteractionMenuDiagnostic(key, message)
    if not optionValue(options.diagnostics, false) then return end
    if interactionMenuDiagnosticStates[key] == message then return end
    interactionMenuDiagnosticStates[key] = message
    diagnosticLog("interaction menu: " .. message)
end

local function installViewpointVanillaMenuIconsIntegration()
    local interactions = ViewpointInteract
    if type(interactions) ~= "table" then
        logInteractionMenuDiagnostic("harvest-hook", "waiting for ViewpointInteract")
        return
    end
    if type(interactions.harvest) ~= "function" then
        logInteractionMenuDiagnostic("harvest-hook", "ViewpointInteract.harvest is unavailable")
        return
    end
    if type(ISWorldObjectContextMenu) ~= "table"
            or type(ISWorldObjectContextMenu.createMenu) ~= "function" then
        logInteractionMenuDiagnostic("harvest-hook", "vanilla world context-menu builder is unavailable")
        return
    end

    if interactions._extendedSupportVanillaIconHarvest == interactions.harvest
            or interactions._extendedSupportLazyHarvest == interactions.harvest then return end
    local originalHarvest = interactions.harvest
    local worldMenu = ISWorldObjectContextMenu
    local wrapper = function(...)
        local captured = {}
        local originalCreateMenu = worldMenu.createMenu
        local menuBuilder = worldMenu._NB_old_createMenu
        if type(menuBuilder) ~= "function" then
            menuBuilder = originalCreateMenu
        end
        local interceptCreateMenu = function(...)
            local menu = menuBuilder(...)
            captureVanillaMenuIcons(menu, captured, {})
            return menu
        end

        worldMenu.createMenu = interceptCreateMenu
        local ok, result = pcall(originalHarvest, ...)
        if worldMenu.createMenu == interceptCreateMenu then
            worldMenu.createMenu = originalCreateMenu
        end

        if ok and result and type(result.labels) == "table" then
            local iconsOk, iconError = pcall(publishVanillaMenuIcons, captured)
            if not iconsOk then
                if Support and Support.clearVanillaContextMenuIcons then
                    pcall(Support.clearVanillaContextMenuIcons)
                end
                if not vanillaMenuIconFailureLogged then
                    diagnosticLog("vanilla interaction icon mapping failed: " .. tostring(iconError))
                    vanillaMenuIconFailureLogged = true
                end
            end
        elseif Support and Support.clearVanillaContextMenuIcons then
            pcall(Support.clearVanillaContextMenuIcons)
        end
        if not ok then error(result, 0) end
        return result
    end
    interactions.harvest = wrapper
    interactions._extendedSupportVanillaIconHarvest = wrapper
    logInteractionMenuDiagnostic("harvest-hook", "Viewpoint interaction harvest hook installed")

    if type(interactions.harvestVehicle) == "function"
            and interactions._extendedSupportVanillaIconVehicle ~= interactions.harvestVehicle
            and interactions._extendedSupportLazyVehicle ~= interactions.harvestVehicle then
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

local pendingInteractionTarget
local pendingInteractionTicks = 0
local lazyInteractionWarningLogged = false

local function interactionTargetHeading(target)
    if not target then return nil end

    local ok, isGround = pcall(function()
        if target:isFloor() then return true end
        local sprite = target:getSprite()
        local properties = sprite and sprite:getProperties()
        return properties and IsoFlagType and properties:has(IsoFlagType.solidfloor) or false
    end)
    local title
    if ok and isGround then
        title = "Interact with Ground"
    else
        local okName, name = pcall(function() return target:getName() end)
        if okName and type(name) == "string" and name ~= "" then
            title = "Interact with " .. name
        else
            local okObjectName, objectName = pcall(function() return target:getObjectName() end)
            if okObjectName and type(objectName) == "string" and objectName ~= ""
                    and objectName ~= "IsoObject" then
                objectName = objectName:gsub("^Iso", ""):gsub("(%l)(%u)", "%1 %2")
                title = "Interact with " .. objectName
            else
                title = "Interact with Object"
            end
        end
    end

    if Support and type(Support.getInteractionCyclePosition) == "function"
            and type(Support.getInteractionCycleCount) == "function" then
        local okPosition, position = pcall(Support.getInteractionCyclePosition)
        local okCount, count = pcall(Support.getInteractionCycleCount)
        if okPosition and okCount and position > 0 and count > 1 then
            title = title .. " (" .. tostring(position) .. "/" .. tostring(count) .. ")"
        end
    end
    return title
end

local function applyInteractionTargetHeading(result, target)
    if type(result) ~= "table" then return result end
    local title = interactionTargetHeading(target)
    if title then result.title = title end
    return result
end

local function lazyInteractionHarvest(originalHarvest, player, target)
    local function harvest()
        return originalHarvest(player, target)
    end
    if not optionValue(options.lazyInteractionOptions, true)
            or not Support or type(Support.requestInteractionOptions) ~= "function" then
        pendingInteractionTarget = nil
        pendingInteractionTicks = 0
        return applyInteractionTargetHeading(harvest(), target)
    end

    if pendingInteractionTarget == target then
        pendingInteractionTarget = nil
        pendingInteractionTicks = 0
        return applyInteractionTargetHeading(harvest(), target)
    end

    pendingInteractionTarget = nil
    pendingInteractionTicks = 0
    if Support.clearVanillaContextMenuIcons then
        Support.clearVanillaContextMenuIcons()
    end

    local title = interactionTargetHeading(target)

    ViewpointInteract.actions = {{
        name = "View options",
        fn = function()
            pendingInteractionTarget = target
            pendingInteractionTicks = 0
            local ok, requested = pcall(Support.requestInteractionOptions)
            if not ok or requested ~= true then
                pendingInteractionTarget = nil
                pendingInteractionTicks = 0
                if not lazyInteractionWarningLogged then
                    diagnosticLog("could not request deferred interaction options")
                    lazyInteractionWarningLogged = true
                end
            end
        end,
        args = {},
        n = 0,
        enabled = true,
    }}

    return { title = title, labels = { "View options" }, enabled = { true }, seen = 0 }
end

local appendNeatLockpickActions

local function installLazyInteractionOptions()
    local interactions = ViewpointInteract
    if type(interactions) ~= "table" then return end

    if type(interactions.harvest) == "function"
            and interactions._extendedSupportVanillaIconHarvest == interactions.harvest
            and interactions._extendedSupportLazyHarvest ~= interactions.harvest then
        local originalHarvest = interactions.harvest
        local wrapper = function(player, object)
            local result = lazyInteractionHarvest(originalHarvest, player, object)
            return appendNeatLockpickActions(result, player, object)
        end
        interactions.harvest = wrapper
        interactions._extendedSupportLazyHarvest = wrapper
        logInteractionMenuDiagnostic("lazy-hook", "deferred interaction harvest hook installed")
    elseif type(interactions.harvest) == "function" then
        logInteractionMenuDiagnostic("lazy-hook", "waiting for the vanilla interaction harvest hook")
    end

    if type(interactions.harvestVehicle) == "function"
            and interactions._extendedSupportVanillaIconVehicle == interactions.harvestVehicle
            and interactions._extendedSupportLazyVehicle ~= interactions.harvestVehicle then
        local originalHarvestVehicle = interactions.harvestVehicle
        local wrapper = function(player, vehicle)
            return lazyInteractionHarvest(originalHarvestVehicle, player, vehicle)
        end
        interactions.harvestVehicle = wrapper
        interactions._extendedSupportLazyVehicle = wrapper
    end
end

local function neatLockpickDoorOptions(player, door)
    if not player or not door or type(instanceof) ~= "function" then return nil end

    local isDoor = false
    pcall(function()
        isDoor = instanceof(door, "IsoDoor")
            or (instanceof(door, "IsoThumpable") and door.isDoor and door:isDoor())
    end)
    if not isDoor then return nil end

    local sandbox = NeatLockpick_SandboxOptions
    local chances = NeatLockpick_Chances
    local unlock = NeatLockpick_Unlock
    local manager = NeatLockpickingManager
    if type(sandbox) ~= "table" or type(sandbox.isEnabled) ~= "function"
            or type(chances) ~= "table" or type(chances.canLockpick) ~= "function"
            or type(unlock) ~= "table" or type(manager) ~= "table" then return nil end

    local okEnabled, enabled = pcall(sandbox.isEnabled)
    if not okEnabled or enabled ~= true then return nil end

    local locked = false
    pcall(function()
        locked = (door.isLockedByKey and door:isLockedByKey())
            or (door.isLocked and door:isLocked()) or false
    end)
    if not locked then return nil end

    local inventory = player:getInventory()
    local hasLockpick = false
    if inventory and type(inventory.containsTypeRecurse) == "function" then
        local okLockpick, hasRegular = pcall(inventory.containsTypeRecurse, inventory,
            "NeatLockpicking.Lockpick")
        local okHandmade, hasHandmade = pcall(inventory.containsTypeRecurse, inventory,
            "NeatLockpicking.HandmadeLockpick")
        hasLockpick = (okLockpick and hasRegular == true) or (okHandmade and hasHandmade == true)
    end

    local canPick = false
    if hasLockpick then
        local okCanPick, allowed = pcall(chances.canLockpick, player)
        canPick = okCanPick and allowed == true
    end

    local hasCrowbar = false
    if type(unlock.playerHasCrowbar) == "function" then
        local okCrowbar, found = pcall(unlock.playerHasCrowbar, player)
        hasCrowbar = okCrowbar and found == true
    end

    if not canPick and not hasCrowbar then return nil end

    local crowbarEnabled = true
    if hasCrowbar and type(unlock.isTooExhaustedToCrowbar) == "function" then
        local okExhaustion, exhausted = pcall(unlock.isTooExhaustedToCrowbar, player)
        if okExhaustion then crowbarEnabled = exhausted ~= true end
    end

    return {
        canPick = canPick and type(manager.createBuildingDoor) == "function",
        canCrowbar = hasCrowbar and type(manager.createCrowbarDoor) == "function",
        crowbarEnabled = crowbarEnabled,
        manager = manager,
    }
end

local function interactionLabelExists(labels, optionLabel)
    for _, label in ipairs(labels) do
        if type(label) == "string" and label:find(optionLabel, 1, true) then return true end
    end
    return false
end

appendNeatLockpickActions = function(result, player, door)
    local options = neatLockpickDoorOptions(player, door)
    if not options then return result end

    result = type(result) == "table" and result or {}
    result.labels = type(result.labels) == "table" and result.labels or {}
    result.enabled = type(result.enabled) == "table" and result.enabled or {}
    result.deliberate = type(result.deliberate) == "table" and result.deliberate or {}
    result.tips = type(result.tips) == "table" and result.tips or {}
    result.title = result.title or interactionTargetHeading(door)

    local actions = ViewpointInteract.actions
    if type(actions) ~= "table" then
        actions = {}
        ViewpointInteract.actions = actions
    end

    local group = getText("ContextMenu_NeatLockpicking_Door")
    local candidates = {}
    if options.canPick then
        candidates[#candidates + 1] = {
            name = getText("ContextMenu_NeatLockpicking_Picklock"),
            enabled = true,
            fn = function() options.manager.createBuildingDoor(player, door, nil) end,
        }
    end
    if options.canCrowbar then
        candidates[#candidates + 1] = {
            name = getText("ContextMenu_NeatLockpicking_Crowbar"),
            enabled = options.crowbarEnabled,
            fn = function() options.manager.createCrowbarDoor(player, door) end,
        }
    end

    for _, candidate in ipairs(candidates) do
        if not interactionLabelExists(result.labels, candidate.name) then
            local label = group and group ~= "" and (group .. ": " .. candidate.name) or candidate.name
            local index = math.max(#result.labels, #actions) + 1
            result.labels[index] = label
            result.enabled[index] = candidate.enabled
            result.deliberate[index] = false
            result.tips[index] = nil
            actions[index] = {
                name = label,
                fn = candidate.fn,
                args = {},
                n = 0,
                enabled = candidate.enabled,
            }
        end
    end

    return result
end

local COMPANION_DOG_TAG_HEAD_OFFSET = 55
local COMPANION_DOG_TAG_WIDTH = 260
local COMPANION_DOG_TAG_LINE_HEIGHT = 16
local companionDogTagHookLogged = false
local companionDogTagProjectionLogged = false

local function installCompanionDogTagProjection()
    if not Support or type(Support.projectCompanionDogTag) ~= "function"
            or not UIManager or not UIManager.getUI then return end
    local ok, uiList = pcall(function() return UIManager.getUI() end)
    if not ok or not uiList or not uiList.size or not uiList.get then return end
    local okSize, size = pcall(function() return uiList:size() end)
    if not okSize then return end

    for i = 0, size - 1 do
        local okUi, ui = pcall(function() return uiList:get(i) end)
        if okUi and ui and ui.getTable then
            local okTable, uiTable = pcall(function() return ui:getTable() end)
            if okTable and uiTable and uiTable.Type == "ISCDNameTag"
                    and not uiTable._viewpointSupportDogTagPrerender then
                local originalPrerender = uiTable.prerender
                if type(originalPrerender) == "function" then
                    local wrapper = function(self, ...)
                        local result = originalPrerender(self, ...)
                        if self.draw and self.lines and self.dog and Support.isViewEnabled
                                and Support.isViewEnabled() then
                            local dog = self.dog
                            local projectedOk, projected = pcall(Support.projectCompanionDogTag,
                                self.playerNum, dog:getX(), dog:getY(), dog:getZ())
                            if projectedOk and projected and projected.size
                                    and projected:size() >= 2 then
                                local positionOk = pcall(function()
                                    local screenX = projected:get(0) - getPlayerScreenLeft(self.playerNum)
                                    local zoom = getCore():getZoom(self.playerNum)
                                    if not zoom or zoom <= 0 then zoom = 1 end
                                    local screenY = projected:get(1) - getPlayerScreenTop(self.playerNum)
                                        - COMPANION_DOG_TAG_HEAD_OFFSET / zoom
                                    self:setX(screenX - COMPANION_DOG_TAG_WIDTH / 2)
                                    self:setY(screenY - #self.lines * COMPANION_DOG_TAG_LINE_HEIGHT)
                                end)
                                if not positionOk then
                                    self.draw = false
                                elseif not companionDogTagProjectionLogged then
                                    diagnosticLog("Companion Dogs name-tag camera projection succeeded")
                                    companionDogTagProjectionLogged = true
                                end
                            else
                                self.draw = false
                            end
                        end
                        return result
                    end
                    uiTable.prerender = wrapper
                    uiTable._viewpointSupportDogTagPrerender = wrapper
                    if not companionDogTagHookLogged then
                        diagnosticLog("Companion Dogs name-tag camera hook installed")
                        companionDogTagHookLogged = true
                    end
                end
            end
        end
    end
end

local extractionTrackerClosePending = false

local function logInteractionCycleKeyEvent(key)
    if not optionValue(options.diagnostics, false) then return end

    local previousKey = optionValue(options.interactionPreviousKey, DEFAULT_PREVIOUS_INTERACTION_KEY)
    local nextKey = optionValue(options.interactionNextKey, DEFAULT_NEXT_INTERACTION_KEY)
    if key ~= previousKey and key ~= nextKey
            and key ~= 200 and key ~= 203 and key ~= 205 and key ~= 208 then return end

    print("[Viewpoint Extended Support] interaction cycling key event: key=" .. tostring(key)
        .. ", previousKey=" .. tostring(previousKey)
        .. ", nextKey=" .. tostring(nextKey))
end

if Events and Events.OnKeyPressed then
    Events.OnKeyPressed.Add(logInteractionCycleKeyEvent)
end

local function closeExtractionTrackerOnLoad()
    if not extractionTrackerClosePending then return end
    local trackerClass = WQS_GPSWindow
    if type(trackerClass) ~= "table" then return end
    local tracker = trackerClass.instance
    if type(tracker) ~= "table" or type(tracker.isVisible) ~= "function" then return end

    local ok, visible = pcall(function() return tracker:isVisible() end)
    if not ok then return end

    if visible then
        local closeOk = pcall(function()
            tracker:setVisible(false)
            tracker:removeFromUIManager()
        end)
        if not closeOk then return end
    end

    WQSCheckFlip = true
    extractionTrackerClosePending = false
end

Events.OnTick.Add(function()
    tickCounter = tickCounter + 1
    rememberOnFootCamera()
    maintainVehicleCamera()
    closeExtractionTrackerOnLoad()
    installCursorWindowHooks()
    if pendingInteractionTarget then
        pendingInteractionTicks = pendingInteractionTicks + 1
        if pendingInteractionTicks > 180 then
            pendingInteractionTarget = nil
            pendingInteractionTicks = 0
        end
    end
    if Support and Support.diagnosticTick then
        Support.diagnosticTick()
    end
    if tickCounter % 10 == 0 then
        PlayerHudVisibility.install(options.hidePlayerHud)
        syncConfiguration()
        installVanillaInventoryCursorIntegration()
        installErgonomicUIIntegration()
        installContextMenuIconsIntegration()
        installViewpointVanillaMenuIconsIntegration()
        installLazyInteractionOptions()
        installCompanionDogTagProjection()
    end
    updateInteractionMenuSuppression()
    updateCursorRequest()
end)

Events.OnGameStart.Add(function()
    PlayerHudVisibility.install(options.hidePlayerHud)
    extractionTrackerClosePending = optionValue(options.closeExtractionTrackerOnLoad, true)
    closeExtractionTrackerOnLoad()
    syncConfiguration()
    rememberOnFootCamera()
    installCursorWindowHooks()
    installVanillaInventoryCursorIntegration()
    installErgonomicUIIntegration()
    installContextMenuIconsIntegration()
    installViewpointVanillaMenuIconsIntegration()
    installLazyInteractionOptions()
    installCompanionDogTagProjection()
    if optionValue(options.startViewpoint, false) and Support and Support.enableViewpoint then
        Support.enableViewpoint()
    end
end)

syncConfiguration()
logInteractionMenuDiagnostic("diagnostics", "interaction diagnostics active (0.4.35)")
