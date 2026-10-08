require "PZAPI/ModOptions"

local Support = ViewpointExtendedSupport

local function keyOrFallback(name, fallback)
    if Keyboard and Keyboard[name] then return Keyboard[name] end
    return fallback
end

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
    options.viewModeKey = modOptions:addKeyBind("ViewModeToggleKey", "Toggle first/third-person mode", keyOrFallback("KEY_Z", 44))
    options.viewModeModifier = addModifierOption(modOptions, "ViewModeToggleModifier", "View mode toggle modifiers", 2)
    options.holdCursor = modOptions:addTickBox("HoldFreeCursor", "Hold a key to use free mouse", true)
    options.cursorKey = modOptions:addKeyBind("FreeCursorKey", "Free mouse hold key", mouseButtonOrFallback(3, 1003))
    options.cursorModifier = addModifierOption(modOptions, "FreeCursorModifier", "Free mouse modifiers", 1)
    options.autoUi = modOptions:addTickBox("AutoFreeCursorInUI", "Automatically use free mouse in inventories and menus", true)
    options.ergonomicUi = modOptions:addTickBox("ErgonomicUIIntegration", "Integrate with Ergonomic UI", true)
    options.vehicleCamera = modOptions:addTickBox("ThirdPersonInVehicles", "Use third person in vehicles", true)
    options.thirdPersonScrollZoom = modOptions:addTickBox("ThirdPersonScrollZoom", "Zoom third-person camera with mouse wheel", true)
    options.thirdPersonZoomOutKey = modOptions:addKeyBind("ThirdPersonZoomOutKey", "Zoom third-person camera out", keyOrFallback("KEY_MINUS", 12))
    options.thirdPersonZoomOutModifier = addModifierOption(modOptions, "ThirdPersonZoomOutModifier", "Zoom out modifiers", 1)
    options.thirdPersonZoomInKey = modOptions:addKeyBind("ThirdPersonZoomInKey", "Zoom third-person camera in", keyOrFallback("KEY_EQUALS", 13))
    options.thirdPersonZoomInModifier = addModifierOption(modOptions, "ThirdPersonZoomInModifier", "Zoom in modifiers", 1)
    options.groupContextMenuActions = modOptions:addTickBox("GroupContextMenuActions", "Use cascading Viewpoint interaction menus", true)
    options.lazyInteractionOptions = modOptions:addTickBox("LazyInteractionOptions", "Show View options before building interaction menus", true)
    options.hideInteractionMenuWhenInventoryOpen = modOptions:addTickBox("HideInteractionMenuWhenInventoryOpen", "Hide Viewpoint interaction menu while inventory is open", true)
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

local function modifierMask(option, fallback)
    local masks = { 0, 1, 2, 4, 3, 5, 6, 7 }
    return masks[optionValue(option, fallback or 1)] or 0
end

local function packedModifierMasks()
    return modifierMask(options.cursorModifier, 1)
        + modifierMask(options.viewModeModifier, 2) * 8
        + modifierMask(options.thirdPersonZoomOutModifier, 1) * 64
        + modifierMask(options.thirdPersonZoomInModifier, 1) * 512
end

local function syncConfiguration()
    if not Support or not Support.configure then return end
    Support.configure(
        24,
        24,
        true,
        optionValue(options.holdCursor, true),
        optionValue(options.cursorKey, 1003),
        optionValue(options.autoUi, true),
        optionValue(options.viewModeKey, 44),
        optionValue(options.cursorDiagnostics, true),
        optionValue(options.thirdPersonScrollZoom, true),
        optionValue(options.skipSetup, true),
        optionValue(options.thirdPersonZoomOutKey, 12),
        optionValue(options.thirdPersonZoomInKey, 13),
        optionValue(options.groupContextMenuActions, true),
        packedModifierMasks()
    )
end

local function isVisible(ui)
    if not ui or not ui.isVisible then return false end
    local ok, value = pcall(function() return ui:isVisible() end)
    return ok and value == true
end

local function hasClassName(ui, names)
    if not ui or not instanceof then return false end
    if type(ui) ~= "table" then
        local ok, uiTable = pcall(function() return ui:getTable() end)
        if not ok or not uiTable then return false end
        ui = uiTable
    end
    for _, name in ipairs(names) do
        local ok, value = pcall(function() return instanceof(ui, name) end)
        if ok and value then return true end
    end
    return false
