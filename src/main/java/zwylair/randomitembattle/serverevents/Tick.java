package zwylair.randomitembattle.serverevents;

import java.util.concurrent.atomic.AtomicInteger;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import static zwylair.randomitembattle.RandomItemBattle.*;


public class Tick {
    static AtomicInteger ticksCount = new AtomicInteger(0);

    public static void register() {
        ServerTickEvents.START_WORLD_TICK.register(Tick::resumeItemSpawning);
    }

    private static ItemStack getRandomItemStack(World world){
        int randomInt = world.getRandom().nextInt(pickableItems.size());
        Item randomItem = pickableItems.get(randomInt);

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
