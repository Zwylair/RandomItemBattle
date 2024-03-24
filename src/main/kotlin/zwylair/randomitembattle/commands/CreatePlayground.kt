package zwylair.randomitembattle.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext

import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

import zwylair.randomitembattle.RandomItemBattle
import zwylair.randomitembattle.utils.PosCalculator.Companion.posToCoord
import zwylair.randomitembattle.utils.PosCalculator.Companion.roundPosition

object CreatePlayground {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal("rib").then(literal("create_playground").executes { createPlayground(it) }))
    }

    private fun createPlayground(ctx: CommandContext<ServerCommandSource>): Int {
        val commandSource = ctx.source.server.commandSource
        val commandManager = ctx.source.server.commandManager

        RandomItemBattle.centerPosition = roundPosition(ctx.source.position)
        val cPos = RandomItemBattle.centerPosition
        RandomItemBattle.playerPositions = ArrayList()
        RandomItemBattle.playerPositions.add(Vec3d(cPos.x + 12, cPos.y + 8, cPos.z))
        RandomItemBattle.playerPositions.add(Vec3d(cPos.x - 12, cPos.y + 8, cPos.z))
        RandomItemBattle.playerPositions.add(Vec3d(cPos.x, cPos.y + 8, cPos.z + 12))
        RandomItemBattle.playerPositions.add(Vec3d(cPos.x, cPos.y + 8, cPos.z - 12))

        if (cPos.y < -40 || cPos.y > 290) {
            ctx.source.sendFeedback({Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} ${RandomItemBattle.CHAT_PREFIX_WARN} The Y coordinate is too close to the world boundaries. This may cause some problems with clearing the playground. Recommended Y coordinate for creating playground = 64.") }, false)
        }

        // clean center and every startpos
        commandManager.executeWithPrefix(commandSource, "/fill ${posToCoord(cPos.x) - 15} ${cPos.y.toInt() - 5} ${posToCoord(cPos.z) - 15} ${posToCoord(cPos.x) + 15} ${cPos.y.toInt() + 20} ${posToCoord(cPos.z) + 15} air")
        RandomItemBattle.playerPositions.forEach { commandManager.executeWithPrefix(commandSource, "/fill ${posToCoord(it.x) - 15} ${it.y.toInt() - 5} ${posToCoord(it.z) - 15} ${posToCoord(it.x) + 15} ${it.y.toInt() + 20} ${posToCoord(it.z) + 15} air") }

        // set bedrock blocks
        commandManager.executeWithPrefix(commandSource, "/fill ${posToCoord(cPos.x) - 1} ${cPos.y.toInt() - 1} ${posToCoord(cPos.z) - 1} ${posToCoord(cPos.x) + 1} ${cPos.y.toInt() - 1} ${posToCoord(cPos.z) + 1} bedrock")
        RandomItemBattle.playerPositions.forEach { commandManager.executeWithPrefix(commandSource, "/setblock ${posToCoord(it.x)} ${it.y.toInt() - 1} ${posToCoord(it.z)} bedrock") }

        ctx.source.sendFeedback({ Text.of(RandomItemBattle.CHAT_PREFIX_MOD + "Playground was created") }, false)
        return 0
    }
}
