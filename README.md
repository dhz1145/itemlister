# ItemLister

A client-side Fabric mod for Minecraft Java Edition **1.21.11** that exports the item IDs currently available through the creative inventory.

## Usage

1. Install the mod and its required Fabric API version.
2. Enter a single-player or multiplayer world.
3. Open **Options → Controls** and bind **Export creative item IDs** under the **ItemLister** category. The key is unbound by default.
4. Press the bound key. ItemLister writes a JSON file to `<game directory>/itemlist/` and reports the result in chat.

Each file uses a timestamped name such as `2026-09-05_12-34-56_789.json`. Existing exports are never overwritten.

## Export contents

The `items` array contains unique item registry IDs, sorted lexicographically:

```json
{
  "format": "itemlister/1",
  "minecraftVersion": "1.21.11",
  "generatedAt": "2026-09-05T12:34:56+08:00",
  "itemCount": 2,
  "items": [
    "examplemod:custom_block",
    "minecraft:stone"
  ]
}
```

ItemLister reads all current creative-mode tabs, so it includes items and block items added by other loaded mods when they are visible in the creative inventory. It intentionally exports each item ID only once: stack counts, NBT, enchantments, durability, and other creative-menu variants are not included.

The export key only works after a world has loaded. This ensures the creative-tab contents reflect the current client and feature configuration. After creating a new world, wait briefly for it to finish loading before exporting so the creative inventory can be traversed successfully.

## Notice

I only wrote part of the code and the overall framework; the rest was completed by Claude.

## Development

Build with Java 21:

```sh
./gradlew build
```

Launch a development client:

```sh
./gradlew runClient
```

## License

This project is available under the CC0 license.
