# BoatRace - A simple racing plugin

A Java Minecraft Paper plugin that provides timers and scoreboards for races.
**This plugin is currently in development. Bugs and errors are expected.**
## What would the plugin do when it's completed
 First create a track, then setup a finish region. When a player in the race session reached the finish line, their timer is sent to players within the same session. When the session completes, the hollogram scoreboard will be updated, showing top 5 score on this track with each player has maximum of 1 record. A player's best 5 runs at each track are also stored.

### Usage:
    /race create <name> # create a race track
    /race setstart1 <name> [x] [y] [z] # set start region point 1
    /race setstart1 <name> [x] [y] [z] # set start region point 2
    /race setfinish1 <name> [x] [y] [z] # set finish region point 1
    /race setfinish2 <name> [x] [y] [z] # set finish region point 2
    /race delete <name> # delete a track
## Feature list:
 - Per player translation (locale file not completed yet)
 - MySQL support (not tested)
 - Support multiple tracks in the same world / custom dimensions

## TODO:
 - Persistent scoreboard using DecentHolograms: personal best and track top 5
 - Timer display during race
 - Luckperms support
 - Tab complete command
 - Limit players who joined the race to the start region at countdown
 - More supported paper versions
