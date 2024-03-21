package zwylair.randomitembattle.commands;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import static net.minecraft.server.command.CommandManager.literal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import static zwylair.randomitembattle.RandomItemBattle.*;
import static zwylair.randomitembattle.commands.ConfigureWorld.isWorldNotConfigured;
import static zwylair.randomitembattle.utils.PosCalculator.posToCoord;


public class GameManage {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rib").then(literal("game").then(literal("start")
                .executes(GameManage::startGame))));
        dispatcher.register(literal("rib").then(literal("game").then(literal("stop")
                .executes(GameManage::stopGame))));

        dispatcher.register(literal("rib").then(literal("items").then(literal("pause")
                .executes(GameManage::pauseItemSpawning))));
        dispatcher.register(literal("rib").then(literal("items").then(literal("resume")
                .executes(GameManage::resumeItemSpawning))));
    }
    
    public static int startGame(CommandContext<ServerCommandSource> ctx) {
        ServerWorld world = ctx.getSource().getWorld();
        List<ServerPlayerEntity> players = world.getPlayers();

        if (isWorldNotConfigured(world)) { ConfigureWorld.configureWorld(ctx); }
        if (centerPosition == null) {
            ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "§cThe center position is not configured! Configure the center position with (/rib set_center_pos)"), false);
            return 1;
        }
        if (playerPositions.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "§cThere is no one position to start with!"), false);
            return 1;
        }
        if (world.getPlayers().size() > playerPositions.size()) {
            ctx.getSource().sendFeedback(() -> Text.literal(String.format(chatModPrefix + "§cThere are only %d positions for %d players!", playerPositions.size(), players.size())), false);
            return 1;
        }

        // shortcuts
        Vec3d cPos = centerPosition;
        MinecraftServer server = ctx.getSource().getServer();
        CommandManager commandManager = server.getCommandManager();
        ServerCommandSource commandSource = server.getCommandSource();
        List<Vec3d> playerPositionsLeft = new ArrayList<>(playerPositions);

        // clean and place procedures in separate forEach because the server chaotic
        // executes commands, so we need some time interval before teleporting players

        // clean center and every startpos
        commandManager.executeWithPrefix(commandSource, String.format("/fill %s %s %s %s %s %s air", posToCoord(cPos.x) - 15, (int) cPos.y - 5, posToCoord(cPos.z) - 15, posToCoord(cPos.x) + 15, (int) cPos.y + 20, posToCoord(cPos.z) + 15));
        playerPositionsLeft.forEach((pos) -> commandManager.executeWithPrefix(commandSource, String.format("/fill %s %s %s %s %s %s air", posToCoord(pos.x) - 15, (int) pos.y - 5, posToCoord(pos.z) - 15, posToCoord(pos.x) + 15, (int) pos.y + 20, posToCoord(pos.z) + 15)));

        // set bedrock blocks
        commandManager.executeWithPrefix(commandSource, String.format("/fill %s %s %s %s %s %s bedrock", posToCoord(cPos.x) - 1, (int) cPos.y - 1, posToCoord(cPos.z) - 1, posToCoord(cPos.x) + 1, (int) cPos.y - 1, posToCoord(cPos.z) + 1));
        playerPositionsLeft.forEach((pos) -> commandManager.executeWithPrefix(commandSource, String.format("/setblock %s %s %s bedrock", posToCoord(pos.x), (int) pos.y - 1, posToCoord(pos.z))));

        commandManager.executeWithPrefix(commandSource, "/kill @e[type=!player]");

        // prepare players (also interval)
        players.forEach((player) -> {
            player.getInventory().clear();
            player.changeGameMode(GameMode.SURVIVAL);
            player.getHungerManager().setFoodLevel(20);
            player.heal(20);
        });

        // teleport players to random startpos
        players.forEach((player) -> {
            int randomPositionIndex = world.getRandom().nextInt(playerPositionsLeft.size());
            Vec3d randomPosition = playerPositionsLeft.get(randomPositionIndex);

            player.teleport(randomPosition.x, randomPosition.y, randomPosition.z);
            playerPositionsLeft.remove(randomPositionIndex);
        });

        itemSpawningStatus = true;
        resetWaitedTicks = true;
        isGameStarted = true;
        return 0;
    }

    public static int stopGame(CommandContext<ServerCommandSource> ctx) {
        if (!isGameStarted) {
            ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "§cThe game is not started!"), false);
            return 1;
        }

        ctx.getSource().getWorld().getPlayers().forEach((player) -> {
            player.changeGameMode(GameMode.CREATIVE);
            player.getHungerManager().setFoodLevel(20);
            player.heal(20);
        });

        itemSpawningStatus = false;
        resetWaitedTicks = true;
        isGameStarted = false;
        return 0;
    }

    public static int pauseItemSpawning(CommandContext<ServerCommandSource> ctx) {
        itemSpawningStatus = false;

        ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Item spawning was paused"), false);
        return 0;
    }

    public static int resumeItemSpawning(CommandContext<ServerCommandSource> ctx) {
        itemSpawningStatus = true;

        ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Item spawning was resumed"), false);
        return 0;
    }
}
