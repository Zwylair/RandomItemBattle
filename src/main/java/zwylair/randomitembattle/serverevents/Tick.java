package zwylair.randomitembattle.serverevents;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

import static zwylair.randomitembattle.RandomItemBattle.*;


public class Tick {
    static AtomicInteger ticksCount = new AtomicInteger(0);
    static List<String> restrictedItems = Arrays.stream(new String[] {
            "minecraft:command_block",
            "minecraft:chain_command_block",
            "minecraft:repeating_command_block",
            "minecraft:command_block_minecart",
            "minecraft:air",
            "minecraft:jigsaw",
            "minecraft:structure_block",
            "minecraft:structure_void",
            "minecraft:ender_dragon_spawn_egg",
            "minecraft:light_gray_stained_glass_pane",
            "minecraft:enchanted_book",
            "minecraft:sculk_shrieker"
    }).toList();

    public static void register() {
        ServerTickEvents.START_WORLD_TICK.register(Tick::resumeItemSpawning);
    }

    private static ItemStack getRandomItemStack(World world){
        Random random = world.getRandom();
        Item randomItem;

        do {
            randomItem = pickableItems.get(random.nextInt(pickableItems.size()));
        } while (
                !randomItem.isEnabled(FeatureFlags.DEFAULT_ENABLED_FEATURES) ||
                restrictedItems.contains(Registries.ITEM.getId(randomItem).toString())
        );

        return new ItemStack(randomItem);
    }

    public static void resumeItemSpawning(ServerWorld server) {
        if (!itemSpawningStatus) {
            return;
        }

        if (resetWaitedTicks) {
            ticksCount.set(1);
            resetWaitedTicks = false;
        }

        if (ticksCount.get() < TIMEOUT_IN_TICKS) {
            ticksCount.incrementAndGet();
        } else {
            server.getPlayers().forEach((player) -> player.getInventory().insertStack(getRandomItemStack(player.getWorld())));
            ticksCount.set(0);
        }
    }
}
