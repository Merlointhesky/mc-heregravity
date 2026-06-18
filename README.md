# HereGravity

A premium, highly-optimized [Paper](https://papermc.io) Minecraft plugin for **selective block-gravity physics** and **instant canopy leaf shattering**! Add custom weight-based physics to logs, terrain, and structures automatically, while enjoying ultra-fast leaf decay.

---

## Features

- **Selective Config-Driven Gravity**:
  - Apply sand/gravel-like falling block physics to any block in Minecraft.
  - Fully supports both **standard material types** (e.g., `TERRACOTTA`, `CLAY`) and native **Minecraft Tags** (e.g., `#logs`, `#dirt`, `#leaves`).
  
- **Dynamic "Shatter" Canopy System**:
  - Tired of floating leaves hanging in the air forever when harvesting trees?
  - Automatically intercepts block physics updates and triggers natural, instant leaf shattering for any leaves with a distance greater than 6 blocks from a tree trunk (unless placed persistently by players!).

- **"Breaks-on-Fall" Landing Interceptor**:
  - Configure specific gravity-affected blocks to shatter on landing instead of placing themselves.
  - Cancel block placement upon impact and drop the block immediately as a vanilla collectible item on the ground (ideal for immersive harvesting or custom block drops).

- **Completely Background & Seamless**:
  - Requires zero active player commands or complex permission setups.
  - Processes block falls asynchronously and efficiently, keeping server tick rates buttery smooth.

---

## How to Configure

All configurations are handled inside the standard [config.yml](src/main/resources/config.yml) file.

### Default Configuration
```yaml
# HereGravity Configuration
# Use standard block names (e.g., TERRACOTTA, CLAY) 
# Or use Minecraft tags by adding a '#' (e.g., '#logs', '#dirt', '#leaves')

gravity-list:
  '#logs':
    breaks-on-fall: true
  '#dirt':
    breaks-on-fall: false
  TERRACOTTA:
    breaks-on-fall: false
  CLAY:
    breaks-on-fall: true
  SNOW_BLOCK:
    breaks-on-fall: true
  GLOWSTONE:
    breaks-on-fall: true
```

### Configuration Options
* **`breaks-on-fall`** (`true` / `false`):
  * If `true`, the falling block will immediately break and drop as an item stack when it hits the ground.
  * If `false`, the falling block will land normally and solidifies back into a placed block.

---

## Building from Source

Ensure you have Java 21 and Gradle configured. Build using the provided Gradle wrapper:
```bash
./gradlew build
```

The compiled, deployment-ready JAR will be located at:
`build/libs/HereGravity-1.0.1.jar`

---

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
