package top.chaodiao.itemlister.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import top.chaodiao.itemlister.Itemlister;

import java.io.IOException;
import java.nio.file.Path;

public class ItemlisterClient implements ClientModInitializer {
	private static KeyMapping EXPORT_KEY;

	@Override
	public void onInitializeClient() {
		EXPORT_KEY = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(
						"key.itemlister.export",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_UNKNOWN,
						KeyMapping.Category.register(
								Identifier.fromNamespaceAndPath(Itemlister.MOD_ID, "itemlister")
						)
				)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (EXPORT_KEY.consumeClick()) {
				exportItemIds(client);
			}
		});
	}

	private static void exportItemIds(Minecraft client) {
		if (client.player == null || client.level == null) {
			Itemlister.LOGGER.warn("Skipped item export because no world is currently loaded.");
			return;
		}

		try {
			CreativeItemExporter.ExportResult result = CreativeItemExporter.export(client);
			Path relativePath = client.gameDirectory.toPath().relativize(result.outputFile());
			client.player.sendSystemMessage(
					Component.literal("已导出 " + result.itemCount() + " 个物品 ID、"
							+ result.blockCount() + " 个方块 ID、"
							+ result.blockEntityCount() + " 个方块实体类型和 "
							+ result.entityCount() + " 个实体 ID 到 " + relativePath)
			);
			Itemlister.LOGGER.info("Exported {} item IDs, {} block IDs, {} block entity types and {} entity IDs to {}",
					result.itemCount(), result.blockCount(), result.blockEntityCount(), result.entityCount(),
					result.outputFile());
		} catch (IOException | RuntimeException exception) {
			client.player.sendSystemMessage(Component.literal("导出物品 ID 失败，请查看日志。"));
			Itemlister.LOGGER.error("Failed to export item IDs.", exception);
		}
	}
}
