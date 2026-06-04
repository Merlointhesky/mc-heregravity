# Plugin Specification: "Selective Gravity"

## 1. Overview
**Target Platform:** Minecraft Java Edition (Paper API 26.1.2+)
**Primary Objective:** Eliminate floating trees and add dynamic, localized terrain hazards without utilizing continuous chunk-scanning or causing cascading server TPS lag. 
**Core Methodology:** Apply vanilla "falling block" (sand) physics to a highly curated whitelist of blocks, triggered strictly by targeted `BlockUpdate` events.

---

## 2. Core Engine Mechanics

### 2.1 The "Bamboo" Wood System
Instead of complex recursive algorithms or top-down tree drops, raw logs will inherit sand physics.
* **Trigger:** When a player breaks a log, the log directly above it receives a block update.
* **Action:** Recognizing it has air beneath it, the above log converts to a falling entity, dropping into the newly vacated space.
* **Result:** Trees do not fall as a single massive entity. Instead, the trunk feeds downward block-by-block as the player chops the bottom, mimicking vanilla bamboo or sugar cane.

### 2.2 The "Shatter" Canopy System (Fast Decay)
Turning leaves into falling entities causes severe entity lag. Leaves must be handled separately from logs.
* **Trigger:** The movement of the log (converting to a falling block) severs the connection to the adjacent leaves.
* **Action:** The server hooks into the vanilla leaf `distance` property. When leaves detect they are no longer supported by a log (distance > 7), they bypass the vanilla decay timer.
* **Result:** Leaves instantly "explode" (break), immediately dropping saplings, apples, and sticks.

---

## 3. Block Whitelists & Environmental Effects

Applying gravity to structural foundations (Stone, Deepslate, Netherrack, End Stone) will cause catastrophic chunk collapses. Gravity is **strictly limited** to the following blocks to create localized, biome-specific hazards.

### 3.1 The Overworld
* **Logs / Stems:** Trees feed downwards.
* **Dirt / Coarse Dirt / Rooted Dirt:** Ravine edges and cave entrances become unstable.
* **Terracotta / Clay:** Badlands (Mesa) biomes become highly dangerous avalanche zones.
* **Snow Blocks / Powder Snow:** Explosions or mining on mountains trigger snowslides.
* **Mud / Mangrove Roots:** Swamp terrain can act as sinkholes.
* **Ice / Packed Ice / Blue Ice:** Ice spikes and icebergs can crash down if the base is destroyed.
* **Hay Bales / Pumpkins / Melons:** Agricultural items obey gravity (prevents floating farm pixel-art).

### 3.2 The Nether
* **Soul Sand / Soul Soil:** Suspended valleys over lava lakes become quicksand traps if destabilized.
* **Glowstone:** Mining or shooting ceiling clusters causes the entire heavy cluster to detach and crash to the floor.
* **Magma Blocks:** Underwater or underground magma veins shift and slide when mined.
* **Crimson/Warped Stems & Wart Blocks:** Inherit the Overworld wood/leaf mechanics.

### 3.3 The Exclusions (CRITICAL)
The following must **NEVER** have gravity applied:
* **All Crafted Woods:** Planks, Slabs, Stairs, Fences (Players must be able to build roofs and bridges).
* **All Foundations:** Stone, Cobblestone, Deepslate, Netherrack, Basalt, Blackstone, End Stone.

---

## 4. Implementation Guidelines & Event Listeners

To maintain server TPS, this plugin must remain completely event-driven. Do not use repeating tasks or continuous scanning.

1. **`BlockPhysicsEvent` / `BlockUpdateEvent`:** Listen for these vanilla events. If the updated block is in the Whitelist, and the block below it is `AIR`, schedule a task to convert the block to a `FallingBlock` entity.
2. **`LeavesDecayEvent`:** Intercept this event. If triggered, cancel the natural slow decay and instantly `block.breakNaturally()` to drop items.
3. **`EntityChangeBlockEvent`:** Handle the moment the `FallingBlock` lands and solidifies to ensure it triggers the *next* block update for the block above it.