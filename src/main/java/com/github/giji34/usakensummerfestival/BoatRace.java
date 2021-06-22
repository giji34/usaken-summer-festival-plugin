package com.github.giji34.usakensummerfestival;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

class BoatRace implements Listener {
  final HashMap<UUID, PlayerBoatRaceRun> boatRaceRuns = new HashMap<>();
  final Logger logger;

  static final String scoreboardName = "boat_race";

  BoatRace(Logger logger) {
    this.logger = logger;
  }

  @EventHandler
  public void onVehicleEnter(VehicleEnterEvent e) {
    Entity entered = e.getEntered();
    if (!(entered instanceof Player)) {
      return;
    }
    Player enteredPlayer = (Player)entered;
    Vehicle vehicle = e.getVehicle();
    UUID vehicleUUid = vehicle.getUniqueId();
    if (boatRaceRuns.containsKey(enteredPlayer.getUniqueId())) {
      PlayerBoatRaceRun run = boatRaceRuns.get(enteredPlayer.getUniqueId());
      if (run.vehicleUUid.equals(vehicleUUid)) {
        return;
      }
      this.cancelBoatRaceRun(enteredPlayer, BoatRaceCancelReason.CHANGED_VEHICLE);
      return;
    }
    Location location = enteredPlayer.getLocation();
    if (!IsValidStartLocation(location)) {
      System.out.println("is not valid location");
      return;
    }
    if (!(vehicle instanceof Boat)) {
      System.out.println("vehicle is not a boat");
      return;
    }
    PlayerBoatRaceRun run = new PlayerBoatRaceRun(vehicleUUid, new TimedLocation(enteredPlayer));
    boatRaceRuns.put(enteredPlayer.getUniqueId(), run);
    enteredPlayer.sendMessage(ChatColor.GRAY + "You did Get. You can Set and Go at any time you like!");
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent e) {
    Player player = e.getPlayer();
    Entity vehicleEntity = player.getVehicle();
    if (!(vehicleEntity instanceof Boat)) {
      return;
    }
    if ((!boatRaceRuns.containsKey(player.getUniqueId()))) {
      return;
    }
    PlayerBoatRaceRun run = boatRaceRuns.get(player.getUniqueId());
    TimedLocation tl = new TimedLocation(player);

    double y = tl.location.getY();
    if (y != 67 && y < 68) {
      cancelBoatRaceRun(player, BoatRaceCancelReason.COURSE_OUT);
      return;
    }

    PlayerBoatRaceRun.UpdateResult result = run.update(tl);
    switch (result) {
      case STARTED:
        player.sendMessage(ChatColor.GRAY + "Started!");
        break;
      case COURSE_OUT:
        this.cancelBoatRaceRun(player, BoatRaceCancelReason.COURSE_OUT);
        break;
      case GOALED:
        int recordMillis = (int)(run.goalTimeMillis - run.startTimeMillis);
        player.sendMessage(ChatColor.AQUA + "Goal!" + ChatColor.GRAY + " Your record is " + ChatColor.AQUA + recordMillis + ChatColor.GRAY + " milli seconds");
        Scoreboard scoreboard = player.getScoreboard();
        Objective boatRace = scoreboard.getObjective(scoreboardName);
        if (boatRace == null) {
          scoreboard.registerNewObjective(scoreboardName, "dummy", "Boat race records (milli seconds)");
          boatRace = scoreboard.getObjective(scoreboardName);
        }
        String name = player.getName();
        Score score = boatRace.getScore(name);
        if (score.getScore() == 0 || score.getScore() > recordMillis) {
          player.sendMessage(ChatColor.AQUA + "Congratulation! Your new record!");
          score.setScore(recordMillis);
        }
        boatRace.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.boatRaceRuns.remove(player.getUniqueId());
        break;
    }
  }

  @EventHandler
  public void onVehicleExit(VehicleExitEvent e) {
    LivingEntity entity = e.getExited();
    if (!(entity instanceof Player)) {
      return;
    }
    Player exitedPlayer = (Player) entity;
    if (!boatRaceRuns.containsKey(exitedPlayer.getUniqueId())) {
      return;
    }
    cancelBoatRaceRun(exitedPlayer, BoatRaceCancelReason.EXITED_BOAT);
  }

  @EventHandler
  public void onPlayerLeave(PlayerQuitEvent e) {
    Player player = e.getPlayer();
    if (!boatRaceRuns.containsKey(player.getUniqueId())) {
      return;
    }
    cancelBoatRaceRun(player, BoatRaceCancelReason.QUIT);
  }

  private void cancelBoatRaceRun(Player player, BoatRaceCancelReason reason) {
    this.boatRaceRuns.remove(player.getUniqueId());
    String message = null;
    switch (reason) {
      case CHANGED_VEHICLE:
        message = "Vehicle changed";
        break;
      case EXITED_BOAT:
        message = "Exited from boat";
        break;
      case QUIT:
        message = null;
        break;
      case COURSE_OUT:
        message = "Course out";
        break;
      default:
        message = "Unknown";
        break;
    }
    if (message != null) {
      player.sendMessage(ChatColor.GRAY + "Cancelled your boat race run. Reason: " + message);
    }
  }

  private static boolean IsValidStartLocation(Location loc) {
    World world = loc.getWorld();
    if (world == null) {
      System.out.println("world is null");
      return false;
    }
    if (world.getEnvironment() != World.Environment.NORMAL) {
      System.out.println("is not overworld");
      return false;
    }
    double x = loc.getX();
    double y = loc.getY();
    double z = loc.getZ();
    if (y != 67 && y != 68) {
      System.out.println("invalid y:" + loc.getY());
      return false;
    }
    if (z >= 49) {
      return false;
    }
    return 76 <= x && x <= 82;
  }

  private static boolean AlmostEquals(double actual, double expected) {
    return (expected - 1.0 / 20.0 <= actual) && (actual <= expected + 1.0 / 20.0);
  }
}
