package justjabka.favorite_items;

import com.google.gson.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import static justjabka.favorite_items.FavoriteItems.LOGGER;

@Environment(EnvType.CLIENT)
public class FavoriteItemsStorage {
        private static final Set<String> FAVORITES = new HashSet<>();
        private static Path saveFile;

        public static void init(MinecraftClient client) {
            String fileName = getString(client);

            saveFile = FabricLoader.getInstance().getConfigDir()
                    .resolve(FavoriteItems.MOD_ID)
                    .resolve(fileName);

            try {
                Files.createDirectories(saveFile.getParent());
            } catch (IOException e) {
                e.printStackTrace();
            }

            loadFavorites();

            LOGGER.info("Initializing client");
        }

    private static @NotNull String getString(MinecraftClient client) {
        String fileName;

        if (client.isIntegratedServerRunning() && client.getServer() != null) {
            // Singleplayer
            fileName = "world." + client.getServer().getSaveProperties().getLevelName() + ".json";
        } else if (client.getCurrentServerEntry() != null) {
            // Multiplayer
            ServerInfo info = client.getCurrentServerEntry();
            fileName = "server." + info.address.replace(":", "_") + ".json";
        } else {
            fileName = "unknown.json";
        }
        return fileName;
    }

    public static void add(ItemStack stack) {
            FAVORITES.add(serializeItem(stack));
            saveFavorites();
        }

        public static void remove(ItemStack stack) {
            FAVORITES.remove(serializeItem(stack));
            saveFavorites();
        }

        public static boolean isFavorite(ItemStack stack) {
            return FAVORITES.contains(serializeItem(stack));
        }

        public static void removeInvalid(ClientPlayerEntity player) {
            if (player == null) return;

            Set<String> currentStacks = new HashSet<>();
            for (ItemStack stack : player.getInventory().main) {
                if (!stack.isEmpty()) currentStacks.add(serializeItem(stack));
            }

            if (FAVORITES.removeIf(key -> !currentStacks.contains(key))) {
                saveFavorites();
            }
        }

        private static String serializeItem(ItemStack stack) {
            NbtCompound tag = stack.getOrCreateNbt().copy();
            tag.remove("Damage"); // Ignoring Damage NBT

            return Registries.ITEM.getId(stack.getItem()) + tag.toString();
        }

        private static void loadFavorites() {
            FAVORITES.clear();
            if (saveFile == null || !Files.exists(saveFile)) return;

            try (Reader reader = Files.newBufferedReader(saveFile)) {
                JsonArray arr = JsonParser.parseReader(reader).getAsJsonArray();
                for (JsonElement el : arr) {
                    FAVORITES.add(el.getAsString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void saveFavorites() {
            if (saveFile == null) return;

            try (Writer writer = Files.newBufferedWriter(saveFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                JsonArray arr = new JsonArray();
                for (String s : FAVORITES) arr.add(s);
                Gson gson = new Gson();
                gson.toJson(arr, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}