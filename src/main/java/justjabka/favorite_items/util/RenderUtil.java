package justjabka.favorite_items.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Unique;

import static justjabka.favorite_items.FavoriteItemsClient.MOD_ID;

public class RenderUtil {
    @Unique private static final Identifier ICON_TEXTURE = new Identifier(MOD_ID, "textures/gui/favorite_item.png");

    public static void renderFavIcon(DrawContext ctx, int x, int y, float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        var matrices = ctx.getMatrices();

        // Draw icon shadow
        RenderSystem.setShaderColor(0f, 0f, 0f, alpha * 0.25f);
        matrices.push();
        matrices.translate(0.5f, 0.5f, 0f);
        ctx.drawTexture(ICON_TEXTURE, x, y, 0, 0, 8, 8, 8, 8);
        matrices.pop();

        // Draw icon
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        ctx.drawTexture(ICON_TEXTURE, x, y, 0, 0, 8, 8, 8, 8);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}