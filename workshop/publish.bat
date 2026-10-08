@echo off
setlocal
set "STEAMCMD=D:\SteamCMD\steamcmd.exe"
set "ITEM_VDF=D:\SteamCMD\workshop\ViewpointExtendedSupport\publish.vdf"

if not exist "%STEAMCMD%" (
    echo SteamCMD was not found at "%STEAMCMD%".
    pause
    exit /b 1
)

if not exist "%ITEM_VDF%" (
    echo Workshop manifest was not found at "%ITEM_VDF%".
    pause
    exit /b 1
)

echo This will publish Viewpoint Extended Support as a public Workshop item.
echo SteamCMD may prompt for your password and Steam Guard code. Do not share them in chat.
set /p "STEAM_ACCOUNT=Steam account name: "
if not defined STEAM_ACCOUNT (
    echo A Steam account name is required.
    pause
    exit /b 1
)

"%STEAMCMD%" +login "%STEAM_ACCOUNT%" +workshop_build_item "%ITEM_VDF%" +quit
set "STEAMCMD_EXIT=%ERRORLEVEL%"

echo.
echo SteamCMD exited with code %STEAMCMD_EXIT%.
echo On first publish, note the new PublishFileID shown above; the manifest must use it for future updates.
pause
exit /b %STEAMCMD_EXIT%
