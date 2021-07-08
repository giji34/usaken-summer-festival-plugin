package com.github.giji34.usakensummerfestival;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

class PlayerShootingSession {
  class Arrow {
    final UUID uuid;
    Optional<Integer> hitTarget = Optional.empty();
    int score = 0;

    Arrow(UUID uuid) {
      this.uuid = uuid;
    }
  }

  enum CancelReason {
    SHOOT_FROM_OUTSIDE_OF_THE_SHOOTING_RANGE,
    SHOOT_BEFORE_HIT,
    HIT_ONE_TARGET_MULTIPLE_TIMES,
    TARGET_ALREADY_POWERED,
  }

  final ArrayList<Arrow> arrows = new ArrayList<>();

  PlayerShootingSession() {
  }

  enum ShootResult {
    OK,
    SHOOT_FROM_OUTSIDE_OF_THE_SHOOTING_RANGE,
    SHOOT_BEFORE_HIT,
  }

  ShootResult shoot(UUID uuid, Location location) {
    if (arrows.size() >= 30) {
      return ShootResult.OK;
    }
    if (!IsValidShootingRange(location)) {
      return ShootResult.SHOOT_FROM_OUTSIDE_OF_THE_SHOOTING_RANGE;
    }
    for (int i = 0; i < arrows.size(); i++) {
      Arrow arrow = arrows.get(i);
      if (!arrow.hitTarget.isPresent()) {
        return ShootResult.SHOOT_BEFORE_HIT;
      }
    }
    Arrow arrow = new Arrow(uuid);
    this.arrows.add(arrow);
    return ShootResult.OK;
  }

  enum HitResult {
    OK,
    TARGET_ALREADY_USED,
    SESSION_FINISHED,
  }

  HitResult hit(int targetIndex, UUID arrowUuid, Server server) {
    int idx = -1;
    for (int i = 0; i < arrows.size(); i++) {
      Arrow arrow = arrows.get(i);
      if (arrow.uuid.equals(arrowUuid)) {
        idx = i;
        break;
      }
    }
    if (idx < 0) {
      return HitResult.OK;
    }
    Arrow arrow = arrows.get(idx);
    arrow.hitTarget = Optional.of(targetIndex);

    boolean[] usedTarget = new boolean[]{false, false, false, false, false};
    Arrays.fill(usedTarget, false);
    int end = idx / 5;
    for (int i = 0; i < 5; i++) {
      int j = 5 * end + i;
      if (j >= arrows.size()) {
        break;
      }
      Optional<Integer> hit = arrows.get(j).hitTarget;
      if (!hit.isPresent()) {
        continue;
      }
      int hitIndex = hit.get();
      if (hitIndex < 0) {
        continue;
      }
      if (usedTarget[hitIndex]) {
        return HitResult.TARGET_ALREADY_USED;
      }
      usedTarget[hitIndex] = true;
    }
    if (arrows.size() < 30) {
      if (targetIndex < 0 && this.arrows.size() % 5 == 0) {
        killArrows(server, false);
      }
      return HitResult.OK;
    } else {
      for (int i = 0; i < arrows.size(); i++) {
        if (!arrows.get(i).hitTarget.isPresent()) {
          return HitResult.OK;
        }
      }
      return HitResult.SESSION_FINISHED;
    }
  }

  enum ScoreResult {
    WAITING_NEXT,
    SESSION_FINISHED,
  }

  ScoreResult score(int targetIndex, int score, Server server) {
    for (int i = 0; i < arrows.size(); i++) {
      Arrow arrow = arrows.get(i);
      if (!arrow.hitTarget.isPresent()) {
        continue;
      }
      if (arrow.hitTarget.get() != targetIndex) {
        continue;
      }
      if (arrow.score > 0) {
        continue;
      }
      arrow.score = score;
      break;
    }
    if (this.arrows.size() % 5 == 0) {
      killArrows(server, false);
    }
    if (this.arrows.size() < 30) {
      return ScoreResult.WAITING_NEXT;
    } else {
      for (int i = 0; i < arrows.size(); i++) {
        if (!arrows.get(i).hitTarget.isPresent()) {
          return ScoreResult.WAITING_NEXT;
        }
      }
      return ScoreResult.SESSION_FINISHED;
    }
  }

  void killArrows(Server server, boolean killFlyingArrows) {
    for (Arrow arrow : this.arrows) {
      if (!killFlyingArrows && !arrow.hitTarget.isPresent()) {
        continue;
      }
      Entity entity = server.getEntity(arrow.uuid);
      if (entity == null) {
        continue;
      }
      entity.remove();
    }
  }

  int totalScore() {
    int ret = 0;
    for (Arrow arrow : arrows) {
      ret += arrow.score;
    }
    return ret;
  }

  boolean isValidArrow(org.bukkit.entity.Arrow a) {
    for (Arrow arrow : arrows) {
      if (arrow.uuid.equals(a.getUniqueId())) {
        return true;
      }
    }
    return false;
  }

  String currentScoresMessage() {
    if (this.arrows.size() < 30) {
      return this.arrows.size() + " / 30 shots, current score: " + totalScore() + " points";
    } else {
      return "Result: " + totalScore() + " points";
    }
  }

  static boolean IsValidShootingRange(Location location) {
    World world = location.getWorld();
    if (world == null) {
      return false;
    }
    if (world.getEnvironment() != World.Environment.NORMAL) {
      return false;
    }
    double x = location.getX();
    double y = location.getY();
    double z = location.getZ();
    if (x < 77.5 || 88.5 < x) {
      return false;
    }
    if (y < 64 || 65.5 < y) {
      return false;
    }
    if (z < 143 || 160 < z) {
      return false;
    }
    return true;
  }
}
