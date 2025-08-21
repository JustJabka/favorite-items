package justjabka.favorite_items.mixin;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryScreenMixin<T extends ScreenHandler> extends HandledScreenMixin<T> {
    /**
     * @see HandledScreenMixin#handleFavoriteItemInteractions(Slot, int, int, SlotActionType, CallbackInfo)
    **/
    @Inject(at = @At("HEAD"), method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", cancellable = true)
    private void handle(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        super.handleFavoriteItemInteractions(slot, slotId, button, actionType, ci);
    }

    protected CreativeInventoryScreenMixin(Text title) {
        super(title);
    }
}