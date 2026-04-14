# AutoClutch
Automatic water bucket clutching for Fabric 1.21.11 with human-like timing!

## Features

- **Automatic Water Bucket Clutching**: Automatically places water buckets when falling to prevent death
- **Human-Like Timing**: Uses truncated normal distribution for realistic, varied timing
- **Anticheat Safe**: Uses vanilla Minecraft interaction system - indistinguishable from manual clicks
- **Configurable**: Adjust mean distance, variance, and enable/disable the mod
- **Toggle Keybind**: Press 'B' (configurable) to toggle the mod on/off in-game

## How It Works

The mod detects when you're falling and automatically places a water bucket at the optimal distance from the ground. The timing uses a truncated normal distribution to mimic human reaction time variations, making it appear natural and avoiding anticheat detection.

### Key Features for Anticheat Safety:
- **No packet manipulation**: Uses `gameMode.useItem()` which is identical to vanilla right-clicks
- **No position teleportation**: Only automates timing, not movement
- **Human-like variance**: Configurable randomness in placement timing
- **Bounds checking**: Only triggers within the safe 1.5-4.5 block window

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) 0.141.3+1.21.11 or later
3. Download AutoClutch JAR from releases
4. Place the JAR file in your `.minecraft/mods` folder
5. Launch Minecraft with Fabric

## Configuration

The mod creates a configuration file at `config/autoclutch.json` with the following settings:

```json
{
  "enabled": true,
  "meanBlocks": 2.5,
  "varianceBlocks": 1.2
}
```

### Configuration Options:

- **enabled** (boolean): Enable or disable the mod
  - Default: `true`
  - Can also be toggled with the 'B' key in-game

- **meanBlocks** (double): Average distance above ground (in blocks) when water is placed
  - Range: 1.5 - 4.5 blocks
  - Default: `2.5`
  - Lower values = later placement (riskier but looks more skilled)
  - Higher values = earlier placement (safer but less optimal)

- **varianceBlocks** (double): Standard deviation of placement timing (in blocks)
  - Range: 0.5 - 2.5 blocks
  - Default: `1.2`
  - Lower values = more consistent timing
  - Higher values = more human-like variation


## Keybinds

- **Toggle AutoClutch**: `B` (configurable in Minecraft's controls menu under "Miscellaneous")

When toggled, the mod displays a message in the action bar showing whether it's enabled or disabled.

## Usage

1. Make sure you have a water bucket in your main hand or offhand
2. Fall from a height
3. The mod will automatically place the water bucket when you reach the configured distance from the ground
4. Press 'B' to toggle the mod on/off at any time

## Technical Details

### How the Timing Works

The mod uses a **truncated normal distribution** bounded between 1.5 and 4.5 blocks:
- **1.5 blocks minimum**: Any later and you'll take damage or die
- **4.5 blocks maximum**: Any earlier and the water may despawn before you land

The distribution naturally clusters around your configured mean, with the tails cut off at the safety bounds. This creates realistic human-like variation while guaranteeing clutches within the safe window.

### Anticheat Considerations

This mod is designed to be undetectable by anticheats:

1. **Packet-identical to manual clicks**: Uses `MultiPlayerGameMode.useItem()` which sends the exact same `ServerboundUseItemPacket` as a real right-click
2. **No inhuman precision**: Random timing variance prevents pixel-perfect consistency
3. **Inventory checking**: Only activates when you're actually holding a water bucket
4. **No position manipulation**: The mod never touches your player position
5. **Natural variation**: Configurable mean and variance allow you to tune "skill level"

### Algorithm

1. Detect falling (fall distance > 3.0, not on ground)
2. On fall start, sample a target distance from truncated normal distribution
3. Each tick, raycast downward to measure distance to ground
4. When distance ≤ target distance, use the item.
5. Reset state when landing or fall ends

## Building from Source

Requirements:
- JDK 21
- Gradle 9.3+

```bash
./gradlew build
```

The built JAR will be in `build/libs/`.


## Support

For issues, feature requests, or questions, please visit the [GitHub repository](https://github.com/HoneyBerries/AutoClutch).

