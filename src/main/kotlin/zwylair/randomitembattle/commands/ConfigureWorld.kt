package zwylair.randomitembattle.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext

import net.minecraft.text.Text
import net.minecraft.world.GameRules
import net.minecraft.world.World
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

import zwylair.randomitembattle.RandomItemBattle

object ConfigureWorld {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal("rib").then(literal("configure_world").executes { configureWorld(it) }))
    }

    fun isWorldNotConfigured(world: World): Boolean {
        val gameRules = world.gameRules

        return gameRules.get(GameRules.DO_MOB_SPAWNING).get() ||
                gameRules.get(GameRules.DO_PATROL_SPAWNING).get() ||
                gameRules.get(GameRules.DO_TRADER_SPAWNING).get() ||
                gameRules.get(GameRules.DO_WARDEN_SPAWNING).get() ||
                !gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).get()
    }

    fun configureWorld(world: World) {
        val gameRules = world.gameRules

        gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, world.server)
        gameRules.get(GameRules.DO_PATROL_SPAWNING).set(false, world.server)
        gameRules.get(GameRules.DO_TRADER_SPAWNING).set(false, world.server)
        gameRules.get(GameRules.DO_WARDEN_SPAWNING).set(false, world.server)
        gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, world.server)
    }

    fun configureWorld(ctx: CommandContext<ServerCommandSource>): Int {
        configureWorld(ctx.source.world)
        ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} The world was configured") }, false)
        return 0
    }
}
