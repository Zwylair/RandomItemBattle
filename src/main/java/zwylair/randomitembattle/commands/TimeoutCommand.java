package zwylair.randomitembattle.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import static zwylair.randomitembattle.RandomItemBattle.*;


public class TimeoutCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rib").then(literal("timeout").then(argument("seconds", IntegerArgumentType.integer())
                .executes(TimeoutCommand::execute))));
    }

    public static int execute(CommandContext<ServerCommandSource> ctx) {
        final int timeoutInSeconds = IntegerArgumentType.getInteger(ctx, "seconds");
        TIMEOUT_IN_TICKS = timeoutInSeconds * 20;

        ctx.getSource().sendFeedback(() -> Text.literal(String.format(chatModPrefix + "Timeout set to %s", timeoutInSeconds)), false);
        return 0;
    }
}
