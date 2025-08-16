package justjabka.favorite_items.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import justjabka.favorite_items.FavoriteItemsStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    private static final Identifier ICON_TEXTURE = new Identifier("favorite_items", "textures/gui/favorite_item.png");

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void renderHotbar(float tickDelta, DrawContext ctx, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int w = client.getWindow().getScaledWidth();
        int h = client.getWindow().getScaledHeight();
        int centerX = w / 2;
        int baseY = h - 19 - 3;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isEmpty() || !FavoriteItemsStorage.isFavorite(stack)) continue;

            int slotX = centerX - 90 + (i * 20) + 12;
            int slotY = baseY + 2;

            ctx.getMatrices().push();
            ctx.getMatrices().translate(0, 0, 300);

            RenderSystem.enableBlend();
            ctx.drawTexture(ICON_TEXTURE, slotX, slotY, 0, 0, 8, 8, 8, 8);
            RenderSystem.disableBlend();

            ctx.getMatrices().pop();
        }
    }
}
