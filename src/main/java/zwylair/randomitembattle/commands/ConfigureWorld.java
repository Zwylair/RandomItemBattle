package zwylair.randomitembattle.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import static net.minecraft.server.command.CommandManager.literal;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import static zwylair.randomitembattle.RandomItemBattle.*;


public class ConfigureWorld {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rib").then(literal("configure_world")
                .executes(ConfigureWorld::configureWorld)));
    }

    public static boolean isWorldNotConfigured(World world) {
        GameRules gameRules = world.getGameRules();

        if (gameRules.get(GameRules.DO_MOB_SPAWNING).get()) { return true; }
        if (gameRules.get(GameRules.DO_PATROL_SPAWNING).get()) { return true; }
        if (gameRules.get(GameRules.DO_TRADER_SPAWNING).get()) { return true; }
        if (gameRules.get(GameRules.DO_WARDEN_SPAWNING).get()) { return true; }
        return !gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).get();
    }

    private static void configureWorld(World world) {
        GameRules gameRules = world.getGameRules();
        MinecraftServer server = world.getServer();

        gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, server);
        gameRules.get(GameRules.DO_PATROL_SPAWNING).set(false, server);
        gameRules.get(GameRules.DO_TRADER_SPAWNING).set(false, server);
        gameRules.get(GameRules.DO_WARDEN_SPAWNING).set(false, server);
        gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);
    }

    public static int configureWorld(CommandContext<ServerCommandSource> ctx) {
        configureWorld(ctx.getSource().getWorld());
        ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "The world was configured"), false);
        return 0;
    }
}
