package justjabka.favorite_items;

import justjabka.favorite_items.event.KeyInputHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class FavoriteItemsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyInputHandler.registerKeyInputs();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                FavoriteItemsStorage.removeInvalid(client.player);
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FavoriteItemsStorage.init(client));
    }
}