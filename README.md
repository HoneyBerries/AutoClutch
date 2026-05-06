# AutoClutch

[![Latest Release](https://img.shields.io/github/v/release/HoneyBerries/AutoClutch?style=flat-square&color=brightgreen)](https://github.com/HoneyBerries/AutoClutch/releases)
[![License](https://img.shields.io/github/license/HoneyBerries/AutoClutch?style=flat-square)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2+-blue?style=flat-square)](https://www.minecraft.net/)
[![Fabric API](https://img.shields.io/badge/Fabric%20API-0.148.0+-blue?style=flat-square)](https://modrinth.com/mod/fabric-api)
[![Build Status](https://img.shields.io/github/actions/workflow/status/HoneyBerries/AutoClutch/build.yml?style=flat-square)](https://github.com/HoneyBerries/AutoClutch/actions)

Automatic water bucket clutching for Fabric with human-like timing!

## Features

- **Automatic Water Bucket Clutching**: Automatically places water buckets when falling to prevent death
- **Human-Like Timing**: Uses truncated normal distribution for realistic, varied timing
- **Anticheat Safe**: Uses vanilla Minecraft interaction system—indistinguishable from manual clicks
- **Configurable**: Adjust mean distance, variance, and enable/disable the mod
- **Toggle Keybind**: Press the 'B' key to toggle the mod on/off in-game

## How It Works

The mod detects when you're falling and automatically places a water bucket at the optimal distance from the ground. The timing uses a truncated normal distribution to mimic human reaction time variations, making it appear natural and avoiding anticheat detection.

### Key Features for Anticheat Safety:
- **No packet manipulation**: Uses `gameMode.useItem()` which is identical to vanilla right-clicks
- **No position teleportation**: Only automates timing, not movement
- **Human-like variance**: Configurable randomness in placement timing
- **Bounds checking**: Only triggers within the safe 1.5-4.5 block window

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
  "meanBlocks": 2.5,
  "varianceBlocks": 1.2
}
```

### Configuration Options

| Option | Type | Range | Default | Description |
|--------|------|-------|---------|-------------|
| `enabled` | boolean | — | `true` | Enable or disable the mod. Can also be toggled with the 'B' key in-game |
| `meanBlocks` | double | 1.5—4.5 | `2.5` | Average distance above ground (in blocks) when water is placed. Lower = riskier; Higher = safer |
| `varianceBlocks` | double | 0.5—2.5 | `1.2` | Standard deviation of placement timing. Lower = consistent; Higher = human-like variation |


## Usage

1. Ensure you have a water bucket in your main hand or offhand
2. Fall from a height
3. The mod will automatically place the water bucket at the configured distance from the ground

## Technical Details

### Timing Algorithm

The mod uses a **truncated normal distribution** bounded between 1.5 and 4.5 blocks to determine when to place the water bucket:

- **1.5 blocks minimum**: Safety threshold—any later risks taking damage
- **4.5 blocks maximum**: Optimal threshold—ensures water is placed before despawning
- **Distribution**: Clusters around your configured mean with natural human-like variation

The algorithm flow:
1. Detect falling (fall distance > 3.0, not on ground)
2. Sample a target distance from the truncated normal distribution
3. Each tick, raycast downward to measure distance to ground
4. When distance ≤ target distance, place the water bucket
5. Reset state when landing or fall ends

### Anticheat Safety

This mod is designed to be indistinguishable from manual water bucket clutching:

- **Packet-identical**: Uses `MultiPlayerGameMode.useItem()`, sending the exact same `ServerboundUseItemPacket` as a real right-click
- **No position manipulation**: Never alters your player position or movement
- **Inventory checking**: Only activates when holding a water bucket
- **Human-like variance**: Random timing variance prevents pixel-perfect consistency
- **Configurable skill level**: Adjust mean and variance to match your playstyle

## Building from Source

### Requirements
- JDK 21+
- Gradle 9.3+

### Build Command

```bash
./gradlew build
```

The compiled JAR will be located in `build/libs/`.

## Contributing & Support

Found a bug or have a feature request? Visit the [GitHub Issues](https://github.com/HoneyBerries/AutoClutch/issues) page to report or discuss.

