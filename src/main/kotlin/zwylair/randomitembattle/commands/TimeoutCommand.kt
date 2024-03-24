package zwylair.randomitembattle.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.arguments.IntegerArgumentType

import net.minecraft.text.Text
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal

import zwylair.randomitembattle.RandomItemBattle

object TimeoutCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal("rib").then(literal("timeout").then(
            argument("seconds", IntegerArgumentType.integer())
                .executes { execute(it) } )
        )) }

    private fun execute(ctx: CommandContext<ServerCommandSource>): Int {
        val timeoutInSeconds = IntegerArgumentType.getInteger(ctx, "seconds")
        RandomItemBattle.TIMEOUT_IN_TICKS = timeoutInSeconds * 20

        ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} Timeout set to $timeoutInSeconds") }, false)
        return 0
    }
}
