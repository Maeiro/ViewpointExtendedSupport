# Viewpoint Extended Support

Optional compatibility bridge for Viewpoint on Project Zomboid Build 42.

The mod uses ZombieBuddy's ByteBuddy runtime patching API and does not modify the Viewpoint JAR. It adds configurable key binds, vehicle camera switching, third-person camera zoom with the mouse wheel or configurable keyboard keys (minus/equal by default), optional cascading Viewpoint interaction menus, Simple Context Menu Icons integration, hold-to-use free mouse mode, UI cursor activation, Ergonomic UI integration, and cursor/reticle visibility that follows the actual free-cursor state. Hold-to-use free mouse mode is enabled by default on Mouse Button 4; the first/third-person toggle defaults to Shift+Z. Custom binds can use Shift, Ctrl, Alt, or combinations. Hold-to-use free mouse mode leaves Viewpoint's middle-mouse cursor toggle intact. Firearm aiming, targeting, and projectile visuals are left entirely to Viewpoint.

When cascading interaction menus are enabled, grouped actions open a nested list with a `< Back` entry; final actions still run through Viewpoint's original handlers. The setting is enabled by default and can be changed in the mod options.

The Viewpoint interaction menu is hidden while the vanilla or Ergonomic UI inventory is open. This behavior is enabled by default and can be changed in the mod options.

By default, Viewpoint shows a single `View options` row when you look at an interactable object. It only builds the full interaction menu after that row is selected, reducing the work done while scanning nearby objects. This behavior can be disabled in the mod options.

Viewpoint interaction rows retain icons attached to the original vanilla context-menu actions, including item-specific textures supplied through `itemForTexture` in nested actions. When Context Menu Icons Core and an icon pack are active, matching pack icons are also used as a fallback; the core mod is optional.

When Project A-Life and Companion Dogs are active, Friendly and Allied A-Life NPCs are excluded from the dog's threat scans; other stances and regular zombies remain detectable. Companion Dogs name tags are also positioned using Viewpoint's camera projection.

When Project A-Life is active, its NPC interaction actions are exposed through Viewpoint's interaction menu, and aiming at an A-Life NPC displays that NPC's tag above them. These features are optional and do not affect Viewpoint interactions with other targets.

Dependencies:

- ZombieBuddy 2.3.0 or newer
- Viewpoint
