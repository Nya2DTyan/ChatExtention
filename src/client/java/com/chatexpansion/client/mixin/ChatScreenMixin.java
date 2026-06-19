package com.chatexpansion.client.mixin;

import com.chatexpansion.client.chat.ChatTabManager;
import com.chatexpansion.client.chat.FloatingChatWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

	@Shadow(remap = false) protected EditBox input;

	private static final int TAB_BAR_X = 2;
	private static final int INPUT_BOX_HEIGHT = 16;

	private int chatexpansion$tabBarY() {
		Minecraft mc = Minecraft.getInstance();
		int screenHeight = mc.getWindow().getGuiScaledHeight();
		return screenHeight - INPUT_BOX_HEIGHT - 2;
	}

	private int chatexpansion$tabBarWidth() {
		Minecraft mc = Minecraft.getInstance();
		// Chat width from options slider (0.0–1.0 → 40–320px)
		double scale = mc.options.chatWidth().get();
		return (int) (scale * 280.0 + 40.0);
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void chatexpansion$renderTabBar(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		var tabBar = ChatTabManager.getInstance().getTabBar();
		if (tabBar == null) return;

		tabBar.render(gfx, TAB_BAR_X, chatexpansion$tabBarY(), chatexpansion$tabBarWidth());

		for (FloatingChatWindow window : ChatTabManager.getInstance().getFloatingWindows()) {
			window.render(gfx, mouseX, mouseY, delta);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void chatexpansion$onMouseClicked(MouseButtonEvent event, boolean isDoubleClick, CallbackInfoReturnable<Boolean> cir) {
		var tabBar = ChatTabManager.getInstance().getTabBar();
		if (tabBar == null) return;

		double mx = event.x();
		double my = event.y();

		var windows = ChatTabManager.getInstance().getFloatingWindows();
		for (int i = windows.size() - 1; i >= 0; i--) {
			var win = windows.get(i);
			if (win.containsPoint(mx, my)) {
				win.mouseClicked(mx, my, event.button());
				cir.setReturnValue(true);
				return;
			}
		}

		int cy = chatexpansion$tabBarY();
		int cw = chatexpansion$tabBarWidth();

		if (event.button() == 0) {
			String clicked = tabBar.handleClick(mx, my, TAB_BAR_X, cy, cw);
			if (clicked != null) {
				ChatTabManager.getInstance().setActiveTab(clicked);
				if ("guild".equals(clicked) && input != null && input.getValue().isEmpty()) {
					input.setValue("?");
				}
				cir.setReturnValue(true);
				return;
			}
		}

		if (event.button() == 1) {
			String detach = tabBar.handleRightClick(mx, my, TAB_BAR_X, cy, cw);
			if (detach != null) {
				ChatTabManager.getInstance().detachTab(detach);
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void chatexpansion$onMouseScrolled(double mx, double my, double hAmt, double vAmt, CallbackInfoReturnable<Boolean> cir) {
		for (var win : ChatTabManager.getInstance().getFloatingWindows()) {
			if (win.mouseScrolled(mx, my, vAmt)) {
				cir.setReturnValue(true);
				return;
			}
		}
	}
}
