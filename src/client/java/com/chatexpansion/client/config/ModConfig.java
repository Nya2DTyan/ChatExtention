package com.chatexpansion.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.chatexpansion.ChatExpansion;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("chatexpansion.json");

	private static ModConfig instance;

	public List<TabConfig> tabs = new ArrayList<>();
	public List<WindowConfig> windows = new ArrayList<>();
	public String activeTabId = "main";

	public static class TabConfig {
		public String id;
		public String displayName;
		public List<String> patterns;
		public boolean visible = true;

		public TabConfig() {}

		public TabConfig(String id, String displayName, List<String> patterns) {
			this.id = id;
			this.displayName = displayName;
			this.patterns = patterns;
		}
	}

	public static class WindowConfig {
		public String tabId;
		public float x;
		public float y;
		public float width;
		public float height;
	}

	public static ModConfig createDefault() {
		ModConfig config = new ModConfig();

		config.tabs.add(new TabConfig("main", "Main", List.of(".*")));
		config.tabs.add(new TabConfig("pm", "PM", List.of(
			"(?i).*->.*",
			"(?i).*\\b(whispers to|msg|pm)\\b.*",
			"(?i).*\\[PM\\].*",
			"(?i).*\\[MSG\\].*"
		)));
		config.tabs.add(new TabConfig("guild", "Guild", List.of(
			"(?i)^MT.*",
			"(?i).*\\[Guild\\].*",
			"(?i).*\\[City\\].*",
			"(?i).*\\[Town\\].*",
			"(?i).*\\[G\\]\\s.*",
			"(?i).*\\[C\\]\\s.*"
		)));
		config.tabs.add(new TabConfig("coreprotect", "CoreProtect", List.of(
			"(?i).*\\[CoreProtect\\].*",
			"(?i).*CoreProtect.*"
		)));

		return config;
	}

	public static ModConfig load() {
		if (Files.exists(CONFIG_PATH)) {
			try {
				String json = Files.readString(CONFIG_PATH);
				instance = GSON.fromJson(json, ModConfig.class);
				ChatExpansion.LOGGER.info("Loaded ChatExpansion config");
				return instance;
			} catch (IOException e) {
				ChatExpansion.LOGGER.error("Failed to load config, using defaults", e);
			}
		}

		instance = createDefault();
		save();
		return instance;
	}

	public static void save() {
		if (instance == null) return;
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(instance));
		} catch (IOException e) {
			ChatExpansion.LOGGER.error("Failed to save config", e);
		}
	}

	public static ModConfig get() {
		if (instance == null) {
			return load();
		}
		return instance;
	}
}
