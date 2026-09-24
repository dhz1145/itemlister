package top.chaodiao.itemlister.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

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
		List<String> itemIds = collectRegisteredIds(BuiltInRegistries.ITEM);
		List<String> blockIds = collectRegisteredIds(BuiltInRegistries.BLOCK);
		List<String> blockEntityIds = collectRegisteredIds(BuiltInRegistries.BLOCK_ENTITY_TYPE);
		List<String> entityIds = collectRegisteredIds(BuiltInRegistries.ENTITY_TYPE);
		List<String> fluidIds = collectRegisteredIds(BuiltInRegistries.FLUID);
		Path outputDirectory = client.gameDirectory.toPath().resolve("itemlist");
		Files.createDirectories(outputDirectory);

		String timestamp = FILE_NAME_TIME_FORMAT.format(OffsetDateTime.now());
		String json = createJson(itemIds, blockIds, blockEntityIds, entityIds, fluidIds);
		Path outputFile = writeNewFile(outputDirectory, timestamp, json);
		return new ExportResult(outputFile, itemIds.size(), blockIds.size(), blockEntityIds.size(),
				entityIds.size(), fluidIds.size());
	}

	private static <T> List<String> collectRegisteredIds(Registry<T> registry) {
		Set<String> ids = new TreeSet<>();

		for (Identifier id : registry.keySet()) {
			ids.add(id.toString());
		}

		return new ArrayList<>(ids);
	}

	private static String createJson(List<String> itemIds, List<String> blockIds,
			List<String> blockEntityIds, List<String> entityIds, List<String> fluidIds) {
		JsonObject root = new JsonObject();
		root.addProperty("format", "itemlister/2");
		root.addProperty("minecraftVersion", SharedConstants.getCurrentVersion().name());
		root.addProperty("generatedAt", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		root.addProperty("itemCount", itemIds.size());
		root.addProperty("blockCount", blockIds.size());
		root.addProperty("blockEntityCount", blockEntityIds.size());
		root.addProperty("entityCount", entityIds.size());
		root.addProperty("fluidCount", fluidIds.size());
		root.add("items", toJsonArray(itemIds));
		root.add("blocks", toJsonArray(blockIds));
		root.add("blockEntities", toJsonArray(blockEntityIds));
		root.add("entities", toJsonArray(entityIds));
		root.add("fluids", toJsonArray(fluidIds));
		return GSON.toJson(root) + System.lineSeparator();
	}

	private static JsonArray toJsonArray(List<String> ids) {
		JsonArray array = new JsonArray();

		for (String id : ids) {
			array.add(id);
		}

		return array;
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

	public record ExportResult(Path outputFile, int itemCount, int blockCount,
			int blockEntityCount, int entityCount, int fluidCount) {
	}
}
