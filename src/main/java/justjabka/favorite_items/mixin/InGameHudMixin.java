package justjabka.favorite_items.mixin;

import justjabka.favorite_items.FavoriteItemsStorage;
import justjabka.favorite_items.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void renderHotbar(float tickDelta, DrawContext ctx, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client.player == null) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isEmpty() || !FavoriteItemsStorage.isFavorite(stack)) continue;

            int x = ((scaledWidth / 2) - 88 + i * 20) + 8;
            int y = scaledHeight - 19;

            RenderUtil.renderFavIcon(ctx, x, y, 1f);
        }
    }
}