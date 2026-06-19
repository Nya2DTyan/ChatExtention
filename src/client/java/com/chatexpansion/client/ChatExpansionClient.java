package com.chatexpansion.client;

import com.chatexpansion.ChatExpansion;
import com.chatexpansion.client.chat.ChatTabManager;
import com.chatexpansion.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ChatExpansionClient implements ClientModInitializer {

	private static final KeyMapping.Category CHATEXPANSION_CATEGORY =
		KeyMapping.Category.register(Identifier.withDefaultNamespace("chatexpansion"));

	private static KeyMapping toggleTab1;
	private static KeyMapping toggleTab2;
	private static KeyMapping toggleTab3;
	private static KeyMapping toggleTab4;

	@Override
	public void onInitializeClient() {
		ModConfig.load();
		ChatTabManager.getInstance().init();

		toggleTab1 = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.chatexpansion.tab_main", org.lwjgl.glfw.GLFW.GLFW_KEY_1, CHATEXPANSION_CATEGORY));
		toggleTab2 = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.chatexpansion.tab_pm", org.lwjgl.glfw.GLFW.GLFW_KEY_2, CHATEXPANSION_CATEGORY));
		toggleTab3 = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.chatexpansion.tab_guild", org.lwjgl.glfw.GLFW.GLFW_KEY_3, CHATEXPANSION_CATEGORY));
		toggleTab4 = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.chatexpansion.tab_coreprotect", org.lwjgl.glfw.GLFW.GLFW_KEY_4, CHATEXPANSION_CATEGORY));

		// Restore detached windows from config
		ModConfig config = ModConfig.get();
		for (ModConfig.WindowConfig wc : config.windows) {
			ChatTabManager.getInstance().detachTab(wc.tabId);
		}

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (toggleTab1.consumeClick()) ChatTabManager.getInstance().setActiveTab("main");
			if (toggleTab2.consumeClick()) ChatTabManager.getInstance().setActiveTab("pm");
			if (toggleTab3.consumeClick()) ChatTabManager.getInstance().setActiveTab("guild");
			if (toggleTab4.consumeClick()) ChatTabManager.getInstance().setActiveTab("coreprotect");
		});

		ChatExpansion.LOGGER.info("ChatExpansion client initialized");
	}
}
