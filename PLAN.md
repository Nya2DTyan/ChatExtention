# Multi-Module Restructure Plan

## Approach
No heavy interfaces. `:common` holds only classes with **zero Minecraft imports**.  
Version modules (`:v26_1`, later `:v1_21`) contain all MC-dependent code.

## Structure
```
ChatExtention/
├── settings.gradle              # include 'common', 'v26_1'
├── build.gradle                 # Root — applies Java plugin to subprojects
├── gradle.properties            # Version properties per module
├── common/                      # Pure Java + Fabric Loader API
│   ├── build.gradle             # java-library, no Loom
│   └── src/main/java/com/chatexpansion/
│       ├── ChatExpansion.java   # ModInitializer (unchanged)
│       ├── chat/ChatTab.java    # Pure Java POJO (unchanged)
│       └── config/ModConfig.java # Refactored: no FabricLoader dep
├── v26_1/                       # Minecraft 26.1 (current)
│   ├── build.gradle             # Fabric Loom, depends :common
│   ├── src/main/resources/fabric.mod.json
│   ├── src/client/java/com/chatexpansion/client/
│   │   ├── ChatExpansionClient.java
│   │   ├── chat/{ChatTabManager,ChatTabBar,FloatingChatWindow}.java
│   │   └── mixin/*.java
│   └── src/client/resources/chatexpansion.client.mixins.json
```

## Changes

### 1. ModConfig.java — remove FabricLoader
- `load(Path configPath)` takes explicit path  
- `save(ModConfig config, Path configPath)` takes explicit path
- Migration methods don't auto-save; caller handles save

### 2. ChatTabManager.java — use new ModConfig API
- `init()` calls `ModConfig.load(configPath)`  
- `setActiveTab()` calls `ModConfig.save(config, configPath)`

### 3. ChatExpansionClient.java — resolve path
- `Path configDir = FabricLoader.getInstance().getConfigDir()`
- `ModConfig.load(configDir.resolve("chatexpansion.json"))`

### 4. Build files
- Root `settings.gradle`: add `include 'common'`, `include 'v26_1'`
- Root `build.gradle`: minimal — applies Java plugin
- `common/build.gradle`: `java-library`, depends `fabric-loader` only
- `v26_1/build.gradle`: Fabric Loom, `implementation project(':common')`, version-specific deps

### 5. gradle.properties
- `minecraft_version` → `minecraft_version_26_1`
- `fabric_api_version` → `fabric_api_version_26_1`
