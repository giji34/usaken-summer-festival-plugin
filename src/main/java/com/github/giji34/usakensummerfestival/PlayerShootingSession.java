package com.github.giji34.usakensummerfestival;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class PlayerShootingSession {
  final Player player;

  enum CancelReason {
    SHOOT_FROM_OUTSIDE_OF_THE_SHOOTING_RANGE,
    HIT_ONE_TARGET_MULTIPLE_TIMES,
    TARGET_ALREADY_POWERED,
  }

  class Hit {
    // [-1, 4]
    // -1: miss
    // 0: most north target block
    final int targetIndex;
    // [-1, 15]
    // -1: arrow does not hit yet
    int score = -1;
    final UUID arrowUuid;

    Hit(UUID arrowUuid, int targetIndex) {
      this.arrowUuid = arrowUuid;
      this.targetIndex = targetIndex;
      if (targetIndex < 0) {
        this.score = 0;
      }
    }
  }

  PlayerShootingSession(Player player) {
    this.player = player;
  }

  private ArrayList<Hit> scores = new ArrayList<>();
  private HashSet<UUID> arrows = new HashSet<>();

  boolean isInShootingRange(Location location) {
    if (arrows.size() <= 1) {
      return IsValidShootingRangeFirst(location);
    } else if (arrows.size() <= 4) {
      return IsValidShootingRangeSecond(location);
    } else {
      return false;
    }
  }

  enum HitResult {
    OK,
    TARGET_ALREADY_USED,
    SESSION_FINISHED,
  }

  HitResult hit(int targetIndex, UUID arrowUuid) {
    if (!this.arrows.contains(arrowUuid)) {
      // just ignore
      return HitResult.OK;
    }

    if (targetIndex > -1) {
      for (int i = 0; i < this.scores.size(); i++) {
        if (this.scores.get(i).targetIndex == targetIndex) {
          return HitResult.TARGET_ALREADY_USED;
        }
      }
    }
    if (this.scores.size() >= 5) {
      return HitResult.SESSION_FINISHED;
    }
    Hit hit = new Hit(arrowUuid, targetIndex);
    this.scores.add(hit);
    if (this.scores.size() == 5 && targetIndex < 0) {
      return HitResult.SESSION_FINISHED;
    } else {
      return HitResult.OK;
    }
  }

  enum ScoreResult {
    WAITING_NEXT,
    SESSION_FINISHED,
  }

  ScoreResult score(int targetIndex, int score) {
    assert targetIndex >= 0;
    for (int i = 0; i < this.scores.size(); i++) {
      Hit hit = this.scores.get(i);
      if (hit.targetIndex != targetIndex) {
        continue;
      }
      if (hit.score < 0) {
        hit.score = score;
      }
      break;
    }
    if (this.scores.size() < 5) {
      return ScoreResult.WAITING_NEXT;
    } else {
      for (int i = 0; i < this.scores.size(); i++) {
        if (this.scores.get(i).score < 0) {
          // some arrows still flying
          return ScoreResult.WAITING_NEXT;
        }
      }
      return ScoreResult.SESSION_FINISHED;
    }
  }

  void killArrows(Server server) {
    for (UUID arrowUuid : this.arrows) {
      Entity entity = server.getEntity(arrowUuid);
      if (entity == null) {
        continue;
      }
      entity.remove();
    }
  }

  int shoot(UUID arrowUuid) {
    if (this.arrows.size() >= 5) {
      return this.arrows.size();
    }
    this.arrows.add(arrowUuid);
    return this.arrows.size();
  }

  boolean isValidArrow(Arrow arrow) {
    return this.arrows.contains(arrow.getUniqueId());
  }

  int totalScore() {
    int ret = 0;
    for (int i = 0; i < this.scores.size(); i++) {
      int s = this.scores.get(i).score;
      if (s < 0) {
        continue;
      }
      ret += s;
    }
    return ret;
  }

  String currentScoresMessage() {
    String s = "";
    int score = 0;
    for (int i = 0; i < this.scores.size(); i++) {
      if (i > 0) {
        s += " + ";
      }
      Hit hit = this.scores.get(i);
      if (hit.score < 0) {
        s += "?";
      } else {
        score += hit.score;
        s += hit.score;
      }
    }
    if (this.scores.size() > 0) {
      if (this.scores.size() == 1) {
        return score + " points";
      } else {
        return s + " = " + score + " points";
      }
    } else {
      return s + " points";
    }
  }

  static boolean IsValidShootingRangeFirst(Location location) {
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
    if (x < 88.5 || 92.5 < x) {
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

  static boolean IsValidShootingRangeSecond(Location location) {
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
