package top.chaodiao.itemlister.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import top.chaodiao.itemlister.Itemlister;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class CreativeItemExporter {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final DateTimeFormatter FILE_NAME_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS");
	private static final int MAX_FILE_NAME_ATTEMPTS = 1000;

	private CreativeItemExporter() {
	}

	public static ExportResult export(Minecraft client) throws IOException {
		List<String> itemIds = collectCreativeItemIds();
		Path outputDirectory = client.gameDirectory.toPath().resolve("itemlist");
		Files.createDirectories(outputDirectory);

		String timestamp = FILE_NAME_TIME_FORMAT.format(OffsetDateTime.now());
		String json = createJson(itemIds);
		Path outputFile = writeNewFile(outputDirectory, timestamp, json);
		return new ExportResult(outputFile, itemIds.size());
	}

	private static List<String> collectCreativeItemIds() {
		Set<String> itemIds = new TreeSet<>();

		for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
			for (ItemStack stack : tab.getDisplayItems()) {
				if (stack.isEmpty()) {
					continue;
				}

				ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
				if (itemId != null) {
					itemIds.add(itemId.toString());
				}
			}
		}

		return new ArrayList<>(itemIds);
	}

	private static String createJson(List<String> itemIds) {
		JsonObject root = new JsonObject();
		root.addProperty("format", "itemlister/1");
		root.addProperty("minecraftVersion", Minecraft.getInstance().getLaunchedVersion());
		root.addProperty("generatedAt", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		root.addProperty("itemCount", itemIds.size());

		JsonArray items = new JsonArray();
		for (String itemId : itemIds) {
			items.add(itemId);
		}
		root.add("items", items);
		return GSON.toJson(root) + System.lineSeparator();
	}

	private static Path writeNewFile(Path outputDirectory, String timestamp, String json) throws IOException {
		for (int attempt = 0; attempt < MAX_FILE_NAME_ATTEMPTS; attempt++) {
			String suffix = attempt == 0 ? "" : "-" + attempt;
			Path outputFile = outputDirectory.resolve(timestamp + suffix + ".json");
			try {
				Files.writeString(outputFile, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
				return outputFile;
			} catch (FileAlreadyExistsException ignored) {
				// Use a numbered suffix rather than replacing a previous export.
			}
		}

		throw new IOException("Could not create a unique export file in " + outputDirectory);
	}

	public record ExportResult(Path outputFile, int itemCount) {
	}
}
