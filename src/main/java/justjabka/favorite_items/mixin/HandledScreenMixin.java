package justjabka.favorite_items.mixin;

import justjabka.favorite_items.FavoriteItemsStorage;
import justjabka.favorite_items.event.KeyInputHandler;
import justjabka.favorite_items.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
    @Shadow @Final protected T handler;

    @Inject(at = @At("TAIL"), method = "drawSlot")
    private void drawSlot(DrawContext ctx, Slot slot, CallbackInfo ci) {
        ItemStack stack = slot.getStack();
        if (stack.isEmpty() || !FavoriteItemsStorage.isFavorite(stack)) return;

        int x = slot.x + 8;
        int y = slot.y - 1;
        RenderUtil.renderFavIcon(ctx, x, y, 1f);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCursorSlot(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ItemStack cursorStack = handler.getCursorStack();
        if (cursorStack.isEmpty() || !FavoriteItemsStorage.isFavorite(cursorStack)) return;

        int x = mouseX;
        int y = mouseY - 8;
        RenderUtil.renderFavIcon(ctx, x, y, 1f);
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    protected void handleFavoriteItemInteractions(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        ItemStack target = ItemStack.EMPTY;

        if (actionType == SlotActionType.THROW && slot != null) {
            target = slot.getStack();
        }

        if (!target.isEmpty() && FavoriteItemsStorage.isFavorite(target)) {
            ci.cancel();
        }
    }

    protected HandledScreenMixin(Text title) {
        super(title);
    }
}


@Mixin(HandledScreen.class)
abstract class HandledScreenKeyMixin {
    @Shadow protected Slot focusedSlot;

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    private void handleFavoriteItemKeybind(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
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