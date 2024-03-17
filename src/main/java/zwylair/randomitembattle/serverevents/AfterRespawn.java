package zwylair.randomitembattle.serverevents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.world.GameMode;
//import net.minecraft.sound.SoundCategory;
//import net.minecraft.sound.SoundEvents;

import static zwylair.randomitembattle.RandomItemBattle.*;


public class AfterRespawn {
    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register(AfterRespawn::resumeItemSpawning);
    }

    public static void resumeItemSpawning(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        if (!isGameStarted) {
            return;
        }

        newPlayer.changeGameMode(GameMode.SPECTATOR);
        newPlayer.teleport(centerPosition.x, centerPosition.y + 7, centerPosition.z);

        List<ServerPlayerEntity> livingPlayers = new ArrayList<>();
        List<ServerPlayerEntity> serverPlayers = Objects.requireNonNull(oldPlayer.getServer()).getPlayerManager().getPlayerList();

        serverPlayers.forEach((sPlayer) -> {
            if (sPlayer.isAlive()) { livingPlayers.add(sPlayer); }
        });

        if (livingPlayers.size() == 1) {
            ServerPlayerEntity winnerPlayer = livingPlayers.get(0);
            winnerPlayer.changeGameMode(GameMode.SPECTATOR);
//				winnerPlayer.getWorld().playSound(winnerPlayer, winnerPlayer.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1f, 1f);

            serverPlayers.forEach((sPlayer -> {
                sPlayer.teleport(centerPosition.x, centerPosition.y + 7, centerPosition.z);
                try {
                    sPlayer.sendMessageToClient(Texts.parse(sPlayer.getCommandSource(), Text.of(winnerPlayer.getName().toString() + " win!"), sPlayer, 0), true);
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }));

            resetWaitedTicks = true;
            itemSpawningStatus = false;
            isGameStarted = false;
        }
    }
}
