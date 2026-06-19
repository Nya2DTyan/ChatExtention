package com.chatexpansion.client.mixin;

import com.chatexpansion.client.chat.ChatTabManager;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

	@Inject(method = "addPlayerMessage", at = @At("HEAD"))
	private void chatexpansion$capturePlayerMessage(Component message, MessageSignature signature, GuiMessageTag tag, CallbackInfo ci) {
		ChatTabManager.getInstance().onChatMessage(message.getString(), new Object[]{message, signature, tag});
	}

	@Inject(method = "addServerSystemMessage", at = @At("HEAD"))
	private void chatexpansion$captureServerSystemMessage(Component message, CallbackInfo ci) {
		ChatTabManager.getInstance().onChatMessage(message.getString(), new Object[]{message});
	}

	@Inject(method = "addClientSystemMessage", at = @At("HEAD"))
	private void chatexpansion$captureClientSystemMessage(Component message, CallbackInfo ci) {
		ChatTabManager.getInstance().onChatMessage(message.getString(), new Object[]{message});
	}
}
