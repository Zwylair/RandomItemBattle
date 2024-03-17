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
import net.minecraft.world.GameRules;
//import net.minecraft.sound.SoundCategory;
//import net.minecraft.sound.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import com.mojang.brigadier.arguments.IntegerArgumentType;


public class RandomItemBattle implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("randomitembattle");
	private static int TIMEOUT_IN_TICKS = 100;
	private static boolean itemSpawningStatus = false;
	private static boolean isGameStarted = false;
	private static boolean resetWaitedTicks = false;
	private static final String chatModPrefix = "§6[§eRandomItemBattle§6]§r ";
	private static final List<Item> blocksAsItems = new ArrayList<>(Registries.BLOCK.stream()
			.map(Block::asItem)
			.collect(Collectors.toList()));
	private static final List<Item> pickableItems = Stream.concat(blocksAsItems.stream(), Registries.ITEM.stream()).toList();
	private final List<Vec3d> playerPositions = new ArrayList<>();
	private static Vec3d centerPosition;

	private static ItemStack getRandomItemStack(World world){
		int randomInt = world.getRandom().nextInt(pickableItems.size());
		Item randomItem = pickableItems.get(randomInt);

		return new ItemStack(randomItem);
	}

	private static Vec3d roundPosition(Vec3d position) {
		double x = (int) position.getX();
		if (position.getX() < 0) { x -= 0.5; } else { x += 0.5; }
		double z = (int) position.getZ();
		if (position.getZ() < 0) { z -= 0.5; } else { z += 0.5; }

		return new Vec3d(x, position.getY(), z);
	}

	private static boolean isWorldNotConfigured(World world) {
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

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("rib")
			.then(literal("timeout").then(argument("seconds", IntegerArgumentType.integer())
				.executes(ctx -> {
					final int timeoutInSeconds = IntegerArgumentType.getInteger(ctx, "seconds");
					TIMEOUT_IN_TICKS = timeoutInSeconds * 20;

					LOGGER.info(String.format("Timeout set to %s (in seconds), to %s (in ticks)", timeoutInSeconds, TIMEOUT_IN_TICKS));
					ctx.getSource().sendFeedback(() -> Text.literal(String.format(chatModPrefix + "Timeout set to %s", timeoutInSeconds)), false);
					return 0;
				}))
			)
		));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("rib")
			.then(literal("start")
				.executes(ctx -> {
					if (isWorldNotConfigured(ctx.getSource().getWorld()) || centerPosition == null) {
						ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "§cYou have not configured this world! To be configured, you need:\n\n\tThe center position set (/rib set_center_pos)\n\tDone (/rib configure_world)"), false);
						return 1;
					}

					ServerWorld world = ctx.getSource().getWorld();
					MinecraftServer server = ctx.getSource().getServer();
					List<ServerPlayerEntity> players = world.getPlayers();

					if (playerPositions.isEmpty()) {
						ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "§cYou have not added any position to start"), false);
						return 1;
					}

					List<Vec3d> playerPositionsLeft = new ArrayList<>(playerPositions);

					if (world.getPlayers().size() > playerPositions.size()) {
						ctx.getSource().sendFeedback(() -> Text.literal(String.format(chatModPrefix + "§cThere are only %d positions for %d players", playerPositions.size(), players.size())), false);
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
					ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Game was started!"), false);
					return 0;
				})
			)
		));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("rib")
			.then(literal("items").then(literal("stop")
				.executes(ctx -> {
					itemSpawningStatus = false;

					ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Item spawning was stopped"), false);
					return 0;
				})
			))
		));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("rib")
			.then(literal("items").then(literal("resume")
				.executes(ctx -> {
					itemSpawningStatus = true;

					ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Item spawning was resumed"), false);
					return 0;
				})
			))
		));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("rib")
			.then(literal("startpos").then(literal("add")
				.executes(ctx -> {
					PlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) { return 1; }  // executor was not a player
					Vec3d spawnPosition = roundPosition(player.getPos());

					if (playerPositions.contains(spawnPosition)) {
						ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "§cThis position already added"), false);
						return 0;
					}
					playerPositions.add(spawnPosition);

					int x = (int) spawnPosition.x;
					int z = (int) spawnPosition.z;
					if (spawnPosition.x < 0) { x -= 1; }
					if (spawnPosition.z < 0) { z -= 1; }
					int finalX = x;
					int finalZ = z;

					ctx.getSource().sendFeedback(() -> Text.literal(String.format(chatModPrefix + "Position [x:%d, y:%d, z:%d] was added", finalX, (int) spawnPosition.y, finalZ)), false);
					return 0;
				})
			))
		));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("rib")
			.then(literal("startpos").then(literal("remove")
				.executes(ctx -> {
					PlayerEntity player = ctx.getSource().getPlayer();
					if (player == null) { return 1; }  // executor was not a player
					Vec3d spawnPosition = roundPosition(player.getPos());

					if (!playerPositions.contains(spawnPosition)) {
						ctx.getSource().sendFeedback(() -> Text.literal(String.format(chatModPrefix + "§cPosition [x:%d, y:%d, z:%d] is not in position list", (int) spawnPosition.x, (int) spawnPosition.y, (int) spawnPosition.z)), false);
						return 0;
					}
					playerPositions.add(spawnPosition);

					int x = (int) spawnPosition.x;
					int z = (int) spawnPosition.z;
					if (spawnPosition.x < 0) { x -= 1; }
					if (spawnPosition.z < 0) { z -= 1; }
					int finalX = x;
					int finalZ = z;

					ctx.getSource().sendFeedback(() -> Text.literal(String.format(chatModPrefix + "Position [x:%d, y:%d, z:%d] was removed", finalX, (int) spawnPosition.y, finalZ)), false);
					return 0;
				})
			))
		));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("rib")
			.then(literal("startpos").then(literal("clear")
				.executes(ctx -> {
					playerPositions.clear();

					ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Start positions were removed"), false);
					return 0;
				})
			))
		));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("rib")
			.then(literal("set_center_position")
				.executes(ctx -> {
					PlayerEntity player = ctx.getSource().getPlayer();
					if (player == null) { return 1; }  // executor was not a player
					centerPosition = roundPosition(player.getPos());

					ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "Center position was set"), false);
					return 0;
				})
			)
		));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("rib")
			.then(literal("configure_world")
				.executes(ctx -> {
					configureWorld(ctx.getSource().getWorld());
					ctx.getSource().sendFeedback(() -> Text.literal(chatModPrefix + "The world was configured"), false);
					return 0;
				})
			)
		));

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
