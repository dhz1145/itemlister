# ItemLister

A client-side Fabric mod for Minecraft Java Edition **26.2** that exports all registered item, block, block entity type, entity type and fluid IDs to a JSON file.

## Usage

1. Install the mod and its required Fabric API version.
2. Enter a single-player or multiplayer world.
3. Open **Options → Controls** and bind **Export creative item IDs** under the **ItemLister** category. The key is unbound by default.
4. Press the bound key. ItemLister writes a JSON file to `<game directory>/itemlist/` and reports the result in chat.

Each file uses a timestamped name such as `2026-09-05_12-34-56_789.json`. Existing exports are never overwritten.

## Export contents

The `items`, `blocks`, `blockEntities`, `entities` and `fluids` arrays contain unique registry IDs, sorted lexicographically:

```json
{
  "format": "itemlister/2",
  "minecraftVersion": "26.2",
  "generatedAt": "2026-09-05T12:34:56+08:00",
  "itemCount": 2,
  "blockCount": 2,
  "blockEntityCount": 2,
  "entityCount": 2,
  "fluidCount": 2,
  "items": [
    "minecraft:cauldron",
    "minecraft:stone"
  ],
  "blocks": [
    "minecraft:cauldron",
    "minecraft:water_cauldron"
  ],
  "blockEntities": [
    "minecraft:chest",
    "minecraft:furnace"
  ],
  "entities": [
    "minecraft:pig",
    "minecraft:zombie"
  ],
  "fluids": [
    "minecraft:empty",
    "minecraft:water"
  ]
}
```

ItemLister reads the item, block, block entity type, entity type and fluid registries directly, so it includes everything registered by the vanilla game and by other loaded mods. Because the registries are independent, an ID can appear in more than one array, and entries without a corresponding item (such as `minecraft:water_cauldron`) appear only in their own array.

Each ID is exported only once: stack counts, NBT, enchantments, durability, and other variants are not included.

The export key only works after a world has loaded. If no IDs are detected, open your inventory once and try exporting again.

## Notice

I only wrote part of the code and the overall framework; the rest was completed by Claude.

## Development

Build with Java 25:

```sh
./gradlew build
```

Launch a development client:

```sh
./gradlew runClient
```

## License

This project is available under the CC0 license.
