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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static justjabka.favorite_items.FavoriteItemsClient.MOD_ID;
import static justjabka.favorite_items.FavoriteItemsClient.LOGGER;

@Environment(EnvType.CLIENT)
public class FavoriteItemsStorage {
    private static final Set<String> FAVORITES = new HashSet<>();
    private static Path saveFile;

    public static void init(MinecraftClient client) {
        String fileName = getString(client);

        saveFile = FabricLoader.getInstance().getConfigDir()
                .resolve(MOD_ID)
                .resolve(fileName);

        try {
            Files.createDirectories(saveFile.getParent());
        } catch (IOException e) {
            LOGGER.error(String.valueOf(e));
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
        FAVORITES.add(getStackId(stack));
        saveFavorites();
    }

    public static void remove(ItemStack stack) {
        FAVORITES.remove(getStackId(stack));
        saveFavorites();
    }

    public static boolean isFavorite(ItemStack stack) {
        return FAVORITES.contains(getStackId(stack));
    }

    public static void removeInvalid(ClientPlayerEntity player) {
        Set<String> stillValid = Stream.concat(
                        // Inventory, Armor and Offhand
                        Stream.concat(
                                player.getInventory().main.stream(),
                                Stream.concat(
                                        player.getInventory().armor.stream(),
                                        Stream.of(player.getOffHandStack())
                                )
                        ),
                        // Cursor (only if screen is open)
                        player.currentScreenHandler != null
                                ? Stream.of(player.currentScreenHandler.getCursorStack())
                                : Stream.empty()
                )
                .filter(stack -> !stack.isEmpty())
                .map(FavoriteItemsStorage::getStackId)
                .collect(Collectors.toSet());

        FAVORITES.removeIf(fav -> !stillValid.contains(fav));
    }

    private static String getStackId(ItemStack stack) {
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
            LOGGER.error(String.valueOf(e));
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
            LOGGER.error(String.valueOf(e));
        }
    }
}