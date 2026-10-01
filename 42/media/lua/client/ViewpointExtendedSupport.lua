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
        optionValue(options.skipSetup, true)
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
    end
    updateCursorRequest()
end)

Events.OnGameStart.Add(function()
    syncConfiguration()
    installErgonomicUIIntegration()
    if optionValue(options.startViewpoint, true) and Support and Support.enableViewpoint then
        Support.enableViewpoint()
    end
end)

syncConfiguration()
