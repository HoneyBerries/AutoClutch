# AutoClutch

[![Latest Release](https://img.shields.io/github/v/release/HoneyBerries/AutoClutch?style=flat-square&color=brightgreen)](https://github.com/HoneyBerries/AutoClutch/releases)
[![License](https://img.shields.io/github/license/HoneyBerries/AutoClutch?style=flat-square)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2+-blue?style=flat-square)](https://www.minecraft.net/)
[![Fabric API](https://img.shields.io/badge/Fabric%20API-0.148.0+-blue?style=flat-square)](https://modrinth.com/mod/fabric-api)
[![Build Status](https://img.shields.io/github/actions/workflow/status/HoneyBerries/AutoClutch/build.yml?style=flat-square)](https://github.com/HoneyBerries/AutoClutch/actions)

Automatic water bucket clutching for Fabric with human-like timing!

## Features

- **Automatic Water Bucket Clutching**: Automatically places water buckets when falling to prevent death
- **Reach-Distance Trigger**: Places water as soon as the ground is within Minecraft's default survival block reach distance (4.5 blocks)
- **Anticheat Safe**: Uses vanilla Minecraft interaction system—indistinguishable from manual clicks
- **Configurable**: Enable/disable the mod and toggle water bucket clutching
- **Toggle Keybind**: Press a configurable keybind to toggle the mod on/off in-game

## How It Works

The mod detects when you're falling and automatically places a water bucket once the ground is within 4.5 blocks—Minecraft's default survival block interaction range—so the placement lands the moment a real right-click could reach it.

### Key Features for Anticheat Safety:
- **No packet manipulation**: Uses `gameMode.useItem()` which is identical to vanilla right-clicks
- **No position teleportation**: Only automates timing, not movement
- **Reach-accurate trigger**: Only fires within the same 4.5-block range a real player could interact in

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.1.2
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) 0.148.0+ or later
3. Download AutoClutch JAR from [GitHub Releases](https://github.com/HoneyBerries/AutoClutch/releases)
4. Place the JAR file in your `.minecraft/mods` folder
5. Launch Minecraft with Fabric

## Configuration

The mod creates a configuration file at `config/autoclutch.json` with the following default settings:

```json
{
  "enabled": true,
  "enableWater": true
}
```

### Configuration Options

| Option | Type | Range | Default | Description |
|--------|------|-------|---------|-------------|
| `enabled` | boolean | — | `true` | Enable or disable the mod. Can also be toggled with the 'B' key in-game |
| `enableWater` | boolean | — | `true` | Enable or disable automatic water bucket placement |

The trigger distance itself (4.5 blocks, Minecraft's default survival block reach) is fixed and not configurable.


## Usage

1. Ensure you have a water bucket in your main hand or offhand
2. Fall from a height
3. The mod will automatically place the water bucket once the ground is within reach distance

## Technical Details

### Timing Algorithm

The mod triggers placement at a fixed **4.5 blocks**—Minecraft's default survival block interaction range—so it fires exactly when a real right-click would first be able to reach the ground:

The algorithm flow:
1. Detect falling (fall distance > 3.0, not on ground)
2. Each tick, raycast downward to measure distance to ground
3. When distance ≤ 4.5 blocks, place the water bucket
4. Reset state when landing or fall ends

### Anticheat Safety

This mod is designed to be indistinguishable from manual water bucket clutching:

- **Packet-identical**: Uses `MultiPlayerGameMode.useItem()`, sending the exact same `ServerboundUseItemPacket` as a real right-click
- **No position manipulation**: Never alters your player position or movement
- **Inventory checking**: Only activates when holding a water bucket
- **Reach-accurate trigger**: Fires only within the same distance a real player could interact in, so it can't place water further away than a human ever could

## Building from Source

### Requirements
- JDK 25+
- Gradle (uses bundled Gradle wrapper v9.5.0)

### Build Command

```bash
./gradlew build
```

The compiled JAR will be located in `build/libs/`.

## Contributing & Support

Found a bug or have a feature request? Visit the [GitHub Issues](https://github.com/HoneyBerries/AutoClutch/issues) page to report or discuss.

