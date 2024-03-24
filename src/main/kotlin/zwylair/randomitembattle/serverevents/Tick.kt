package zwylair.randomitembattle.serverevents

import java.util.concurrent.atomic.AtomicInteger

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.random.Random

import zwylair.randomitembattle.RandomItemBattle


object Tick {
    private val ticksCount = AtomicInteger(0)
    private val random = Random.create()

    fun register() {
        ServerTickEvents.START_WORLD_TICK.register(::resumeItemSpawning)
    }

    private fun getRandomItemStack(): ItemStack {
        return ItemStack(RandomItemBattle.pickableItems[random.nextInt(RandomItemBattle.pickableItems.size)])
    }

    private fun resumeItemSpawning(server: ServerWorld) {
        if (!RandomItemBattle.itemSpawningStatus) {
            return
        }

        if (RandomItemBattle.resetWaitedTicks) {
            ticksCount.set(1)
            RandomItemBattle.resetWaitedTicks = false
        }

        if (ticksCount.get() < RandomItemBattle.TIMEOUT_IN_TICKS) {
            ticksCount.incrementAndGet()
        } else {
            server.players.forEach { it.inventory.insertStack(getRandomItemStack()) }
            ticksCount.set(0)
        }
    }
}
