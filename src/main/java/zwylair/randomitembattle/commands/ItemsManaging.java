package zwylair.randomitembattle.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;
import static net.minecraft.server.command.CommandManager.literal;
import static zwylair.randomitembattle.RandomItemBattle.*;


public class ItemsManaging {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rib").then(literal("items").then(literal("stop")
                .executes(ItemsManaging::stopItemSpawning))));

        dispatcher.register(literal("rib").then(literal("items").then(literal("resume")
                .executes(ItemsManaging::resumeItemSpawning))));
    }

    public static int stopItemSpawning(CommandContext<ServerCommandSource> ctx) {
        itemSpawningStatus = false;

        ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Item spawning was stopped"), false);
        return 0;
    }

    public static int resumeItemSpawning(CommandContext<ServerCommandSource> ctx) {
        itemSpawningStatus = true;

        ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Item spawning was resumed"), false);
        return 0;
    }
}
