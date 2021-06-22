package com.github.giji34.usakensummerfestival;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

class PlayerBoatRaceRun {
  @NotNull
  final UUID vehicleUUid;
  Long startTimeMillis;
  Long goalTimeMillis;
  TimedLocation lastLocation;

  PlayerBoatRaceRun(UUID vehicleUUid, TimedLocation location) {
    this.vehicleUUid = vehicleUUid;
    this.lastLocation = location;
  }

  enum UpdateResult {
    SUCCESS,
    COURSE_OUT,
    STARTED,
    GOALED,
  }

  UpdateResult update(TimedLocation tl) {
    double t = tl.timeMillis;
    double x = tl.location.getX();
    double y = tl.location.getY();
    double z = tl.location.getZ();
    double t0 = this.lastLocation.timeMillis;
    double z0 = this.lastLocation.location.getZ();
    if (this.startTimeMillis == null) {
      if (y != 67 && y != 68) {
        return UpdateResult.COURSE_OUT;
      }
      if (z >= 49) {
        double a = (t0 - t) / (z0 - z);
        double start = t0 + (49.0 - z0) * a;
        this.startTimeMillis = (long)Math.floor(start);
        this.lastLocation = tl;
        return UpdateResult.STARTED;
      } else {
        this.lastLocation = tl;
        return UpdateResult.SUCCESS;
      }
    } else {
      if (z <= 47 && 62 <= x && x <= 68 && 69 <= y && y <= 73) {
        double a = (t0 - t) / (z0 - z);
        double goal = t0 + (47.0 - z0) * a;
        this.goalTimeMillis = (long)Math.ceil(goal);
        return UpdateResult.GOALED;
      } else {
        this.lastLocation = tl;
        return UpdateResult.SUCCESS;
      }
    }
  }
}
