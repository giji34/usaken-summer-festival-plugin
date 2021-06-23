package com.github.giji34.usakensummerfestival;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

class ScoreboardVisibility {
  long visibleUntilMillis;
  final JavaPlugin owner;
  final String scoreboardName;

  static long visibleDurationMillis = 5 * 60 * 1000;

  ScoreboardVisibility(JavaPlugin owner, String scoreboardName) {
    this.visibleUntilMillis = System.currentTimeMillis();
    this.owner = owner;
    this.scoreboardName = scoreboardName;

    owner.getServer().getScheduler().scheduleSyncRepeatingTask(owner, this::updateVisibility, 0, 10 * 20);
  }

  void makeVisible() {
    this.visibleUntilMillis = System.currentTimeMillis() + visibleDurationMillis;
  }

  private void updateVisibility() {
    Scoreboard scoreboard = owner.getServer().getScoreboardManager().getMainScoreboard();
    Objective objective = scoreboard.getObjective(scoreboardName);
    if (objective == null) {
      return;
    }
    if (System.currentTimeMillis() > visibleUntilMillis) {
      objective.setDisplaySlot(null);
    } else {
      objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
  }
}
