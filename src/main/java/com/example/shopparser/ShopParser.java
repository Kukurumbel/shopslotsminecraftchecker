package com.example.shopparser;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ShopParser {
    public static boolean isParsing = false;
    private static int currentCategoryIndex = 0;
    private static final List<Integer> categorySlots = new ArrayList<>();

    public static void start() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        isParsing = true;
        currentCategoryIndex = 0;
        categorySlots.clear();

        client.player.sendMessage(Text.literal("§e[Парсер] Запуск... Открываю магазин."), false);
        client.player.networkHandler.sendChatCommand("shop");
    }

    public static void handleScreen(GenericContainerScreen screen) {
        if (!isParsing) return;

        MinecraftClient client = MinecraftClient.getInstance();
        var handler = screen.getScreenHandler();
        String title = screen.getTitle().getString();

        if (categorySlots.isEmpty()) {
            client.player.sendMessage(Text.literal("§e[Парсер] Сканирую категории..."), false);
            
            for (int i = 0; i < handler.slots.size() - 36; i++) {
                ItemStack stack = handler.getSlot(i).getStack();
                if (!stack.isEmpty()) {
                    categorySlots.add(i);
                }
            }

            if (categorySlots.isEmpty()) {
                client.player.sendMessage(Text.literal("§c[Парсер] Категории не найдены!"), false);
                isParsing = false;
                return;
            }

            delayClick(screen, categorySlots.get(currentCategoryIndex));
        } else {
            parseItemsInContainer(screen);

            currentCategoryIndex++;
            if (currentCategoryIndex < categorySlots.size()) {
                executeDelayed(() -> {
                    if (client.player != null) {
                        client.player.closeHandledScreen();
                        client.player.networkHandler.sendChatCommand("shop");
                    }
                }, 400);
            } else {
                isParsing = false;
                client.player.sendMessage(Text.literal("§a[Парсер] Цены успешно сохранены в файл shop_prices.txt!"), false);
                executeDelayed(() -> {
                    if (client.player != null) client.player.closeHandledScreen();
                }, 200);
            }
        }
    }

    private static void parseItemsInContainer(GenericContainerScreen screen) {
        var handler = screen.getScreenHandler();
        String categoryName = screen.getTitle().getString().replaceAll("§.", "");
        String filePath = Paths.get(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), "shop_prices.txt").toString();

        try (FileWriter fw = new FileWriter(filePath, true);
             PrintWriter writer = new PrintWriter(fw)) {

            writer.println("=== Категория: " + categoryName + " ===");

            for (int i = 0; i < handler.slots.size() - 36; i++) {
                ItemStack stack = handler.getSlot(i).getStack();
                if (stack.isEmpty()) continue;

                String itemName = stack.getName().getString().replaceAll("§.", "");
                writer.println("Предмет: " + itemName);

                LoreComponent loreComponent = stack.getComponents().get(DataComponentTypes.LORE);
                if (loreComponent != null) {
                    for (Text line : loreComponent.lines()) {
                        String cleanLine = line.getString().replaceAll("§.", "").trim();
                        if (!cleanLine.isEmpty()) {
                            writer.println("  " + cleanLine);
                        }
                    }
                }
            }
            writer.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void delayClick(GenericContainerScreen screen, int slotId) {
        executeDelayed(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.interactionManager != null && client.player != null) {
                client.interactionManager.clickSlot(
                        screen.getScreenHandler().syncId,
                        slotId,
                        0,
                        SlotActionType.PICKUP,
                        client.player
                );
            }
        }, 300);
    }

    private static void executeDelayed(Runnable action, long delayMs) {
        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS).execute(() -> {
            MinecraftClient.getInstance().execute(action);
        });
    }
}
