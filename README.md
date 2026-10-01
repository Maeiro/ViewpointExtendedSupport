# Viewpoint Extended Support

Optional compatibility bridge for Viewpoint on Project Zomboid Build 42.

The mod uses ZombieBuddy's ByteBuddy runtime patching API and does not modify the Viewpoint JAR. It adds configurable key binds, vehicle camera switching, third-person camera zoom with the mouse wheel, hold-to-use free mouse mode, UI cursor activation, Ergonomic UI integration, and cursor/reticle visibility that follows the actual free-cursor state. In Viewpoint aiming mode it centers the vanilla firearm reticle, replaces Viewpoint's center pixel, translates the camera's vertical aim into the 3D muzzle direction and weapon animation, and keeps valid targets aligned with that 3D aim ray so players can aim below the horizon.

Dependencies:

- ZombieBuddy 2.3.0 or newer
- Viewpoint
