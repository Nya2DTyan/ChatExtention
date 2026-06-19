package com.chatexpansion.client.mixin;

import com.chatexpansion.client.chat.ChatTabManager;
import com.chatexpansion.client.chat.FloatingChatWindow;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void chatexpansion$renderFloatingWindows(GuiGraphicsExtractor gfx, DeltaTracker delta, CallbackInfo ci) {
		for (FloatingChatWindow window : ChatTabManager.getInstance().getFloatingWindows()) {
			window.render(gfx, -1, -1, 0);
		}
	}
}
