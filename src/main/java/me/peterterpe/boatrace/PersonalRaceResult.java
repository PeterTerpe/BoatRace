package me.peterterpe.boatrace;

import java.time.Instant;

public class PersonalRaceResult {
    private long elapsed;
    private Instant timestamp;

    public PersonalRaceResult() {}
    public PersonalRaceResult(long elapsed) {
        this.elapsed = elapsed;
        this.timestamp = Instant.now();
    }

    public long getElapsed() { return elapsed; }
    public Instant getTimestamp() { return timestamp; }

}
