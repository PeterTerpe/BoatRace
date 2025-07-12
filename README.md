# BoatRace - A simple racing plugin

A Java Minecraft Paper plugin that provides timers and scoreboards for races.
**This plugin is currently in development. Bugs and errors are expected.**
## What would the plugin do when it's completed
 First create a track, then setup a finish region. When a player in the race session reached the finish line, their timer is sent to players within the same session. When the session completes, the hollogram scoreboard will be updated, showing top 5 score on this track with each player has maximum of 1 record. A player's best 5 runs at each track are also stored.

## Dependencies:
DecentHolograms

## Usage:
    /race create <name> # create a race track
    /race setstart1 <name> [x] [y] [z] # set start region point 1
    /race setstart1 <name> [x] [y] [z] # set start region point 2
    /race setfinish1 <name> [x] [y] [z] # set finish region point 1
    /race setfinish2 <name> [x] [y] [z] # set finish region point 2
    /race delete <name> # delete a track
    /race stop <name> # force stop a race session
    /race hologram <name> <arg> # modify track top5 scoreboard
## Feature list:
 - Per player translation (locale file not completed yet)
 - .yml file storage support
 - Limit players who joined the race to the start region at countdown
 - Timer display during race
 - Support multiple tracks in the same world & Multiverse
 - Countdown
 - Finish line message
 - Top5 scoreboard for each track
 - Luckperms support
## TODO:
 - Personal bests scoreboards
 - More supported paper versions
