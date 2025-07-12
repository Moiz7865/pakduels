# PakDuels

Advanced duels plugin for Minecraft 1.21.4 with comprehensive arena management, kit system, and FAWE integration.

## Features

### Core Duel System
- **Interactive Duel Requests**: Players receive clickable accept/deny buttons
- **Multi-round Duels**: Support for 1-10 rounds per duel
- **Kit-based Combat**: Customizable kits with rule enforcement
- **Real-time Scoreboards**: Live duel statistics and progress tracking

### Arena Management
- **Arena Creation**: Define arena boundaries with corner positions
- **Spawn Points**: Set dual spawn points for fair starts
- **Kit Restrictions**: Control which kits can be used in specific arenas
- **Center-based Cloning**: Advanced arena duplication system
- **FAWE Integration**: Automatic schematic generation and regeneration

### Arena Cloning System

#### Approach Used
The arena cloning system uses a **center-based positional mathematics approach** with the following methodology:

1. **Center Point Storage**: Each arena stores a center location that serves as the reference point for all calculations
2. **Offset Calculation**: When cloning, we calculate the offset of each arena component (corners, spawns) relative to the original center
3. **Position Recalculation**: Apply the same offsets to the new center location to maintain structural integrity
4. **Schematic Management**: FAWE handles the actual block copying asynchronously

#### Implementation Details

**Mathematical Approach:**
```java
// Calculate offsets from original center
Location pos1Offset = arena.getPosition1().clone().subtract(originalCenter);
Location pos2Offset = arena.getPosition2().clone().subtract(originalCenter);
Location spawn1Offset = arena.getSpawnPoint1().clone().subtract(originalCenter);
Location spawn2Offset = arena.getSpawnPoint2().clone().subtract(originalCenter);

// Apply offsets to new center
Location newPos1 = newCenter.clone().add(pos1Offset);
Location newPos2 = newCenter.clone().add(pos2Offset);
Location newSpawn1 = newCenter.clone().add(spawn1Offset);
Location newSpawn2 = newCenter.clone().add(spawn2Offset);
```

**Key Methods:**

1. **ArenaCloneManager.saveArenaSchematic(Arena arena)**
   - Creates a schematic file from arena boundaries
   - Uses FAWE's EditSession for efficient copying
   - Saves to `/plugins/PakDuels/schematics/`

2. **ArenaCloneManager.cloneArenaSchematic(Arena original, Arena cloned, Player player)**
   - Loads the original arena's schematic
   - Pastes it at the new center location
   - Handles async operations to prevent server lag

3. **ArenaCloneManager.regenerateArena(Arena arena)**
   - Restores arena to original state using saved schematic
   - Used during duel rounds for consistent gameplay

**FAWE Integration:**
- **Async Operations**: All schematic operations run asynchronously
- **EditSession Management**: Proper session lifecycle management
- **Clipboard System**: Uses WorldEdit's clipboard for reliable copying
- **Error Handling**: Comprehensive error handling with user feedback

#### Usage Workflow

1. **Set Center**: Click "Set Center" in arena editor to save current location
2. **Clone Arena**: Click "Clone Arena" to duplicate at your current position
3. **Automatic Processing**: 
   - Calculates new positions using offset mathematics
   - Copies all arena settings (allowed kits, regeneration, etc.)
   - Creates and saves schematic if not exists
   - Pastes schematic at new location
4. **Result**: Fully functional arena copy with preserved spatial relationships

### Kit System
- **Custom Loadouts**: Create kits from player inventory
- **Rule Enforcement**: Control game mechanics per kit
- **Visual Editor**: GUI-based kit rule configuration

### Health & Scoreboard System
- **Conditional Health Display**: Shows health indicators only during active duels when kit allows
- **Dynamic Scoreboards**: Real-time updates with duel progress
- **Clean State Management**: Proper cleanup when duels end

## Commands

### Player Commands
- `/duel <player> <kit> <rounds>` - Challenge another player
- `/duel accept` - Accept pending duel request
- `/duel deny` - Deny pending duel request

### Admin Commands
- `/pakmc create <kitname>` - Create kit from inventory
- `/pakmc arena create <name>` - Create new arena
- `/pakmc arena editor` - Open arena management GUI
- `/pakmc kit editor <kitname>` - Edit kit rules
- `/pakmc setspawn` - Set lobby spawn location
- `/pakmc reload` - Reload configurations

## Permissions

- `pakmc.duel.use` - Allow dueling (default: true)
- `pakmc.kit.create` - Create and edit kits (default: op)
- `pakmc.arena.create` - Create arenas (default: op)
- `pakmc.arena.edit` - Edit arenas (default: op)
- `pakmc.admin` - Full admin access (default: op)

## Configuration

### Main Config (`config.yml`)
```yaml
duel:
  inventory-countdown-time: 10  # Seconds for inventory organization
  round-delay: 2               # Delay between rounds
  regeneration:
    use-fawe: true            # Enable FAWE integration
    delay: 60                 # Regeneration delay in ticks
```

### Messages (`messages.yml`)
Fully customizable messages with color code support and placeholders.

### Scoreboard (`scoreboard.yml`)
Configurable scoreboard layout with dynamic placeholders:
- `{player1}`, `{player2}` - Player names
- `{score1}`, `{score2}` - Current scores
- `{kit}` - Kit name
- `{round}`, `{maxrounds}` - Round information
- `{state}` - Current duel state

## Technical Implementation

### Architecture
- **Manager Pattern**: Separate managers for different systems
- **Event-Driven**: Bukkit event system for game mechanics
- **Async Processing**: FAWE operations run asynchronously
- **State Management**: Comprehensive duel state tracking

### Dependencies
- **Paper API 1.21.4**: Core server functionality
- **FastAsyncWorldEdit**: Schematic and regeneration system
- **FastBoard**: Scoreboard management
- **Adventure API**: Modern text components and interactions

### File Structure
```
plugins/PakDuels/
├── config.yml
├── messages.yml
├── scoreboard.yml
├── arenas/           # Arena configuration files
├── kits/             # Kit configuration files
└── schematics/       # Arena schematic files
```

## Installation

1. Install FastAsyncWorldEdit on your server
2. Download PakDuels plugin
3. Place in `plugins/` folder
4. Restart server
5. Configure as needed

## Support

For issues, feature requests, or contributions, please visit the project repository.