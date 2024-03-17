package zwylair.randomitembattle;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.text.Texts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.GameMode;
//import net.minecraft.sound.SoundCategory;
//import net.minecraft.sound.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import zwylair.randomitembattle.commands.*;


public class RandomItemBattle implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("randomitembattle");
	public static int TIMEOUT_IN_TICKS = 100;
    public static boolean itemSpawningStatus = false;
    public static boolean isGameStarted = false;
    public static boolean resetWaitedTicks = false;
    public static final String chatModPrefix = "§6[§eRandomItemBattle§6]§r ";
    public static final List<Item> blocksAsItems = new ArrayList<>(Registries.BLOCK.stream()
			.map(Block::asItem)
			.collect(Collectors.toList()));
    public static final List<Item> pickableItems = Stream.concat(blocksAsItems.stream(), Registries.ITEM.stream()).toList();
    public static final List<Vec3d> playerPositions = new ArrayList<>();
    public static Vec3d centerPosition;

	private static ItemStack getRandomItemStack(World world){
		int randomInt = world.getRandom().nextInt(pickableItems.size());
		Item randomItem = pickableItems.get(randomInt);

		return new ItemStack(randomItem);
	}

	@Override
	public void onInitialize() {
		AtomicInteger ticksCount = new AtomicInteger(0);

		ServerTickEvents.START_WORLD_TICK.register((server) -> {
			if (!itemSpawningStatus) {
				return;
			}

			if (resetWaitedTicks) {
				ticksCount.set(1);
				resetWaitedTicks = false;
			}

			if (ticksCount.get() < TIMEOUT_IN_TICKS) {
				ticksCount.incrementAndGet();
			} else {
				server.getPlayers().forEach((player) -> player.getInventory().insertStack(getRandomItemStack(player.getWorld())));
				ticksCount.set(0);
			}
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) -> {
			if (!isGameStarted) {
				return;
			}

			newPlayer.changeGameMode(GameMode.SPECTATOR);
			newPlayer.teleport(centerPosition.x, centerPosition.y + 7, centerPosition.z);

			List<ServerPlayerEntity> livingPlayers = new ArrayList<>();
			List<ServerPlayerEntity> serverPlayers = Objects.requireNonNull(oldPlayer.getServer()).getPlayerManager().getPlayerList();

			serverPlayers.forEach((sPlayer) -> {
				if (sPlayer.isAlive()) { livingPlayers.add(sPlayer); }
			});

			if (livingPlayers.size() == 1) {
				ServerPlayerEntity winnerPlayer = livingPlayers.get(0);
				winnerPlayer.changeGameMode(GameMode.SPECTATOR);
//				winnerPlayer.getWorld().playSound(winnerPlayer, winnerPlayer.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1f, 1f);

				serverPlayers.forEach((sPlayer -> {
					sPlayer.teleport(centerPosition.x, centerPosition.y + 7, centerPosition.z);
                    try {
                        sPlayer.sendMessageToClient(Texts.parse(sPlayer.getCommandSource(), Text.of(winnerPlayer.getName().toString() + " win!"), sPlayer, 0), true);
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }));

				resetWaitedTicks = true;
				itemSpawningStatus = false;
				isGameStarted = false;
			}
		});

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TimeoutCommand.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> StartposCommands.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ItemsManaging.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ConfigureWorld.register(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> StartCommand.register(dispatcher));

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
