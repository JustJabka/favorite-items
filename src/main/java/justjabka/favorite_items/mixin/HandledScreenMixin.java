package justjabka.favorite_items.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import justjabka.favorite_items.FavoriteItemsStorage;
import justjabka.favorite_items.event.KeyInputHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    private static final Identifier ICON_TEXTURE = new Identifier("favorite_items", "textures/gui/favorite_item.png");

    @Inject(at = @At("HEAD"), method = "drawSlot")
    private void drawSlot(DrawContext ctx, Slot slot, CallbackInfo ci) {
        ItemStack stack = slot.getStack();
        if (stack.isEmpty() || !FavoriteItemsStorage.isFavorite(stack)) return;

        int slotX = slot.x + 10;
        int slotY = slot.y - 2;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(0, 0, 300);

        RenderSystem.enableBlend();
        ctx.drawTexture(ICON_TEXTURE, slotX, slotY, 0, 0, 8, 8, 8, 8);
        RenderSystem.disableBlend();

        ctx.getMatrices().pop();
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void blockInventoryDrop(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        ItemStack target = ItemStack.EMPTY;

        if (actionType == SlotActionType.THROW && slot != null) {
            target = slot.getStack();
        }

        if (!target.isEmpty() && FavoriteItemsStorage.isFavorite(target)) {
            ci.cancel();
        }
    }
}

@Mixin(HandledScreen.class)
abstract class HandledScreenKeyMixin {
    @Shadow protected Slot focusedSlot;

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    private void toggleOnKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!KeyInputHandler.lockItemKey.matchesKey(keyCode, scanCode)) return;
        if (focusedSlot == null || !focusedSlot.hasStack()) return;

        var stack = focusedSlot.getStack();
        if (FavoriteItemsStorage.isFavorite(stack)) {
            FavoriteItemsStorage.remove(stack);
        } else {
            FavoriteItemsStorage.add(stack);
        }
        cir.setReturnValue(true);
    }
}