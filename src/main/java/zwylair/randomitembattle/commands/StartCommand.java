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


public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rib").then(literal("start")
                .executes(StartCommand::startGame)));
    }

    private static int posToInt(double number) {
        if (number < 0) { return (int) ((int) number - 0.5); } else { return (int) number; }
    }

    public static int startGame(CommandContext<ServerCommandSource> ctx) {
        ServerWorld world = ctx.getSource().getWorld();
        List<ServerPlayerEntity> players = world.getPlayers();

        if (isWorldNotConfigured(world)) {
            ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "§cThis world need to be configured! Enter (/rib configure_world) to prepare the world to game"), false);
            return 1;
        }
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
        commandManager.executeWithPrefix(commandSource, String.format("/fill %s %s %s %s %s %s air", posToInt(cPos.x) - 15, (int) cPos.y - 5, posToInt(cPos.z) - 15, posToInt(cPos.x) + 15, (int) cPos.y + 20, posToInt(cPos.z) + 15));
        playerPositionsLeft.forEach((pos) -> commandManager.executeWithPrefix(commandSource, String.format("/fill %s %s %s %s %s %s air", posToInt(pos.x) - 15, (int) pos.y - 5, posToInt(pos.z) - 15, posToInt(pos.x) + 15, (int) pos.y + 20, posToInt(pos.z) + 15)));

        // set bedrock blocks
        commandManager.executeWithPrefix(commandSource, String.format("/fill %s %s %s %s %s %s bedrock", posToInt(cPos.x) - 1, (int) cPos.y - 1, posToInt(cPos.z) - 1, posToInt(cPos.x) + 1, (int) cPos.y - 1, posToInt(cPos.z) + 1));
        playerPositionsLeft.forEach((pos) -> commandManager.executeWithPrefix(commandSource, String.format("/setblock %s %s %s bedrock", posToInt(pos.x), (int) pos.y - 1, posToInt(pos.z))));

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
}
