package com.github.giji34.usakensummerfestival;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class BowShooting implements Listener {
  final HashMap<UUID, PlayerShootingSession> sessions = new HashMap<>();
  final JavaPlugin owner;
  ScoreboardVisibility scoreboardVisibility;

  public static final String scoreboardName = "bow_shooting";

  BowShooting(JavaPlugin owner) {
    this.owner = owner;
    this.scoreboardVisibility = new ScoreboardVisibility(owner, scoreboardName);
  }

  @EventHandler
  public void onEntityShootBowEvent(EntityShootBowEvent e) {
    Entity entity = e.getProjectile();
    if (!(entity instanceof Arrow)) {
      return;
    }
    Arrow arrow = (Arrow) entity;

    LivingEntity shooterEntity = e.getEntity();
    if (!(shooterEntity instanceof Player)) {
      return;
    }
    this.scoreboardVisibility.makeVisible();
    Player shooter = (Player) shooterEntity;
    Location location = shooter.getLocation();
    PlayerShootingSession session = null;
    if (sessions.containsKey(shooter.getUniqueId())) {
      session = sessions.get(shooter.getUniqueId());
    }
    if (session == null) {
      if (!PlayerShootingSession.IsValidShootingRangeFirst(location)) {
        return;
      }
      session = new PlayerShootingSession(shooter);
      sessions.put(shooter.getUniqueId(), session);
    } else if (!session.isInShootingRange(location)) {
      arrow.remove();
      this.cancelSession(shooter, PlayerShootingSession.CancelReason.SHOOT_FROM_OUTSIDE_OF_THE_SHOOTING_RANGE);
      return;
    }
    if (session.shoot(arrow.getUniqueId()) == 2) {
      shooter.sendMessage(ChatColor.YELLOW + "Change your shooting position. 3rd ~ 5th shot must be done behind the white line");
    }
  }

  void finishSession(Player player, PlayerShootingSession session) {
    int resultScore = session.totalScore();
    player.sendMessage("Shooting session finished!: Your score is " + session.currentScoresMessage());
    session.killArrows(owner.getServer());
    sessions.remove(player.getUniqueId());

    Scoreboard scoreboard = player.getScoreboard();
    Objective bowShooting = scoreboard.getObjective(scoreboardName);
    if (bowShooting == null) {
      scoreboard.registerNewObjective(scoreboardName, "dummy", "Bow shooting scores");
      bowShooting = scoreboard.getObjective(scoreboardName);
    }
    String name = player.getName();
    Score score = bowShooting.getScore(name);
    if (score.getScore() == 0 || score.getScore() < resultScore) {
      player.sendMessage(ChatColor.AQUA + "Congratulation! Your new record!");
      score.setScore(resultScore);
    }
  }

  void cancelSession(Player player, PlayerShootingSession.CancelReason reason) {
    if (sessions.containsKey(player.getUniqueId())) {
      PlayerShootingSession session = sessions.get(player.getUniqueId());
      session.killArrows(owner.getServer());
    }
    sessions.remove(player.getUniqueId());
    switch (reason) {
      case SHOOT_FROM_OUTSIDE_OF_THE_SHOOTING_RANGE:
        player.sendMessage(ChatColor.RED + "Shooting session canceled: shoot from outside of the shooting ranges");
        break;
      case HIT_ONE_TARGET_MULTIPLE_TIMES:
        player.sendMessage(ChatColor.RED + "Shooting session canceled: shoot one target multiple times");
        break;
    }
  }

  @EventHandler
  public void onProjectileHit(ProjectileHitEvent e) {
    Projectile projectile = e.getEntity();
    if (!(projectile instanceof Arrow)) {
      return;
    }
    Arrow arrow = (Arrow)projectile;

    ProjectileSource source = projectile.getShooter();
    if (!(source instanceof Player)) {
      return;
    }
    final Player player = (Player) source;
    if (!sessions.containsKey(player.getUniqueId())) {
      return;
    }

    final PlayerShootingSession session = sessions.get(player.getUniqueId());

    if (!session.isValidArrow(arrow)) {
      return;
    }
    Block block = e.getHitBlock();
    int hitTarget = TargetIndex(block);
    if (hitTarget > -1) {
      Optional<String> power = GetProperty(block.getBlockData(), "power");
      if (power.isPresent() && !power.get().equals("0")) {
        this.cancelSession(player, PlayerShootingSession.CancelReason.TARGET_ALREADY_POWERED);
        return;
      }
    }
    switch (session.hit(hitTarget, arrow.getUniqueId())) {
      case OK:
        break;
      case TARGET_ALREADY_USED:
        this.cancelSession(player, PlayerShootingSession.CancelReason.HIT_ONE_TARGET_MULTIPLE_TIMES);
        return;
      case SESSION_FINISHED:
        this.finishSession(player, session);
        return;
    }
    if (hitTarget == -1) {
      player.sendMessage(ChatColor.GRAY + "Miss! Current score: " + session.currentScoresMessage());
      return;
    }
    final World world = block.getWorld();
    int x = block.getX();
    int y = block.getY();
    int z = block.getZ();
    owner.getServer().getScheduler().runTaskLater(owner, () -> {
      Block b = world.getBlockAt(x, y, z);
      BlockData data = b.getBlockData();
      int score = 0;
      Optional<String> power = GetProperty(data, "power");
      if (power.isPresent()) {
        try {
          score = Integer.parseInt(power.get(), 10);
        } catch (Exception ex) {
        }
      }
      switch (session.score(hitTarget, score)) {
        case WAITING_NEXT:
          player.sendMessage(ChatColor.GRAY + "Hit! Current score: " + session.currentScoresMessage());
          break;
        case SESSION_FINISHED:
          this.finishSession(player, session);
          break;
      }
    }, 1);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent e) {
    Player player = e.getPlayer();
    sessions.remove(player.getUniqueId());
  }

  private static Optional<String> GetProperty(BlockData data, String name) {
    String s = data.getAsString(false);
    int begin = s.lastIndexOf("[");
    if (begin < 0) {
      return Optional.empty();
    }
    int end = s.indexOf("]", begin + 1);
    if (end < 0) {
      return Optional.empty();
    }
    String props = s.substring(begin + 1, end);
    String[] tokens = props.split(",");
    for (String token : tokens) {
      String[] kv = token.split("=");
      if (kv.length != 2) {
        continue;
      }
      if (!kv[0].equals(name)) {
        continue;
      }
      return Optional.of(kv[1]);
    }
    return Optional.empty();
  }

  private static int TargetIndex(Block block) {
    if (block.getBlockData().getMaterial() != Material.TARGET) {
      return -1;
    }
    int x = block.getX();
    if (x != 108) {
      return -1;
    }
    int y = block.getY();
    int z = block.getZ();
    if (y == 67 && z == 147) {
      return 0;
    } else if (y == 65 && z == 149) {
      return 1;
    } else if (y == 66 && z == 151) {
      return 2;
    } else if (y == 65 && z == 153) {
      return 3;
    } else if (y == 67 && z == 155) {
      return 4;
    }
    return -1;
  }
}

