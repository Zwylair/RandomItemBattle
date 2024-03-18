package zwylair.randomitembattle.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import static net.minecraft.server.command.CommandManager.literal;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static zwylair.randomitembattle.RandomItemBattle.*;
import static zwylair.randomitembattle.commands.StartCommand.posToInt;
import static zwylair.randomitembattle.commands.StartposCommands.roundPosition;


public class CreatePlayground {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rib").then(literal("create_playground")
                .executes(CreatePlayground::createPlayground)));
    }

    public static int createPlayground(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        assert player != null;  // executor was not a player
        MinecraftServer server = ctx.getSource().getServer();
        CommandManager commandManager = server.getCommandManager();
        ServerCommandSource commandSource = server.getCommandSource();

        centerPosition = roundPosition(player.getPos());
        Vec3d cPos = centerPosition;  // shortcut
        playerPositions.add(new Vec3d(cPos.x + 12, cPos.y + 8, cPos.z));
        playerPositions.add(new Vec3d(cPos.x - 12, cPos.y + 8, cPos.z));
        playerPositions.add(new Vec3d(cPos.x, cPos.y + 8, cPos.z + 12));
        playerPositions.add(new Vec3d(cPos.x, cPos.y + 8, cPos.z - 12));

        // clean center and every startpos
        commandManager.executeWithPrefix(commandSource, String.format("/fill %s %s %s %s %s %s air", posToInt(cPos.x) - 15, (int) cPos.y - 5, posToInt(cPos.z) - 15, posToInt(cPos.x) + 15, (int) cPos.y + 20, posToInt(cPos.z) + 15));
        playerPositions.forEach((pos) -> commandManager.executeWithPrefix(commandSource, String.format("/fill %s %s %s %s %s %s air", posToInt(pos.x) - 15, (int) pos.y - 5, posToInt(pos.z) - 15, posToInt(pos.x) + 15, (int) pos.y + 20, posToInt(pos.z) + 15)));

        // set bedrock blocks
        commandManager.executeWithPrefix(commandSource, String.format("/fill %s %s %s %s %s %s bedrock", posToInt(cPos.x) - 1, (int) cPos.y - 1, posToInt(cPos.z) - 1, posToInt(cPos.x) + 1, (int) cPos.y - 1, posToInt(cPos.z) + 1));
        playerPositions.forEach((pos) -> commandManager.executeWithPrefix(commandSource, String.format("/setblock %s %s %s bedrock", posToInt(pos.x), (int) pos.y - 1, posToInt(pos.z))));

        ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Playground was created"), false);
        return 0;
    }
}