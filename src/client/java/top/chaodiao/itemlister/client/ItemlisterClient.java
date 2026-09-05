package top.chaodiao.itemlister.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import top.chaodiao.itemlister.Itemlister;

import java.io.IOException;
import java.nio.file.Path;

public class ItemlisterClient implements ClientModInitializer {
	private static final KeyMapping EXPORT_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.itemlister.export",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			Category.register(Identifier.fromNamespaceAndPath(Itemlister.MOD_ID, "itemlister"))
	));

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (EXPORT_KEY.consumeClick()) {
				exportCreativeItemIds(client);
			}
		});
	}

	private static void exportCreativeItemIds(Minecraft client) {
		if (client.player == null || client.level == null) {
			Itemlister.LOGGER.warn("Skipped item export because no world is currently loaded.");
			return;
		}

		try {
			CreativeItemExporter.ExportResult result = CreativeItemExporter.export(client);
			Path relativePath = client.gameDirectory.toPath().relativize(result.outputFile());
			client.player.displayClientMessage(
					Component.literal("已导出 " + result.itemCount() + " 个物品 ID 到 " + relativePath),
					false
			);
			Itemlister.LOGGER.info("Exported {} creative item IDs to {}", result.itemCount(), result.outputFile());
		} catch (IOException | RuntimeException exception) {
			client.player.displayClientMessage(Component.literal("导出物品 ID 失败，请查看日志。"), false);
			Itemlister.LOGGER.error("Failed to export creative item IDs.", exception);
		}
	}
}
