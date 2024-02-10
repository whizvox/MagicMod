package me.whizvox.magicmod.common.lib;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.MagicUser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class MagicUserManager {

  private static final LevelResource MAGIC_USER_DATA_DIR = new LevelResource("magicuserdata");

  private static final Map<UUID, MagicUser> users = new HashMap<>();

  public static MagicUser getUser(UUID playerId) {
    return users.get(playerId);
  }

  public static MagicUser getUser(Player player) {
    return getUser(player.getUUID());
  }

  public static Stream<Map.Entry<UUID, MagicUser>> stream() {
    return users.entrySet().stream();
  }

  public static void forEach(BiConsumer<UUID, MagicUser> consumer) {
    users.forEach(consumer);
  }

  public static void save(Path outputDir) {
    users.forEach((playerId, magicUser) -> {
      Path userDataPath = outputDir.resolve(playerId.toString() + ".dat");
      CompoundTag nbt = magicUser.serializeNBT();
      try {
        NbtIo.write(nbt, userDataPath.toFile());
      } catch (IOException e) {
        MagicMod.LOGGER.warn("Could not save user data file " + userDataPath, e);
      }
    });
    MagicMod.LOGGER.info("Saved {} magic user(s)", users.size());
  }

  public static void register(IEventBus bus) {
    bus.addListener(MagicUserManager::onServerStarting);
    bus.addListener(MagicUserManager::onServerStopping);
    bus.addListener(MagicUserManager::onPlayerLoggedIn);
  }

  private static void loadMagicUserData(Path userDataPath) {
    String fileName = userDataPath.getFileName().toString();
    int extIndex = fileName.indexOf('.');
    if (extIndex != -1) {
      String playerIdStr = fileName.substring(0, extIndex);
      String fileExt = fileName.substring(extIndex);
      if (fileExt.equals(".dat")) {
        try {
          UUID playerId = UUID.fromString(playerIdStr);
          CompoundTag userDataTag = NbtIo.read(userDataPath.toFile());
          MagicUser magicUser = new MagicUser();
          magicUser.deserializeNBT(userDataTag);
          users.put(playerId, magicUser);
        } catch (IllegalArgumentException e) {
          MagicMod.LOGGER.warn("Invalid file name in magicuserdata directory: " + fileName);
        } catch (IOException e) {
          MagicMod.LOGGER.warn("Could not read user data file from magicuserdata directory", e);
        }
      }
    }
  }

  private static void onServerStarting(ServerStartingEvent event) {
    Path dataDir = event.getServer().getWorldPath(MAGIC_USER_DATA_DIR);
    try {
      Files.createDirectories(dataDir);
      users.clear();
      try (Stream<Path> walk = Files.walk(dataDir, 1)) {
        walk.forEach(MagicUserManager::loadMagicUserData);
      }
      MagicMod.LOGGER.info("Loaded {} magic user(s)", users.size());
    } catch (IOException e) {
      MagicMod.LOGGER.warn("Could not load magic user data", e);
    }
  }

  private static void onServerStopping(ServerStoppingEvent event) {
    Path dataDir = event.getServer().getWorldPath(MAGIC_USER_DATA_DIR);
    try {
      Files.createDirectories(dataDir);
      save(dataDir);
    } catch (IOException e) {
      MagicMod.LOGGER.warn("Could not save magic user data", e);
    }
  }

  private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    users.putIfAbsent(event.getEntity().getUUID(), new MagicUser());
    // TODO update client
  }

}
