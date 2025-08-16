package justjabka.favorite_items.event;

import justjabka.favorite_items.FavoriteItemsStorage;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;


public class KeyInputHandler {
    public static final String KEY_CATEGORY_MAIN = "category.favorite_items.main";
    public static final String KEY_LOCK_ITEM = "key.favorite_items.lock_item";

    public static KeyBinding lockItemKey;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (lockItemKey.wasPressed()) {
                assert client.player != null;
                var stack = client.player.getMainHandStack();

                if (stack.isEmpty()) return;
                if (FavoriteItemsStorage.isFavorite(stack)) {
                    FavoriteItemsStorage.remove(stack);
                } else {
                    FavoriteItemsStorage.add(stack);
                }
            }
        });
    }

    public static void registerKeyInputs() {
        lockItemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_LOCK_ITEM,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KEY_CATEGORY_MAIN
        ));
        register();
    }
}