# Viewpoint Extended Support

Optional compatibility bridge for Viewpoint on Project Zomboid Build 42.

Current version: 0.4.44.

The mod uses ZombieBuddy's ByteBuddy runtime patching API and does not modify the Viewpoint JAR. It adds vehicle camera switching, optional cascading Viewpoint interaction menus, Simple Context Menu Icons integration, hold-to-use free mouse mode, UI cursor activation, Ergonomic UI integration, and cursor/reticle visibility that follows the actual free-cursor state. Hold-to-use free mouse mode is enabled by default on Mouse Button 4; its configurable bind can use Shift, Ctrl, Alt, or combinations. Hold-to-use free mouse mode leaves Viewpoint's middle-mouse cursor toggle intact. Firearm aiming, targeting, and projectile visuals are left entirely to Viewpoint.

When cascading interaction menus are enabled, grouped actions open a nested list with a `< Back` entry; final actions still run through Viewpoint's original handlers. The setting is enabled by default and can be changed in the mod options.

The Viewpoint interaction menu is hidden while the vanilla or Ergonomic UI inventory is open. Collapsing an inventory restores access to interaction options and releases cursor ownership from a collapsed Ergonomic UI panel. This behavior is enabled by default and can be changed in the mod options.

Automatic UI cursor support centers the mouse once when an inventory or supported menu opens. It stays free while the interface remains open; reopening the interface centers it again without restoring old cursor positions.

When Wolf Extraction Quest is installed, its Extraction Tracker starts closed whenever a save is loaded. This behavior can be changed in the mod options; the tracker can still be opened manually in-game.

By default, Viewpoint shows a single `View options` row when you look at an interactable object. It only builds the full interaction menu after that row is selected, reducing the work done while scanning nearby objects. This behavior can be disabled in the mod options.

Nearby Viewpoint interaction targets can be cycled with the Left and Right arrow keys or Alt+mouse wheel by default. The scroll modifier and arrow keys can be changed in the mod options.

Container contents are collapsed into a separate submenu for each container by default, keeping long inventories out of the main interaction list. This behavior can be disabled in the mod options.

Viewpoint interaction rows retain icons attached to the original vanilla context-menu actions, including item-specific textures supplied through `itemForTexture` in nested actions. When Context Menu Icons Core and an icon pack are active, matching pack icons are also used as a fallback; the core mod is optional.

Vehicle camera behavior is a single selection: disabled, force third person in the vehicle and return to first person on exit, or preserve the view used before entering both in and out of the vehicle.

When Project A-Life is active, NPC speech bubbles and the Answer prompt follow the NPC in Viewpoint's camera view. Viewpoint hides its interaction panel and makes the cursor available during conversations, then restores normal mouse control when the conversation closes. Talk and trade actions are limited to the NPC targeted by Viewpoint when multiple NPCs are nearby.

When Project A-Life and Companion Dogs are active, Friendly and Allied A-Life NPCs are excluded from the dog's threat scans; other stances and regular zombies remain detectable. Companion Dogs and A-Life NPC name tags are positioned using Viewpoint's camera projection.

When Project A-Life is active, its NPC interaction actions are exposed through Viewpoint's interaction menu, and aiming at an A-Life NPC displays that NPC's tag above them. These features are optional and do not affect Viewpoint interactions with other targets.

Dependencies:

- ZombieBuddy 2.3.0 or newer
- Viewpoint