end

local UI_CLASSES = {
    "ISInventoryPage",
    "ISRadialMenu",
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

local function visibleErgonomicInventory()
    local panel = ErgonomicUI and ErgonomicUI.panel
    if not isVisible(panel) or not panel.isBodyVisible then return false end
    local ok, value = pcall(function() return panel:isBodyVisible() end)
    return ok and value == true
end

local function updateInteractionMenuSuppression()
    if not Support then return end
    local inventoryOpen = visibleInventory(getPlayerInventory)
        or visibleInventory(getPlayerLoot)
        or visibleErgonomicInventory()
    if Support.setInventoryOpen then
        Support.setInventoryOpen(inventoryOpen)
    end
    if Support.setInteractionMenuSuppressed then
        Support.setInteractionMenuSuppressed(
            optionValue(options.hideInteractionMenuWhenInventoryOpen, true) and inventoryOpen
        )
    end
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
            self._viewpointSupportCursorRequested = visible
            updateInteractionMenuSuppression()
            return result
        end
    end

    if ErgUIPanel.revealFromContainer then
        local originalReveal = ErgUIPanel.revealFromContainer
        ErgUIPanel.revealFromContainer = function(self, ...)
            local result = originalReveal(self, ...)
            self._viewpointSupportCursorRequested = true
            updateInteractionMenuSuppression()
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

local nilActionArgument = {}

local function actionIndexNode(index, fn, args, create)
    if fn == nil or type(args) ~= "table" then return nil end
    local node = index[fn]
    if not node then
        if not create then return nil end
        node = {}
        index[fn] = node
    end

    for argument = 1, 11 do
        local key = args[argument]
        if type(key) == "number" and key ~= key then return nil end
        if key == nil then key = nilActionArgument end
        local child = node[key]
        if not child then
            if not create then return nil end
            child = {}
            node[key] = child
        end
        node = child
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
    for _, entry in ipairs(captured) do
        local option = entry.option
        if not option.subOption then
            local node = actionIndexNode(iconIndex, option.onSelect, optionArguments(option), true)
            if node then
                node.entries = node.entries or {}
                table.insert(node.entries, entry)
            end
        end
    end

    for _, action in ipairs(actions) do
        local node = actionIndexNode(iconIndex, action.fn, action.args, false)
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
local lastCharacterHarvestTarget
local lastAimedALifeNpc
local alifeNpcTagMenuFallbackLogged = false
local lastALifeHarvestDiagnostic
local lastALifeHarvestDiagnosticAt = 0

local function logInteractionMenuDiagnostic(key, message)
    if interactionMenuDiagnosticStates[key] == message then return end
    interactionMenuDiagnosticStates[key] = message
    print("[Viewpoint Extended Support] interaction menu: " .. message)
end

local function isProjectALifeNpc(target)
    if not target then return false end

    local speech = ProjectALife and ProjectALife.Speech
    if type(speech) == "table" and type(speech.variable) == "function" then
        local ok, uid = pcall(speech.variable, target, "ALifeUID")
        if ok and uid ~= nil and uid ~= "" then return true end
    end

    if type(target.getModData) == "function" then
        local ok, data = pcall(target.getModData, target)
        return ok and type(data) == "table" and type(data.ProjectALifeUID) == "string"
            and data.ProjectALifeUID ~= ""
    end
    return false
end

local function collectContextOptions(menu, options, visited)
    if not menu or visited[menu] or type(menu.options) ~= "table" then return end
    visited[menu] = true

    for _, option in ipairs(menu.options) do
        if type(option) == "table" then
            options[option] = true
            if option.subOption and type(menu.getSubMenu) == "function" then
                local ok, submenu = pcall(function() return menu:getSubMenu(option.subOption) end)
                if ok then collectContextOptions(submenu, options, visited) end
            end
        end
    end
end

local function linkNewContextActionsToTarget(menu, target, existingOptions, visited)
    if not menu or visited[menu] or type(menu.options) ~= "table" then return 0 end
    visited[menu] = true

    local linked = 0
    for _, option in ipairs(menu.options) do
        if type(option) == "table" then
            if not existingOptions[option] then
                local lastParameter = 0
                local alreadyTargetsNpc = option.target == target
                for index = 1, 10 do
                    local parameter = option["param" .. index]
                    if parameter ~= nil then lastParameter = index end
                    if parameter == target then alreadyTargetsNpc = true end
                end
                if option.onSelect and not option.subOption and not alreadyTargetsNpc
                        and lastParameter < 10 then
                    option["param" .. (lastParameter + 1)] = target
                    linked = linked + 1
                end
            end
            if option.subOption and type(menu.getSubMenu) == "function" then
                local ok, submenu = pcall(function() return menu:getSubMenu(option.subOption) end)
                if ok then
                    linked = linked + linkNewContextActionsToTarget(submenu, target,
                        existingOptions, visited)
                end
            end
        end
    end
    return linked
end

local function diagnoseCharacterHarvestTarget(target)
    local isCharacter = false
    if target and type(instanceof) == "function" then
        local ok, result = pcall(instanceof, target, "IsoGameCharacter")
        isCharacter = ok and result == true
    end

    if not isCharacter then
        lastCharacterHarvestTarget = nil
        lastAimedALifeNpc = nil
        return
    end

    local isALifeNpc = isProjectALifeNpc(target)
    lastAimedALifeNpc = isALifeNpc and target or nil
    if target == lastCharacterHarvestTarget then return end

    lastCharacterHarvestTarget = target
    logInteractionMenuDiagnostic("target", "Viewpoint harvested a character target; Project A-Life NPC="
        .. tostring(isALifeNpc))
end

local function logALifeHarvestResult(message)
    local now = type(getTimestampMs) == "function" and getTimestampMs() or 0
    if message == lastALifeHarvestDiagnostic and now - lastALifeHarvestDiagnosticAt < 3000 then return end
    lastALifeHarvestDiagnostic = message
    lastALifeHarvestDiagnosticAt = now
    print("[Viewpoint Extended Support] A-Life interaction harvest: " .. message)
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
        local player, target = ...
        local isALifeNpc = isProjectALifeNpc(target)
        local contextMenuClass = ISContextMenu
        local originalContextMenuGet = isALifeNpc and type(contextMenuClass) == "table"
            and contextMenuClass.get or nil
        local capturedContext
        local interceptContextMenuGet
        if type(originalContextMenuGet) == "function" then
            interceptContextMenuGet = function(...)
                local menu = originalContextMenuGet(...)
                if capturedContext == nil then capturedContext = menu end
                return menu
            end
            contextMenuClass.get = interceptContextMenuGet
        end

        local originalTriggerEvent = isALifeNpc and type(triggerEvent) == "function" and triggerEvent or nil
        local fillEventTriggered = false
        local linkedContextActions = 0
        local interceptTriggerEvent
        if originalTriggerEvent then
            interceptTriggerEvent = function(eventName, ...)
                local args = { ... }
                local eventContext = eventName == "OnFillWorldObjectContextMenu" and args[2] or nil
                local isTargetFill = eventContext ~= nil and eventContext == capturedContext
                local existingOptions
                if isTargetFill then
                    existingOptions = {}
                    collectContextOptions(eventContext, existingOptions, {})
                end
                local result = originalTriggerEvent(eventName, ...)
                if isTargetFill then
                    fillEventTriggered = true
                    linkedContextActions = linkedContextActions + linkNewContextActionsToTarget(
                        eventContext, target, existingOptions, {})
                end
                return result
            end
            triggerEvent = interceptTriggerEvent
        end

        local captured = {}
        local originalCreateMenu = worldMenu.createMenu
        local menuBuilder = worldMenu._NB_old_createMenu
        if type(menuBuilder) ~= "function" then
            menuBuilder = originalCreateMenu
        end
        local createMenuCalled = false
        local fetchCount
        local recoveredContext = false
        local filledRecoveredContext = false
        local interceptCreateMenu = function(...)
            createMenuCalled = true
            local menu = menuBuilder(...)
            if isALifeNpc and capturedContext ~= nil then
                local fetch = worldMenu.fetchVars
                fetchCount = type(fetch) == "table" and tonumber(fetch.c) or nil
                if not fillEventTriggered and originalTriggerEvent and player
                        and type(player.getPlayerNum) == "function" then
                    local ok, playerNum = pcall(player.getPlayerNum, player)
                    if ok then
                        local existingOptions = {}
                        collectContextOptions(capturedContext, existingOptions, {})
                        local fillOk = pcall(originalTriggerEvent, "OnFillWorldObjectContextMenu", playerNum,
                            capturedContext, { target }, false)
                        filledRecoveredContext = fillOk
                        fillEventTriggered = fillOk
                        if fillOk then
                            linkedContextActions = linkedContextActions + linkNewContextActionsToTarget(
                                capturedContext, target, existingOptions, {})
                        end
                    end
                end
                if type(menu) ~= "table" then
                    menu = capturedContext
                    recoveredContext = true
                end
            end
            captureVanillaMenuIcons(menu, captured, {})
            return menu
        end

        worldMenu.createMenu = interceptCreateMenu
        local ok, result = pcall(originalHarvest, ...)
        if worldMenu.createMenu == interceptCreateMenu then
            worldMenu.createMenu = originalCreateMenu
        end
        if interceptTriggerEvent and triggerEvent == interceptTriggerEvent then
            triggerEvent = originalTriggerEvent
        end
        if interceptContextMenuGet and contextMenuClass.get == interceptContextMenuGet then
            contextMenuClass.get = originalContextMenuGet
        end

        if isALifeNpc then
            local resultSummary
            if not ok then
                resultSummary = "harvest failed: " .. tostring(result)
            elseif type(result) == "table" and type(result.labels) == "table" then
                local labels = {}
                for index = 1, math.min(#result.labels, 8) do
                    labels[index] = tostring(result.labels[index])
                end
                resultSummary = "menu labels=" .. tostring(#result.labels)
                    .. ", seen=" .. tostring(result.seen)
                    .. ", entries=" .. table.concat(labels, " | ")
            else
                resultSummary = "no labels; reason=" .. tostring(type(result) == "table" and result.why or result)
            end
            logALifeHarvestResult(resultSummary .. "; createMenu=" .. tostring(createMenuCalled)
                .. ", fetchCount=" .. tostring(fetchCount)
                .. ", fillEvent=" .. tostring(fillEventTriggered)
                .. ", npcActionsLinked=" .. tostring(linkedContextActions)
                .. ", contextRecovered=" .. tostring(recoveredContext)
                .. ", recoveredOnFill=" .. tostring(filledRecoveredContext))
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

local function lazyInteractionHarvest(originalHarvest, player, target)
    diagnoseCharacterHarvestTarget(target)
    if not optionValue(options.lazyInteractionOptions, true)
            or not Support or type(Support.requestInteractionOptions) ~= "function" then
        pendingInteractionTarget = nil
        pendingInteractionTicks = 0
        return originalHarvest(player, target)
    end

    if pendingInteractionTarget == target then
        pendingInteractionTarget = nil
        pendingInteractionTicks = 0
        return originalHarvest(player, target)
    end

    pendingInteractionTarget = nil
    pendingInteractionTicks = 0
    if Support.clearVanillaContextMenuIcons then
        Support.clearVanillaContextMenuIcons()
    end

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
                    print("[Viewpoint Extended Support] could not request deferred interaction options")
                    lazyInteractionWarningLogged = true
                end
            end
        end,
        args = {},
        n = 0,
        enabled = true,
    }}

    return { labels = { "View options" }, enabled = { true }, seen = 0 }
end

local function installLazyInteractionOptions()
    local interactions = ViewpointInteract
    if type(interactions) ~= "table" then return end

    if type(interactions.harvest) == "function"
            and interactions._extendedSupportVanillaIconHarvest == interactions.harvest
            and interactions._extendedSupportLazyHarvest ~= interactions.harvest then
        local originalHarvest = interactions.harvest
        local wrapper = function(player, object)
            return lazyInteractionHarvest(originalHarvest, player, object)
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

local function viewpointAimHudActive()
    if not Support or type(Support.isViewEnabled) ~= "function" then return false end
    local ok, enabled = pcall(Support.isViewEnabled)
    if not ok or enabled ~= true then return false end
    if type(Support.shouldSkipInteractionMenu) == "function" then
        local skipOk, skip = pcall(Support.shouldSkipInteractionMenu)
        if skipOk and skip == true then return false end
    end
    return true
end

local function aimedALifeNpc()
    if Support and type(Support.getAimedInteractionObject) == "function" then
        local ok, target = pcall(Support.getAimedInteractionObject)
        if ok then
            if target and isProjectALifeNpc(target) then
                lastAimedALifeNpc = target
                return target
            end
            lastAimedALifeNpc = nil
            return nil
        end
    end

    if Support and type(Support.getAimedALifeNpc) == "function" then
        local ok, target = pcall(Support.getAimedALifeNpc)
        if ok and target then
            lastAimedALifeNpc = target
            return target
        end
    end

    if lastAimedALifeNpc then
        if not alifeNpcTagMenuFallbackLogged then
            print("[Viewpoint Extended Support] A-Life NPC tag using the confirmed interaction-menu target")
            alifeNpcTagMenuFallbackLogged = true
        end
        return lastAimedALifeNpc
    end
    return nil
end

local function alifeNpcUid(target)
    local speech = ProjectALife and ProjectALife.Speech
    if type(speech) == "table" and type(speech.variable) == "function" then
        local ok, uid = pcall(speech.variable, target, "ALifeUID")
        if ok and uid ~= nil and uid ~= "" then return tostring(uid) end
    end
    if target and type(target.getModData) == "function" then
        local ok, data = pcall(target.getModData, target)
        if ok and type(data) == "table" and data.ProjectALifeUID ~= nil
                and data.ProjectALifeUID ~= "" then
            return tostring(data.ProjectALifeUID)
        end
    end
    return nil
end

local ALIFE_NPC_TAG_LOWERING_TILES = 0.2

local function placeNpcTagAboveAimedNpc(label, target)
    if not target or not Support or type(Support.projectCompanionDogTag) ~= "function" then
        return false
    end

    local ok, projected = pcall(function()
        return Support.projectCompanionDogTag(0, target:getX(), target:getY(),
            target:getZ() - ALIFE_NPC_TAG_LOWERING_TILES)
    end)
    if not ok or not projected or not projected.size or projected:size() < 2 then return false end

    local okPosition, screenX, screenY = pcall(function()
        return projected:get(0) - getPlayerScreenLeft(0),
            projected:get(1) - getPlayerScreenTop(0)
    end)
    if not okPosition then return false end

    label:setX(screenX - label.width / 2)
    label:setY(screenY - label.height - 4)
    return true
end

local alifeNpcTagIntegrationLogged = false
local alifeNpcTagWaitingLogged = false
local alifeNpcTagLabelLogged = false
local alifeNpcTagTargetLogged = false
local alifeNpcTagNoAimLogged = false
local alifeNpcTagUidFailureLogged = false
local alifeNpcTagDataFailureLogged = false
local alifeNpcTagProjectionLogged = false

local function installALifeNpcTagIntegration()
    local npcTags = ProjectALifeJimmy and ProjectALifeJimmy.NpcTags
    if type(npcTags) ~= "table" or type(npcTags.hovered) ~= "function"
            or type(npcTags.extraLines) ~= "function" then
        if not alifeNpcTagWaitingLogged then
            print("[Viewpoint Extended Support] A-Life NPC tag integration is waiting for ProjectALifeJimmy.NpcTags")
            alifeNpcTagWaitingLogged = true
        end
        return
    end

    if npcTags._viewpointSupportHovered ~= npcTags.hovered then
        local originalHovered = npcTags.hovered
        local wrapper = function(playerNum, player)
            npcTags._viewpointSupportAimedTarget = nil
            if not viewpointAimHudActive() then return originalHovered(playerNum, player) end
            local target = aimedALifeNpc()
            if not target and not alifeNpcTagNoAimLogged then
                print("[Viewpoint Extended Support] A-Life NPC tag has no Viewpoint aimed NPC target")
                alifeNpcTagNoAimLogged = true
            end
            local uid = target and alifeNpcUid(target) or nil
            if not uid then
                if target and not alifeNpcTagUidFailureLogged then
                    print("[Viewpoint Extended Support] A-Life NPC tag target has no A-Life UID")
                    alifeNpcTagUidFailureLogged = true
                end
                return nil
            end
            if not alifeNpcTagTargetLogged then
                print("[Viewpoint Extended Support] A-Life NPC tag resolved an aimed NPC")
                alifeNpcTagTargetLogged = true
            end
            npcTags._viewpointSupportAimedTarget = target
            return { uid = uid, shell = target }, 0, 0, 1
        end
        npcTags.hovered = wrapper
        npcTags._viewpointSupportHovered = wrapper
    end

    if npcTags._viewpointSupportExtraLines ~= npcTags.extraLines then
        local originalExtraLines = npcTags.extraLines
        local wrapper = function(...)
            local lines = originalExtraLines(...)
            if not viewpointAimHudActive() then return lines end

            local withoutSpots
            for index, line in ipairs(lines) do
                if type(line) == "table" and line.spot ~= nil then
                    if not withoutSpots then
                        withoutSpots = {}
                        for previous = 1, index - 1 do
                            withoutSpots[previous] = lines[previous]
                        end
                    end
                    local copy = {}
                    for key, value in pairs(line) do
                        if key ~= "spot" then copy[key] = value end
                    end
                    withoutSpots[index] = copy
                elseif withoutSpots then
                    withoutSpots[index] = line
                end
            end
            return withoutSpots or lines
        end
        npcTags.extraLines = wrapper
        npcTags._viewpointSupportExtraLines = wrapper
    end
    if not alifeNpcTagIntegrationLogged then
        print("[Viewpoint Extended Support] A-Life NPC tag hover integration installed")
        alifeNpcTagIntegrationLogged = true
    end

    local label = npcTags.label
    if label and type(label.prerender) == "function"
            and label._viewpointSupportPrerender ~= label.prerender then
        local originalPrerender = label.prerender
        local wrapper = function(self, ...)
            local result = originalPrerender(self, ...)
            if viewpointAimHudActive() then
                local target = npcTags._viewpointSupportAimedTarget
                if target and not self.show then
                    if not alifeNpcTagDataFailureLogged then
                        print("[Viewpoint Extended Support] A-Life NPC tag target found, but its tag data produced no label")
                        alifeNpcTagDataFailureLogged = true
                    end
                elseif self.show then
                    local projected = placeNpcTagAboveAimedNpc(self, target)
                    if not projected then self.show = nil end
                    if not alifeNpcTagProjectionLogged then
                        print("[Viewpoint Extended Support] A-Life NPC tag head projection "
                            .. (projected and "succeeded" or "failed"))
                        alifeNpcTagProjectionLogged = true
                    end
                end
            end
            return result
        end
        label.prerender = wrapper
        label._viewpointSupportPrerender = wrapper
    end
    if label and type(label.prerender) == "function" and not alifeNpcTagLabelLogged then
        print("[Viewpoint Extended Support] A-Life NPC tag label hook installed")
        alifeNpcTagLabelLogged = true
    end
end

local COMPANION_DOG_TAG_HEIGHT_ADJUSTMENT = 0.5

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
                                self.playerNum, dog:getX(), dog:getY(),
                                dog:getZ() - COMPANION_DOG_TAG_HEIGHT_ADJUSTMENT)
                            if projectedOk and projected and projected.size
                                    and projected:size() >= 2 then
                                local screenX = projected:get(0) - getPlayerScreenLeft(self.playerNum)
                                local screenY = projected:get(1) - getPlayerScreenTop(self.playerNum)
                                self:setX(screenX - 130)
                                self:setY(screenY - #self.lines * 16)
                            else
                                self.draw = false
                            end
                        end
                        return result
                    end
                    uiTable.prerender = wrapper
                    uiTable._viewpointSupportDogTagPrerender = wrapper
                end
            end
        end
    end
end

local tickCounter = 0
Events.OnTick.Add(function()
    tickCounter = tickCounter + 1
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
    if Support and Support.pollThirdPersonZoom then
        Support.pollThirdPersonZoom()
    end
    if tickCounter % 10 == 0 then
        syncConfiguration()
        installVanillaInventoryCursorIntegration()
        installErgonomicUIIntegration()
        installContextMenuIconsIntegration()
        installViewpointVanillaMenuIconsIntegration()
        installLazyInteractionOptions()
        installALifeNpcTagIntegration()
        installCompanionDogTagProjection()
    end
    updateInteractionMenuSuppression()
    updateCursorRequest()
end)

Events.OnGameStart.Add(function()
    syncConfiguration()
    installVanillaInventoryCursorIntegration()
    installErgonomicUIIntegration()
    installContextMenuIconsIntegration()
    installViewpointVanillaMenuIconsIntegration()
    installLazyInteractionOptions()
    installALifeNpcTagIntegration()
    installCompanionDogTagProjection()
    if optionValue(options.startViewpoint, true) and Support and Support.enableViewpoint then
        Support.enableViewpoint()
    end
end)

syncConfiguration()
logInteractionMenuDiagnostic("diagnostics", "interaction diagnostics active (0.4.35)")
