package com.chatexpansion.client.chat;

import com.chatexpansion.client.chat.ChatTab.ChatMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class FloatingChatWindow {
	private final ChatTab tab;
	private float x, y, width, height;
	private boolean dragging;
	private boolean resizing;
	private float dragOffsetX, dragOffsetY;
	private boolean focused;

	private static final int TITLE_BAR_HEIGHT = 14;
	private static final int TITLE_COLOR = 0xFF222222;
	private static final int TITLE_TEXT_COLOR = 0xFFFFFFFF;
	private static final int BG_COLOR = 0xC0000000;
	private static final int BORDER_COLOR = 0xFF555555;
	private static final int CLOSE_BTN_SIZE = 10;

	public FloatingChatWindow(ChatTab tab, float x, float y, float width, float height) {
		this.tab = tab;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public ChatTab getTab() { return tab; }
	public float getX() { return x; }
	public float getY() { return y; }
	public float getWidth() { return width; }
	public float getHeight() { return height; }
	public boolean isFocused() { return focused; }
	public void setFocused(boolean focused) { this.focused = focused; }

	public void render(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		int ix = (int) x;
		int iy = (int) y;
		int iw = (int) width;
		int ih = (int) height;

		gfx.fill(ix, iy, ix + iw, iy + ih, BG_COLOR);
		gfx.fill(ix, iy, ix + iw, iy + TITLE_BAR_HEIGHT, focused ? 0xFF3355AA : TITLE_COLOR);

		gfx.text(font, tab.getDisplayName(), ix + 3, iy + 3, TITLE_TEXT_COLOR);

		int closeX = ix + iw - CLOSE_BTN_SIZE - 2;
		int closeY = iy + 2;
		gfx.fill(closeX, closeY, closeX + CLOSE_BTN_SIZE, closeY + CLOSE_BTN_SIZE, 0xFFCC3333);
		gfx.text(font, "x", closeX + 2, closeY + 1, 0xFFFFFFFF);

		gfx.fill(ix + iw - 6, iy + ih - 6, ix + iw, iy + ih, 0xFF888888);

		gfx.fill(ix, iy, ix + iw, iy + 1, BORDER_COLOR);
		gfx.fill(ix, iy + ih - 1, ix + iw, iy + ih, BORDER_COLOR);
		gfx.fill(ix, iy, ix + 1, iy + ih, BORDER_COLOR);
		gfx.fill(ix + iw - 1, iy, ix + iw, iy + ih, BORDER_COLOR);

		renderMessages(gfx, font, ix + 2, iy + TITLE_BAR_HEIGHT + 2, iw - 4, ih - TITLE_BAR_HEIGHT - 4);
	}

	private void renderMessages(GuiGraphicsExtractor gfx, Font font, int chatX, int chatY, int chatWidth, int chatHeight) {
		List<ChatMessage> messages = tab.getMessages();

		if (messages.isEmpty()) {
			gfx.text(font, "No messages", chatX, chatY, 0xFF888888);
			return;
		}

		int lineHeight = font.lineHeight + 1;
		int maxVisibleLines = chatHeight / lineHeight;
		int scrollOffset = tab.getScrollOffset();

		List<FormattedCharSequence> allLines = new ArrayList<>();
		for (ChatMessage msg : messages) {
			try {
				Component text = Component.literal(msg.plainText());
				List<FormattedCharSequence> wrapped = font.split(text, chatWidth);
				allLines.addAll(wrapped);
			} catch (Exception ignored) {}
		}

		int startIndex = Math.min(scrollOffset, allLines.size());
		int endIndex = Math.min(startIndex + maxVisibleLines, allLines.size());

		int drawY = chatY;
		for (int i = startIndex; i < endIndex; i++) {
			gfx.text(font, allLines.get(i), chatX, drawY, 0xFFCCCCCC);
			drawY += lineHeight;
		}

		if (allLines.size() > maxVisibleLines) {
			String scrollText = (scrollOffset + maxVisibleLines) + "/" + allLines.size();
			gfx.text(font, scrollText, chatX + chatWidth - font.width(scrollText), chatY, 0xFF888888);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return false;

		int ix = (int) x;
		int iy = (int) y;
		int iw = (int) width;
		int ih = (int) height;

		if (mouseX < ix || mouseX > ix + iw || mouseY < iy || mouseY > iy + ih) {
			focused = false;
			return false;
		}

		focused = true;

		int closeX = ix + iw - CLOSE_BTN_SIZE - 2;
		int closeY = iy + 2;
		if (mouseX >= closeX && mouseX <= closeX + CLOSE_BTN_SIZE
			&& mouseY >= closeY && mouseY <= closeY + CLOSE_BTN_SIZE) {
			ChatTabManager.getInstance().reattachTab(tab.getId());
			return true;
		}

		if (mouseX >= ix + iw - 8 && mouseX <= ix + iw
			&& mouseY >= iy + ih - 8 && mouseY <= iy + ih) {
			resizing = true;
			dragOffsetX = (float) (ix + iw - mouseX);
			dragOffsetY = (float) (iy + ih - mouseY);
			return true;
		}

		if (mouseY <= iy + TITLE_BAR_HEIGHT) {
			dragging = true;
			dragOffsetX = (float) (mouseX - x);
			dragOffsetY = (float) (mouseY - y);
			return true;
		}

		return true;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			boolean wasInteracting = dragging || resizing;
			dragging = false;
			resizing = false;
			return wasInteracting;
		}
		return false;
	}

	public boolean mouseDragged(double mouseX, double mouseY) {
		if (dragging) {
			x = (float) mouseX - dragOffsetX;
			y = (float) mouseY - dragOffsetY;
			return true;
		}
		if (resizing) {
			width = Math.max(80, (float) mouseX - x + dragOffsetX);
			height = Math.max(40, (float) mouseY - y + dragOffsetY);
			return true;
		}
		return false;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		int ix = (int) x;
		int iy = (int) y;
		int iw = (int) width;
		int ih = (int) height;

		if (mouseX < ix || mouseX > ix + iw || mouseY < iy || mouseY > iy + ih) {
			return false;
		}

		if (delta > 0) {
			tab.scrollUp(3);
		} else if (delta < 0) {
			tab.scrollDown(3);
		}
		return true;
	}

	public boolean containsPoint(double px, double py) {
		return px >= x && px <= x + width && py >= y && py <= y + height;
	}
}
