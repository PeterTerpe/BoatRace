package me.peterterpe.boatrace;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.google.gson.annotations.Expose;

public class PersonalRaceResult {
    @Expose private long elapsed;
    @Expose private Instant timestamp;

    public PersonalRaceResult() {}
    public PersonalRaceResult(long elapsed) {
        this.elapsed = elapsed;
        this.timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public long getElapsed() { return elapsed; }
    public Instant getTimestamp() { return timestamp; }

}
