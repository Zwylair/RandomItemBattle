package zwylair.randomitembattle.serverevents

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameMode

import zwylair.randomitembattle.RandomItemBattle.Companion.isGameStarted
import zwylair.randomitembattle.RandomItemBattle.Companion.centerPosition
import zwylair.randomitembattle.RandomItemBattle.Companion.resetWaitedTicks
import zwylair.randomitembattle.RandomItemBattle.Companion.itemSpawningStatus

object AfterRespawn {
    fun register() {
        ServerPlayerEvents.AFTER_RESPAWN.register(::resumeItemSpawning)
    }

    private fun resumeItemSpawning(oldPlayer: ServerPlayerEntity, newPlayer: ServerPlayerEntity, alive: Boolean) {
        if (!isGameStarted) return

        val gameEndTitleCommand = """/title @a title ["",{"text":"%s","bold":true,"color":"gold"},{"text\":" win!","bold":true,"color":"yellow"}]"""
        val livingPlayers = mutableListOf<ServerPlayerEntity>()
        val server = newPlayer.server ?: return  // if executor was not a player
        val serverPlayers = server.playerManager.playerList
        val centerBlockPos = BlockPos(centerPosition.x.toInt(), centerPosition.y.toInt(), centerPosition.z.toInt())

        newPlayer.changeGameMode(GameMode.SPECTATOR)
        newPlayer.teleport(centerPosition.x, centerPosition.y + 7, centerPosition.z)

        serverPlayers.forEach { if (it.interactionManager.gameMode == GameMode.SURVIVAL) livingPlayers.add(it) }

        when (livingPlayers.size) {
            1 -> {
                val winnerPlayer = livingPlayers[0]
                winnerPlayer.changeGameMode(GameMode.SPECTATOR)

                serverPlayers.forEach {
                    it.teleport(centerPosition.x, centerPosition.y + 7, centerPosition.z)
                    server.commandManager.executeWithPrefix(server.commandSource, gameEndTitleCommand.format(winnerPlayer.name.string))
                }

                newPlayer.world.playSound(null, centerBlockPos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1f, 1f)

                resetWaitedTicks = true
                itemSpawningStatus = false
                isGameStarted = false
            }

            0 -> {
                serverPlayers.forEach {
                    it.teleport(centerPosition.x, centerPosition.y + 7, centerPosition.z)
                    server.commandManager.executeWithPrefix(server.commandSource, gameEndTitleCommand.format("No one"))
                }

                resetWaitedTicks = true
                itemSpawningStatus = false
                isGameStarted = false
            }

            else -> newPlayer.world.playSound(newPlayer, centerBlockPos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1f, 1f)
        }
    }
}
