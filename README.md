# SkyGrid

SkyGrid is a challenging survival world for Minecraft, in the spirit of SkyBlock. Instead of a floating island, the whole world is an open grid of single blocks floating in the void. Every block is picked at random from the game's materials, and the blocks sit 4 blocks apart on every axis. There are no rules and no objective given to you. You decide what to build, what to farm, and how to survive.

Because the generation is deterministic, the same world seed always produces the same layout. Two players using the same seed get the exact same grid.

## What makes it a survival challenge

Everything you need has to be reached one block at a time. Ores, wood, water, and food are scattered across the grid, so early game is about bridging carefully between blocks without falling into the void. Chests are part of the grid and hold random loot. Some blocks are mob spawners, so exploring can be dangerous.

The world keeps vanilla survival intact. Crops grow, redstone works, fluids flow, and gravity behaves normally. Generated leaves are made persistent so they never decay on their own, but leaves you place or trees you cut behave the way they do in a normal world.

## Requirements

You need a Spigot or Paper server on version 26.2, running on Java 25. Paper is recommended for performance on this kind of world.

## Installation

1. Download the latest `SkyGrid-x.y.jar` from the releases, or build it yourself with `./gradlew build` and take the jar from `build/libs`.
2. Stop your server.
3. Drop the jar into the `plugins` folder of your server.
4. Start the server once so the plugin loads. You should see SkyGrid in the plugin list with `/plugins`.

## Setting SkyGrid as the world generator on Spigot

The plugin name used in the examples below is `SkyGrid`. It has to match the `name` field in the plugin's `plugin.yml`, so keep the same capitalization.

### As the default world, without Multiverse

This turns the main server world into a SkyGrid world using plain Spigot config. The main world is the base world of the server, so switching it over means starting it fresh.

1. Stop the server if it is running.
2. Delete the existing world if one is already there. The main world is named by `level-name` in `server.properties`, which is `world` by default, so remove the `world`, `world_nether`, and `world_the_end` folders. A world that already holds normal terrain will not turn into a grid on its own, so the old one has to be removed and generated again.
3. Open `bukkit.yml`, which sits in the root of your server next to `server.properties`, and attach the generator to that world name.

```yaml
worlds:
  world:
    generator: SkyGrid
```

4. Start the server. The world regenerates from scratch as a SkyGrid.

If you use a world name other than `world`, set the same name in both `server.properties` and `bukkit.yml` so the two agree.

```properties
level-name=skygrid
```

```yaml
worlds:
  skygrid:
    generator: SkyGrid
```

### As a separate world, with Multiverse

If your server uses Multiverse you can keep your normal main world and add a SkyGrid world next to it. Nothing needs to be stopped or deleted. The value after `-g` is the plugin name.

```
/mv create skygrid NORMAL -g SkyGrid
```

Once the command finishes, teleport into it.

```
/mv tp skygrid
```

## Notes

The world is meant to be played in survival. Creative and other modes work, but the design assumes you are bridging and gathering by hand.

Performance is tuned for this style of world. Blocks that would tick constantly, such as furnaces and hoppers, are kept out of the grid and handed to you as chest loot instead, so a busy grid stays smooth.