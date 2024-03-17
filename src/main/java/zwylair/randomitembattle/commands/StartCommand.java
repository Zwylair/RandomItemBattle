package zwylair.randomitembattle.commands;

import java.util.ArrayList;
import java.util.List;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import static net.minecraft.server.command.CommandManager.literal;
import static zwylair.randomitembattle.RandomItemBattle.*;
import static zwylair.randomitembattle.commands.ConfigureWorld.isWorldNotConfigured;


public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rib").then(literal("start")
                .executes(StartCommand::startGame)));
    }

    public static int startGame(CommandContext<ServerCommandSource> ctx) {
        if (isWorldNotConfigured(ctx.getSource().getWorld())) {
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

        ServerWorld world = ctx.getSource().getWorld();
        MinecraftServer server = ctx.getSource().getServer();
        List<ServerPlayerEntity> players = world.getPlayers();


        List<Vec3d> playerPositionsLeft = new ArrayList<>(playerPositions);

        if (world.getPlayers().size() > playerPositions.size()) {
            ctx.getSource().sendFeedback(() -> Text.literal(String.format(chatModPrefix + "§cThere are only %d positions for %d players!", playerPositions.size(), players.size())), false);
            return 1;
        }

        players.forEach((player) -> {
            int randomPositionIndex = world.getRandom().nextInt(playerPositionsLeft.size());
            Vec3d randomPosition = playerPositionsLeft.get(randomPositionIndex);

            player.getInventory().clear();
            player.changeGameMode(GameMode.SURVIVAL);
            player.getHungerManager().setFoodLevel(20);
            player.heal(20);
            player.teleport(randomPosition.x, randomPosition.y, randomPosition.z);

            playerPositionsLeft.remove(randomPositionIndex);
        });
        server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/kill @e[type=!player]");

        itemSpawningStatus = true;
        resetWaitedTicks = true;
        isGameStarted = true;
        server.sendMessage(Text.literal(chatModPrefix + "Game was started!"));
        return 0;
    }
}
