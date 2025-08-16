package justjabka.favorite_items.mixin;

import justjabka.favorite_items.FavoriteItemsStorage;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "dropSelectedItem", cancellable = true)
    public void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir){
        ItemStack selected = ((ClientPlayerEntity) (Object) this).getMainHandStack();

        if (FavoriteItemsStorage.isFavorite(selected)) {
            cir.cancel();
        }
    }
}

