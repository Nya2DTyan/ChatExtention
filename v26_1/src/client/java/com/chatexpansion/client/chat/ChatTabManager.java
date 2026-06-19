package com.chatexpansion.client.chat;

import com.chatexpansion.ChatExpansion;
import com.chatexpansion.chat.ChatTab;
import com.chatexpansion.config.ModConfig;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatTabManager {
	private static ChatTabManager instance;

	private final List<ChatTab> tabs = new ArrayList<>();
	private String activeTabId = "main";
	private final List<FloatingChatWindow> floatingWindows = new ArrayList<>();
	private ChatTabBar tabBar;

	private ChatTabManager() {}

	public static ChatTabManager getInstance() {
		if (instance == null) {
			instance = new ChatTabManager();
		}
		return instance;
	}

	public void init() {
		tabs.clear();
		tabBar = new ChatTabBar(this);
		ModConfig config = ModConfig.get();

		for (ModConfig.TabConfig tabConfig : config.tabs) {
			ChatTab tab = new ChatTab(tabConfig.id, tabConfig.displayName, tabConfig.patterns, tabConfig.stealsFromMain);
			tabs.add(tab);
		}

		activeTabId = config.activeTabId;
		ChatExpansion.LOGGER.info("ChatTabManager initialized with {} tabs", tabs.size());

		// Apply filter after a short delay to let the GUI initialize
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui != null) {
			applyVisibleFilter();
		}
	}

	public ChatTabBar getTabBar() {
		return tabBar;
	}

	public List<ChatTab> getTabs() {
		return tabs;
	}

	public ChatTab getActiveTab() {
		for (ChatTab tab : tabs) {
			if (tab.getId().equals(activeTabId)) return tab;
		}
		return tabs.isEmpty() ? null : tabs.getFirst();
	}

	public ChatTab getTab(String id) {
		for (ChatTab tab : tabs) {
			if (tab.getId().equals(id)) return tab;
		}
		return null;
	}

	public void setActiveTab(String id) {
		this.activeTabId = id;
		ModConfig.get().activeTabId = id;
		ModConfig.save();
		applyVisibleFilter();
	}

	/**
	 * Apply the active tab's filter to the vanilla ChatComponent so only matching messages are displayed.
	 * setVisibleMessageFilter internally calls refreshTrimmedMessages() to rebuild the display list.
	 */
	public void applyVisibleFilter() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui == null) return;

		net.minecraft.client.gui.components.ChatComponent chat = mc.gui.getChat();
		ChatTab active = getActiveTab();

		if (active == null || active.getId().equals("main")) {
			chat.setVisibleMessageFilter(guiMessage -> {
				// Exclude messages that belong to stealsFromMain tabs
				String text = guiMessage.content().getString();
				for (ChatTab t : tabs) {
					if (t.isStealsFromMain() && t.matches(text)) {
						return false;
					}
				}
				return true;
			});
		} else {
			chat.setVisibleMessageFilter(guiMessage -> active.matches(guiMessage.content().getString()));
		}
	}

	/**
	 * Route an incoming chat message to all matching tabs.
	 * Also logs all messages to a debug file for filter tuning.
	 */
	public void onChatMessage(String plainText, Object[] components) {
		int time = (int) (Minecraft.getInstance().level != null
			? Minecraft.getInstance().level.getGameTime() : 0);

		ChatTab.ChatMessage message = new ChatTab.ChatMessage(plainText, components, time);

		// Check if any stealsFromMain tab matches this message
		boolean stolen = false;
		for (ChatTab tab : tabs) {
			if (tab.isStealsFromMain() && tab.matches(plainText)) {
				stolen = true;
				break;
			}
		}

		// Route to matching tabs
		StringBuilder matches = new StringBuilder();
		for (ChatTab tab : tabs) {
			if (tab.matches(plainText)) {
				// Skip main if a stealsFromMain tab claimed this message
				if (!tab.getId().equals("main") || !stolen) {
					tab.addMessage(message);
				}
				if (!matches.isEmpty()) matches.append(", ");
				matches.append(tab.getId());
			}
		}
		if (matches.isEmpty()) matches.append("(none)");

		// Log to file
		try {
			Path logPath = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("chatexpansion_chat.log");
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			String line = "[" + timestamp + "] matched=" + matches + " | " + plainText.replace("\n", "\\n") + "\n";
			Files.writeString(logPath, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			// silent
		}
	}

	public List<FloatingChatWindow> getFloatingWindows() {
		return floatingWindows;
	}

	/**
	 * Detach a tab into a floating window.
	 */
	public void detachTab(String tabId) {
		ChatTab tab = getTab(tabId);
		if (tab == null || tab.isDetached()) return;

		tab.setDetached(true);

		Minecraft mc = Minecraft.getInstance();
		float x = mc.getWindow().getGuiScaledWidth() * 0.6f;
		float y = 20;
		float width = 200;
		float height = 120;

		ModConfig config = ModConfig.get();
		for (ModConfig.WindowConfig wc : config.windows) {
			if (wc.tabId.equals(tabId)) {
				x = wc.x;
				y = wc.y;
				width = wc.width;
				height = wc.height;
				break;
			}
		}

		FloatingChatWindow window = new FloatingChatWindow(tab, x, y, width, height);
		floatingWindows.add(window);
	}

	/**
	 * Reattach a floating window's tab back to the tab bar.
	 */
	public void reattachTab(String tabId) {
		ChatTab tab = getTab(tabId);
		if (tab == null) return;

		tab.setDetached(false);
		floatingWindows.removeIf(w -> w.getTab().getId().equals(tabId));

		ModConfig config = ModConfig.get();
		config.windows.removeIf(w -> w.tabId.equals(tabId));
		ModConfig.save();
	}

	public void saveWindowPositions() {
		ModConfig config = ModConfig.get();
		config.windows.clear();

		for (FloatingChatWindow window : floatingWindows) {
			ModConfig.WindowConfig wc = new ModConfig.WindowConfig();
			wc.tabId = window.getTab().getId();
			wc.x = window.getX();
			wc.y = window.getY();
			wc.width = window.getWidth();
			wc.height = window.getHeight();
			config.windows.add(wc);
		}

		ModConfig.save();
	}
}
