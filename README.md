# Viewpoint Extended Support

Optional compatibility bridge for Viewpoint on Project Zomboid Build 42.

The mod uses ZombieBuddy's ByteBuddy runtime patching API and does not modify the Viewpoint JAR. It adds configurable key binds, vehicle camera switching, third-person camera zoom with the mouse wheel or configurable keyboard keys (minus/equal by default), optional cascading Viewpoint interaction menus, Simple Context Menu Icons integration, hold-to-use free mouse mode, UI cursor activation, Ergonomic UI integration, and cursor/reticle visibility that follows the actual free-cursor state. Hold-to-use free mouse mode leaves Viewpoint's middle-mouse cursor toggle intact. Firearm aiming, targeting, and projectile visuals are left entirely to Viewpoint.

When cascading interaction menus are enabled, grouped actions open a nested list with a `< Back` entry; final actions still run through Viewpoint's original handlers. The setting is enabled by default and can be changed in the mod options.

Viewpoint interaction rows retain icons attached to the original vanilla context-menu actions, including item-specific textures supplied through `itemForTexture` in nested actions. When Context Menu Icons Core and an icon pack are active, matching pack icons are also used as a fallback; the core mod is optional.

Dependencies:

- ZombieBuddy 2.3.0 or newer
- Viewpoint
