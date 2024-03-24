package zwylair.randomitembattle.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import zwylair.randomitembattle.RandomItemBattle
import zwylair.randomitembattle.utils.PosCalculator.Companion.roundPosition

object StartposCommands {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal("rib").then(literal("startpos").then(literal("add").executes { addStartpos(it) })))
        dispatcher.register(literal("rib").then(literal("startpos").then(literal("remove").executes { removeStartpos(it) })))
        dispatcher.register(literal("rib").then(literal("startpos").then(literal("clear").executes { removeAllStartpos(it) })))
        dispatcher.register(literal("rib").then(literal("set_center_position").executes { setCenterPos(it) }))
    }

    private fun addStartpos(ctx: CommandContext<ServerCommandSource>): Int {
        val player: PlayerEntity = ctx.source.player ?: return 1  // return if executor was not a player
        val spawnPosition: Vec3d = roundPosition(player.pos)

        if (spawnPosition in RandomItemBattle.playerPositions) {
            ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} §cThis position already added") }, false)
            return 1
        }

        RandomItemBattle.playerPositions.add(spawnPosition)

        val finalX = if (spawnPosition.x < 0) spawnPosition.x.toInt() - 1 else spawnPosition.x.toInt()
        val finalZ = if (spawnPosition.z < 0) spawnPosition.z.toInt() - 1 else spawnPosition.z.toInt()

        ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} Position [x:$finalX, y:${spawnPosition.y.toInt()}, z:$finalZ] was added") }, false)
        return 0
    }

    private fun removeStartpos(ctx: CommandContext<ServerCommandSource>): Int {
        val player: PlayerEntity = ctx.source.player ?: return 1
        val spawnPosition: Vec3d = roundPosition(player.pos)
        val finalX = if (spawnPosition.x < 0) spawnPosition.x.toInt() - 1 else spawnPosition.x.toInt()
        val finalZ = if (spawnPosition.z < 0) spawnPosition.z.toInt() - 1 else spawnPosition.z.toInt()

        if (spawnPosition !in RandomItemBattle.playerPositions) {
            ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} §cPosition [x:$finalX, y:${spawnPosition.y.toInt()}, z:$finalZ] is not in position list") }, false)
            return 1
        }
        RandomItemBattle.playerPositions.remove(spawnPosition)

        ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} Position [x:$finalX, y:${spawnPosition.y.toInt()}, z:$finalZ] was removed") }, false)
        return 0
    }

    private fun removeAllStartpos(ctx: CommandContext<ServerCommandSource>): Int {
        RandomItemBattle.playerPositions.clear()
        ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} Start positions were removed") }, false)
        return 0
    }

    private fun setCenterPos(ctx: CommandContext<ServerCommandSource>): Int {
        val player: PlayerEntity = ctx.source.player ?: return 1  // return if executor was not a player

        RandomItemBattle.centerPosition = roundPosition(player.pos)
        ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} Center position was set") }, false)
        return 0
    }
}
