package zwylair.randomitembattle.serverevents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
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

        String gameEndTitleCommand = "/title @a title [\"\",{\"text\":\"%s\",\"bold\":true,\"color\":\"gold\"},{\"text\":\" win!\",\"bold\":true,\"color\":\"yellow\"}]";
        List<ServerPlayerEntity> livingPlayers = new ArrayList<>();
        List<ServerPlayerEntity> serverPlayers = Objects.requireNonNull(oldPlayer.getServer()).getPlayerManager().getPlayerList();
        MinecraftServer server = newPlayer.getServer();
        assert server != null;
        CommandManager commandManager = server.getCommandManager();
        ServerCommandSource commandSource = server.getCommandSource();
        World world = newPlayer.getWorld();

        newPlayer.changeGameMode(GameMode.SPECTATOR);
        newPlayer.teleport(centerPosition.x, centerPosition.y + 7, centerPosition.z);

        serverPlayers.forEach((sPlayer) -> {
            if (sPlayer.interactionManager.getGameMode() == GameMode.SURVIVAL) { livingPlayers.add(sPlayer); }
        });

        if (livingPlayers.size() == 1) {
            ServerPlayerEntity winnerPlayer = livingPlayers.get(0);
            winnerPlayer.changeGameMode(GameMode.SPECTATOR);

            serverPlayers.forEach((sPlayer -> {
                sPlayer.teleport(centerPosition.x, centerPosition.y + 7, centerPosition.z);
                commandManager.executeWithPrefix(commandSource, gameEndTitleCommand.formatted(winnerPlayer.getName().getString()));
            }));

            world.playSound(null, BlockPos.ofFloored(centerPosition), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1f, 1f);

            resetWaitedTicks = true;
            itemSpawningStatus = false;
            isGameStarted = false;
        } else {
            world.playSound(null, BlockPos.ofFloored(centerPosition), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1f, 1f);
        }
    }
}
