package zwylair.randomitembattle;

import net.minecraft.resource.featuretoggle.FeatureFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.util.math.Vec3d;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;

import zwylair.randomitembattle.commands.*;
import zwylair.randomitembattle.serverevents.*;


public class RandomItemBattle implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("randomitembattle");
	public static int TIMEOUT_IN_TICKS = 100;
    public static boolean itemSpawningStatus = false;
    public static boolean isGameStarted = false;
    public static boolean resetWaitedTicks = false;
    public static final String chatModPrefix = "§6[§eRandomItemBattle§6]§r ";
	public static final String warningChatPrefix = "§5[§dWarning§5]§r ";
    public static final List<Item> blocksAsItems = new ArrayList<>(Registries.BLOCK.stream()
			.map(Block::asItem)
			.collect(Collectors.toList()));
	public static final List<String> restrictedItems = Arrays.stream(new String[] {
			"minecraft:command_block",
			"minecraft:chain_command_block",
			"minecraft:repeating_command_block",
			"minecraft:command_block_minecart",
			"minecraft:air",
			"minecraft:jigsaw",
			"minecraft:structure_block",
			"minecraft:structure_void",
			"minecraft:ender_dragon_spawn_egg",
			"minecraft:light_gray_stained_glass_pane",
			"minecraft:enchanted_book",
			"minecraft:sculk_shrieker"
	}).toList();
    public static List<Item> pickableItems = new ArrayList<>();
    public static List<Vec3d> playerPositions = new ArrayList<>();
    public static Vec3d centerPosition;

	@Override
	public void onInitialize() {
		pickableItems = Stream.concat(blocksAsItems.stream(), Registries.ITEM.stream()).toList();
		pickableItems.forEach((item) -> {
			if (!item.isEnabled(FeatureFlags.DEFAULT_ENABLED_FEATURES) || restrictedItems.contains(Registries.ITEM.getId(item).toString())) {
				pickableItems.remove(item);
			}
		});

		Tick.register();
		AfterRespawn.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TimeoutCommand.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> StartposCommands.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> GameManage.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ConfigureWorld.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CreatePlayground.register(dispatcher));

//		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("rib")
//			.then(literal("startpos").then(literal("list")
//				.executes(ctx -> {
//					playerPositions.clear();
//
//					ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Start positions were removed"), false);
//					return 0;
//				})
//			))
//		));
	}
}
