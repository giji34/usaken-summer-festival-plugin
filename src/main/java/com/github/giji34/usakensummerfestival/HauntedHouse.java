package com.github.giji34.usakensummerfestival;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class HauntedHouse implements Listener {
  final JavaPlugin owner;
  final HashMap<UUID, PlayerHauntedHouseSession> sessions = new HashMap<>();
  boolean leftComparatorPowered = false;
  boolean rightComparatorPowered = false;

  HauntedHouse(JavaPlugin owner) {
    this.owner = owner;
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent e) {
    Player player = e.getPlayer();
    Location location = player.getLocation();
    World world = location.getWorld();
    if (world == null) {
      return;
    }
    if (world.getEnvironment() != World.Environment.NORMAL) {
      return;
    }
    double x = location.getX();
    double y = location.getY();
    double z = location.getZ();
    if (104 <= x && x < 115 && 67 <= y && y < 69 && 20 <= z && z < 23) {
      if (!sessions.containsKey(player.getUniqueId())) {
        PlayerHauntedHouseSession session = new PlayerHauntedHouseSession(player);
        sessions.put(player.getUniqueId(), session);
      }
    } else if (95 <= x && x < 103 && 67 <= y && y < 69 && 25 <= z && z < 28) {
      if (sessions.containsKey(player.getUniqueId())) {
        PlayerHauntedHouseSession session = sessions.get(player.getUniqueId());
        sessions.remove(player.getUniqueId());
        session.close(player);
      }
    }
  }

  @EventHandler
  public void onBlockPhysics(BlockPhysicsEvent e) {
    Block block = e.getBlock();
    World world = block.getWorld();
    if (world.getEnvironment() != World.Environment.NORMAL) {
      return;
    }
    Location location = block.getLocation();
    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();
    if (x != 149) {
      return;
    }
    if (y != 46) {
      return;
    }
    if (z != 32 && z != 33) {
      return;
    }
    BlockData data = block.getBlockData();
    if (!(data instanceof Powerable)) {
      return;
    }
    boolean powered = ((Powerable) data).isPowered();
    boolean prev = leftComparatorPowered || rightComparatorPowered;
    if (z == 32) {
      leftComparatorPowered = powered;
    } else {
      rightComparatorPowered = powered;
    }
    boolean next = leftComparatorPowered || rightComparatorPowered;
    if (prev == next) {
      return;
    }
    if (next) {
      return;
    }
    owner.getServer().getScheduler().runTaskLater(owner, () -> {
      resetTrappedChest(world);
    }, 20);
  }

  private void resetTrappedChest(World world) {
    Block left = world.getBlockAt(148, 46, 32);
    Block right = world.getBlockAt(148, 46, 33);
    BlockState leftBlockState = left.getState();
    BlockState rightBlockState = right.getState();
    if (!(leftBlockState instanceof InventoryHolder)) {
      return;
    }
    if (!(rightBlockState instanceof InventoryHolder)) {
      return;
    }
    ((InventoryHolder) leftBlockState).getInventory().clear();
    Inventory inventory = ((InventoryHolder) rightBlockState).getInventory();
    inventory.clear();
    inventory.setItem(4, new ItemStack(Material.GRAY_CANDLE));
  }
}
