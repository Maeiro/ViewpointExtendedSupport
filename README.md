# Viewpoint Extended Support

Optional compatibility bridge for Viewpoint on Project Zomboid Build 42.

The mod uses ZombieBuddy's ByteBuddy runtime patching API and does not modify the Viewpoint JAR. It adds configurable key binds, vehicle camera switching, third-person camera zoom with the mouse wheel, hold-to-use free mouse mode, UI cursor activation, Ergonomic UI integration, and cursor/reticle visibility that follows the actual free-cursor state. It keeps Viewpoint's white dot outside firearm aiming. While aiming a firearm, it draws a centered crosshair that turns red on a valid target and outlines the targeted zombie. It adapts PZ3D's camera-ray ballistics flow so the muzzle, shot, and aiming pose follow the camera ray instead of snapping to nearby targets, including when aiming below the horizon at prone targets.

Dependencies:

- ZombieBuddy 2.3.0 or newer
- Viewpoint
