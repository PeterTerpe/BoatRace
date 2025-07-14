package me.peterterpe.boatrace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class PersonalTrackRecord {
    int attempts = 0;
    List<PersonalRaceResult> best = new ArrayList<>();

    public PersonalTrackRecord() {}

    public boolean addRecord(PersonalRaceResult result) {
        Comparator<PersonalRaceResult> byElapsed = Comparator.comparingLong(PersonalRaceResult::getElapsed);
        int idx = Collections.binarySearch(best, result, byElapsed);
        if (idx < 0) idx = -idx - 1;
        if (idx < 5) {
            best.add(idx, result);
            if (best.size() > 5) best.subList(5, best.size()).clear();;
            return true;
        }
        return false;
    }

    public List<PersonalRaceResult> getRecords() { return best; }
}
