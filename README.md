# CustomWeapons

A Paper plugin that adds custom admin weapons to Minecraft. Built for Paper API 1.21.11 (compiles with Java 17).

## Weapons

| Weapon | Base | Behavior |
| --- | --- | --- |
| **Stun Crossbow** | Crossbow | Hitting a target 15 times stuns it for 4 seconds, while an iron-chain orbit effect plays. |
| **Black Knife** | Netherite Sword | Deals 14 normal damage on a fully charged hit. Cannot be enchanted or thrown. Right-click casts **Wraith Slash**, a 5-heart true-damage arc that ignores armor and protection (360s cooldown). |
| **Siphon Trident** | Trident | Deals 13 damage and is un-throwable. While held at 70%+ swing charge, durability lost by other players repairs the holder's armor. Also gives a sword attack speed. |

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/weapongive <player> <weapon>` | `weapons.give` (op) | Give a custom weapon to a player. |
| `/recipegui [player]` | `weapons.recipe` (op) | Open a GUI to assign a crafting recipe to any item by dragging ingredients into a 3x3 grid. |

## Building

Requires JDK 17+ and Maven:

```bash
mvn package
```

The plugin jar is written to `target/custom-weapons-1.0.0.jar`. Drop it into your Paper server's `plugins/` folder.

## Recipe assignment

`/recipegui` opens an "Assign Recipe" inventory:

- Drag/place items into the 3x3 grid and set the result slot.
- Click the green **Save Recipe** button to register a shaped recipe (exact-item matching, up to 26 distinct ingredients) and persist it to `plugins/CustomWeapons/recipes.yml`.
- Saved recipes are re-registered automatically when the server starts.
- Click the red **Clear Grid** button to empty the working grid; closing the GUI returns leftover items to the player.
