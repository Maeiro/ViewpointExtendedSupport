# Viewpoint Extended Support

Optional compatibility bridge for Viewpoint on Project Zomboid Build 42.

The mod uses ZombieBuddy's ByteBuddy runtime patching API and does not modify the Viewpoint JAR. It adds configurable key binds, vehicle camera switching, third-person camera zoom with the mouse wheel, hold-to-use free mouse mode, UI cursor activation, Ergonomic UI integration, and cursor/reticle visibility that follows the actual free-cursor state. In Viewpoint aiming mode it draws a centered PZ3D-style crosshair and adapts PZ3D's camera-ray ballistics flow: the muzzle is centered on the camera ray, the native ballistics reticle follows that ray, and the controller's 3D aiming position stays on the ray rather than snapping to nearby targets. This supports aiming below the horizon, including at prone targets.

Dependencies:

- ZombieBuddy 2.3.0 or newer
- Viewpoint
