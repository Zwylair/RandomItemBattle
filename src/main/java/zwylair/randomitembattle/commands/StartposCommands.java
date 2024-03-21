package zwylair.randomitembattle.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import static net.minecraft.server.command.CommandManager.literal;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static zwylair.randomitembattle.RandomItemBattle.*;
import static zwylair.randomitembattle.utils.PosCalculator.roundPosition;


public class StartposCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rib").then(literal("startpos").then(literal("add")
                .executes(StartposCommands::addStartpos))));

        dispatcher.register(literal("rib").then(literal("startpos").then(literal("remove")
                .executes(StartposCommands::removeStartpos))));

        dispatcher.register(literal("rib").then(literal("startpos").then(literal("clear")
                .executes(StartposCommands::removeAllStartpos))));

        dispatcher.register(literal("rib").then(literal("set_center_position")
                .executes(StartposCommands::setCenterPos)));
    }

    public static int addStartpos(CommandContext<ServerCommandSource> ctx) {
        PlayerEntity player = ctx.getSource().getPlayer();
        assert player != null;  // executor was not a player
        Vec3d spawnPosition = roundPosition(player.getPos());

        if (playerPositions.contains(spawnPosition)) {
            ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "§cThis position already added"), false);
            return 1;
        }
        playerPositions.add(spawnPosition);

        int x = (int) spawnPosition.x;
        int z = (int) spawnPosition.z;
        if (spawnPosition.x < 0) { x -= 1; }
        if (spawnPosition.z < 0) { z -= 1; }
        int finalX = x;
        int finalZ = z;

        ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Position [x:%d, y:%d, z:%d] was added".formatted(finalX, (int) spawnPosition.y, finalZ)), false);
        return 0;
    }

    public static int removeStartpos(CommandContext<ServerCommandSource> ctx) {
        PlayerEntity player = ctx.getSource().getPlayer();
        assert player != null;  // executor was not a player
        Vec3d spawnPosition = roundPosition(player.getPos());

        if (!playerPositions.contains(spawnPosition)) {
            ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "§cPosition [x:%d, y:%d, z:%d] is not in position list".formatted((int) spawnPosition.x, (int) spawnPosition.y, (int) spawnPosition.z)), false);
            return 1;
        }
        playerPositions.remove(spawnPosition);

        int x = (int) spawnPosition.x;
        int z = (int) spawnPosition.z;
        if (spawnPosition.x < 0) { x -= 1; }
        if (spawnPosition.z < 0) { z -= 1; }
        int finalX = x;
        int finalZ = z;

        ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Position [x:%d, y:%d, z:%d] was removed".formatted(finalX, (int) spawnPosition.y, finalZ)), false);
        return 0;
    }

    public static int removeAllStartpos(CommandContext<ServerCommandSource> ctx) {
        playerPositions.clear();

        ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Start positions were removed"), false);
        return 0;
    }

    public static int setCenterPos(CommandContext<ServerCommandSource> ctx) {
        PlayerEntity player = ctx.getSource().getPlayer();
        assert player != null;  // executor was not a player
        centerPosition = roundPosition(player.getPos());

        ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Center position was set"), false);
        return 0;
    }
}
