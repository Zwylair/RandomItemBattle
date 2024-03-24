package zwylair.randomitembattle

import java.util.ArrayList

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

import net.minecraft.util.math.Vec3d
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.resource.featuretoggle.FeatureFlags

import zwylair.randomitembattle.commands.*
import zwylair.randomitembattle.serverevents.*


class RandomItemBattle : ModInitializer {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger("randomitembattle")
        var TIMEOUT_IN_TICKS: Int = 100
        var itemSpawningStatus: Boolean = false
        var isGameStarted: Boolean = false
        var resetWaitedTicks: Boolean = false
        const val CHAT_PREFIX_MOD: String = "§6[§eRandomItemBattle§6]§r"
        const val CHAT_PREFIX_WARN: String = "§5[§dWarning§5]§r"
        val restrictedItems: List<String> = listOf(
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
        )
        val pickableItems: MutableList<Item> = ArrayList()
        var playerPositions: MutableList<Vec3d> = ArrayList()
        lateinit var centerPosition: Vec3d
    }

    override fun onInitialize() {
        pickableItems.addAll(Registries.BLOCK.map { it.asItem() })
        pickableItems.addAll(Registries.ITEM.stream()
            .filter { item ->
                item.isEnabled(FeatureFlags.DEFAULT_ENABLED_FEATURES) && !restrictedItems.contains(Registries.ITEM.getId(item).toString())
            }.toList())

		Tick.register()
		AfterRespawn.register()
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			TimeoutCommand.register(dispatcher)
			StartposCommands.register(dispatcher)
			GameManage.register(dispatcher)
			ConfigureWorld.register(dispatcher)
			CreatePlayground.register(dispatcher)
		}
    }
}
