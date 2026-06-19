package com.chatexpansion.client.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class ChatTabBar {
	private static final int TAB_HEIGHT = 12;
	private static final int TAB_PADDING = 4;
	private static final int TAB_GAP = 2;
	private static final int ACTIVE_TAB_COLOR = 0xFF555555;
	private static final int INACTIVE_TAB_COLOR = 0xFF333333;
	private static final int ACTIVE_TEXT_COLOR = 0xFFFFFFFF;
	private static final int INACTIVE_TEXT_COLOR = 0xFFAAAAAA;
	private static final int BG_COLOR = 0xC0000000;

	private final ChatTabManager tabManager;

	public ChatTabBar(ChatTabManager tabManager) {
		this.tabManager = tabManager;
	}

	public void render(GuiGraphicsExtractor gfx, int chatX, int chatY, int chatWidth) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		List<ChatTab> tabs = tabManager.getTabs();
		String activeTabId = tabManager.getActiveTab().getId();

		gfx.fill(chatX - 1, chatY - TAB_HEIGHT - 2, chatX + chatWidth + 1, chatY, BG_COLOR);

		int currentX = chatX;
		for (ChatTab tab : tabs) {
			if (tab.isDetached()) continue;

			String label = tab.getDisplayName();
			int textWidth = font.width(label);
			int tabWidth = textWidth + TAB_PADDING * 2;

			boolean isActive = tab.getId().equals(activeTabId);
			int bgColor = isActive ? ACTIVE_TAB_COLOR : INACTIVE_TAB_COLOR;
			int textColor = isActive ? ACTIVE_TEXT_COLOR : INACTIVE_TEXT_COLOR;

			gfx.fill(currentX, chatY - TAB_HEIGHT - 1, currentX + tabWidth, chatY - 1, bgColor);
			gfx.text(font, label, currentX + TAB_PADDING, chatY - TAB_HEIGHT + 1, textColor);

			currentX += tabWidth + TAB_GAP;
		}
	}

	public String handleClick(double mouseX, double mouseY, int chatX, int chatY, int chatWidth) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		List<ChatTab> tabs = tabManager.getTabs();

		if (mouseY < chatY - TAB_HEIGHT - 2 || mouseY > chatY) return null;

		int currentX = chatX;
		for (ChatTab tab : tabs) {
			if (tab.isDetached()) continue;

			String label = tab.getDisplayName();
			int textWidth = font.width(label);
			int tabWidth = textWidth + TAB_PADDING * 2;

			if (mouseX >= currentX && mouseX <= currentX + tabWidth) {
				return tab.getId();
			}

			currentX += tabWidth + TAB_GAP;
		}

		return null;
	}

	public String handleRightClick(double mouseX, double mouseY, int chatX, int chatY, int chatWidth) {
		String clickedTab = handleClick(mouseX, mouseY, chatX, chatY, chatWidth);
		if (clickedTab != null && !clickedTab.equals("main")) {
			return clickedTab;
		}
		return null;
	}

	public int getTabBarHeight() {
		return TAB_HEIGHT + 2;
	}
}
