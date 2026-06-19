package com.chatexpansion.client.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChatTab {
	private final String id;
	private final String displayName;
	private final List<Pattern> filterPatterns;
	private final List<ChatMessage> messages = new ArrayList<>();
	private boolean detached;
	private int scrollOffset;
	private boolean stealsFromMain;

	public ChatTab(String id, String displayName, List<String> patterns) {
		this(id, displayName, patterns, false);
	}

	public ChatTab(String id, String displayName, List<String> patterns, boolean stealsFromMain) {
		this.id = id;
		this.displayName = displayName;
		this.stealsFromMain = stealsFromMain;
		this.filterPatterns = new ArrayList<>();
		for (String p : patterns) {
			try {
				this.filterPatterns.add(Pattern.compile(p));
			} catch (Exception e) {
				// skip invalid patterns
			}
		}
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public List<ChatMessage> getMessages() {
		return messages;
	}

	public boolean isDetached() {
		return detached;
	}

	public boolean isStealsFromMain() {
		return stealsFromMain;
	}

	public void setDetached(boolean detached) {
		this.detached = detached;
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public void setScrollOffset(int offset) {
		this.scrollOffset = Math.max(0, offset);
	}

	public void scrollUp(int amount) {
		this.scrollOffset = Math.min(messages.size() - 1, this.scrollOffset + amount);
	}

	public void scrollDown(int amount) {
		this.scrollOffset = Math.max(0, this.scrollOffset - amount);
	}

	/**
	 * Returns true if this tab's filters match the given plain text.
	 * The "main" tab (id "main") always matches all messages.
	 */
	public boolean matches(String plainText) {
		if (id.equals("main")) return true;
		if (filterPatterns.isEmpty()) return false;
		for (Pattern p : filterPatterns) {
			if (p.matcher(plainText).find()) {
				return true;
			}
		}
		return false;
	}

	private static final int MAX_MESSAGES = 300;

	public void addMessage(ChatMessage message) {
		messages.addFirst(message);
		while (messages.size() > MAX_MESSAGES) {
			messages.removeLast();
		}
	}

	public record ChatMessage(String plainText, Object[] components, int addedTime) {
	}
}
