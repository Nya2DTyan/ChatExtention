package com.chatexpansion.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Logger LOGGER = LoggerFactory.getLogger("chatexpansion");

	private static Path configPath;
	private static ModConfig instance;

	public List<TabConfig> tabs = new ArrayList<>();
	public List<WindowConfig> windows = new ArrayList<>();
	public String activeTabId = "main";

	public static class TabConfig {
		public String id;
		public String displayName;
		public List<String> patterns;
		public boolean visible = true;
		public boolean stealsFromMain;

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
		config.tabs.add(new TabConfig("guild", "Town", List.of(
			"(?i)^MT.*",
			"(?i).*\\[Guild\\].*",
			"(?i).*\\[City\\].*",
			"(?i).*\\[Town\\].*",
			"(?i).*\\[G\\]\\s.*",
			"(?i).*\\[C\\]\\s.*"
		)));
		TabConfig coreprotect = new TabConfig("coreprotect", "CoreProtect", List.of(
			"(?i).*\\[CoreProtect\\].*",
			"(?i).*CoreProtect.*",
			"(?i).*\\d+\\.\\d+/(h|m|d).*ago.*",
			"(?i).*\\(x-?\\d+/y-?\\d+/z-?\\d+.*",
			"(?i).*Page.*\\d+/\\d+.*",
			"(?i).*-----.*"
		));
		coreprotect.stealsFromMain = true;
		config.tabs.add(coreprotect);

		return config;
	}

	public static ModConfig load(Path path) {
		configPath = path;
		if (Files.exists(path)) {
			try {
				String json = Files.readString(path);
				instance = GSON.fromJson(json, ModConfig.class);
				migrateTabs(instance);
				LOGGER.info("Loaded ChatExpansion config");
				return instance;
			} catch (IOException e) {
				LOGGER.error("Failed to load config, using defaults", e);
			}
		}

		instance = createDefault();
		save();
		return instance;
	}

	private static void migrateTabs(ModConfig config) {
		boolean changed = false;
		for (TabConfig tab : config.tabs) {
			if ("guild".equals(tab.id) && "Guild".equals(tab.displayName)) {
				tab.displayName = "Town";
				changed = true;
				LOGGER.info("Renamed Guild tab to Town");
			}
			if ("coreprotect".equals(tab.id)) {
				if (tab.patterns != null && tab.patterns.size() <= 2) {
					tab.patterns = List.of(
						"(?i).*\\[CoreProtect\\].*",
						"(?i).*CoreProtect.*",
						"(?i).*\\d+\\.\\d+/(h|m|d).*ago.*",
						"(?i).*\\(x-?\\d+/y-?\\d+/z-?\\d+.*",
						"(?i).*Page.*\\d+/\\d+.*",
						"(?i).*-----.*"
					);
					changed = true;
					LOGGER.info("Migrated CoreProtect patterns to v2");
				}
				if (!tab.stealsFromMain) {
					tab.stealsFromMain = true;
					changed = true;
					LOGGER.info("Set CoreProtect stealsFromMain=true");
				}
			}
		}
		if (changed && configPath != null) {
			save();
		}
	}

	public static void save() {
		if (instance == null || configPath == null) return;
		try {
			Files.createDirectories(configPath.getParent());
			Files.writeString(configPath, GSON.toJson(instance));
		} catch (IOException e) {
			LOGGER.error("Failed to save config", e);
		}
	}

	public static ModConfig get() {
		if (instance == null) {
			throw new IllegalStateException("ModConfig not loaded yet. Call ModConfig.load(path) first.");
		}
		return instance;
	}
}
