package zwylair.randomitembattle.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext

import net.minecraft.text.Text
import net.minecraft.world.GameMode
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

import zwylair.randomitembattle.RandomItemBattle
import zwylair.randomitembattle.commands.ConfigureWorld.isWorldNotConfigured
import zwylair.randomitembattle.utils.PosCalculator.Companion.posToCoord

object GameManage {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal("rib").then(literal("game").then(literal("start").executes { startGame(it) })))
        dispatcher.register(literal("rib").then(literal("game").then(literal("stop").executes { stopGame(it) })))
        dispatcher.register(literal("rib").then(literal("items").then(literal("pause").executes { pauseItemSpawning(it) })))
        dispatcher.register(literal("rib").then(literal("items").then(literal("resume").executes { resumeItemSpawning(it) })))
    }

    private fun startGame(ctx: CommandContext<ServerCommandSource>): Int {
        val world = ctx.source.world  // shortcut

        if (isWorldNotConfigured(world)) ConfigureWorld.configureWorld(ctx)

        // centerpos is not set
        try {
            RandomItemBattle.centerPosition
        } catch (e: UninitializedPropertyAccessException) {
            ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} §cThe center position is not configured! Configure the center position with (/rib set_center_pos)") }, false)
            return 1
        }

        // there is no startpos
        if (RandomItemBattle.playerPositions.isEmpty()) {
            ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} §cThere is no one position to start with!") }, false)
            return 1
        }

        // there are too many players for startpos count
        if (world.players.size > RandomItemBattle.playerPositions.size) {
            ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} §cThere are only ${RandomItemBattle.playerPositions.size} positions for ${world.players.size} players!") }, false)
            return 1
        }

        // centerpos is too close to world borders
        if (RandomItemBattle.centerPosition.y < -40 || RandomItemBattle.centerPosition.y > 290) {
            ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} ${RandomItemBattle.CHAT_PREFIX_WARN} The Y coordinate is too close to the world borders. This may cause some problems with clearing the playground. Recommended Y coordinate for creating playground = 64.") }, false)
        }

        val cPos = RandomItemBattle.centerPosition  // shortcut
        val server = ctx.source.server  // shortcut
        val playerPositionsLeft = ArrayList(RandomItemBattle.playerPositions)

        // clean and place procedures in separate forEach because the server chaotic
        // executes commands, so we need some time interval before teleporting players

        // clean center and every startpos
        server.commandManager.executeWithPrefix(server.commandSource, "/fill ${posToCoord(cPos.x) - 15} ${cPos.y.toInt() - 5} ${posToCoord(cPos.z) - 15} ${posToCoord(cPos.x) + 15} ${cPos.y.toInt() + 20} ${posToCoord(cPos.z) + 15} air")
        RandomItemBattle.playerPositions.forEach {
            server.commandManager.executeWithPrefix(server.commandSource, "/fill ${posToCoord(it.x) - 15} ${it.y.toInt() - 5} ${posToCoord(it.z) - 15} ${posToCoord(it.x) + 15} ${it.y.toInt() + 20} ${posToCoord(it.z) + 15} air")
        }

        // set bedrock blocks
        server.commandManager.executeWithPrefix(server.commandSource, "/fill ${posToCoord(cPos.x) - 1} ${cPos.y.toInt() - 1} ${posToCoord(cPos.z) - 1} ${posToCoord(cPos.x) + 1} ${cPos.y.toInt() - 1} ${posToCoord(cPos.z) + 1} bedrock")
        RandomItemBattle.playerPositions.forEach {
            server.commandManager.executeWithPrefix(server.commandSource, "/setblock ${posToCoord(it.x)} ${it.y.toInt() - 1} ${posToCoord(it.z)} bedrock")
        }

        server.commandManager.executeWithPrefix(server.commandSource, "/kill @e[type=!player]")

        // prepare players (also interval)
        world.players.forEach {
            it.inventory.clear()
            it.changeGameMode(GameMode.SURVIVAL)
            it.hungerManager.foodLevel = 20
            it.heal(20.0f)
        }

        // teleport players to random startpos
        world.players.forEach {
            val randomPositionIndex = world.random.nextInt(playerPositionsLeft.size)
            val randomPosition = playerPositionsLeft[randomPositionIndex]

            it.teleport(randomPosition.x, randomPosition.y, randomPosition.z)
            playerPositionsLeft.removeAt(randomPositionIndex)
        }

        RandomItemBattle.itemSpawningStatus = true
        RandomItemBattle.resetWaitedTicks = true
        RandomItemBattle.isGameStarted = true
        return 0
    }

    private fun stopGame(ctx: CommandContext<ServerCommandSource>): Int {
        if (!RandomItemBattle.isGameStarted)
        {
            ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} §cThe game is not started!") }, false)
            return 1
        }

        ctx.source.world.players.forEach {
            it.changeGameMode(GameMode.CREATIVE)
            it.hungerManager.foodLevel = 20
            it.heal(20.0f)
        }

        RandomItemBattle.itemSpawningStatus = false
        RandomItemBattle.resetWaitedTicks = true
        RandomItemBattle.isGameStarted = false
        return 0
    }

    private fun pauseItemSpawning(ctx: CommandContext<ServerCommandSource>): Int {
        RandomItemBattle.itemSpawningStatus = false

        ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} Item spawning was paused") }, false)
        return 0
    }

    private fun resumeItemSpawning(ctx: CommandContext<ServerCommandSource>): Int {
        RandomItemBattle.itemSpawningStatus = true

        ctx.source.sendFeedback({ Text.of("${RandomItemBattle.CHAT_PREFIX_MOD} Item spawning was resumed") }, false)
        return 0
    }
}
