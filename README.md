
# BoatRace - A simple racing plugin
A Java Minecraft Paper plugin that provides timers and scoreboards for races.
## How to use:
1. Setup a track using commands.
2. Players can join tracks by using /race join &lt;name>
3. An item will be given to players, which can be used to switch their status between ready and not ready on right click
4. When all participants in the track are ready, the countdown starts and race begins afterwards
5. During countdown, participants who are outside the start region will be teleported to the spawn of the track. And if participants tried to move outside the start region while mounting a boat, the boat is removed. Player movements of participants outside the start region during countdown will be cancelled. **Caution: player position will be lowered when they mount a boat, this must be considered when setting the start region**
6. When a participant reached the finish line, their timer is sent to all participants within the same session. Their  personal records and track records will be updated immediately if conditions are met.
## Dependencies:
DecentHolograms (tested with v2.9.4)
## Usage:
/race create <name> # create a race track<br>
/race setstart1 <name> [x] [y] [z] # set start region point 1<br>
/race setstart1 <name> [x] [y] [z] # set start region point 2<br>
/race setfinish1 <name> [x] [y] [z] # set finish region point 1<br>
/race setfinish2 <name> [x] [y] [z] # set finish region point 2<br>
/race delete <name> # delete a track<br>
/race stop <name> # force stop a race session<br>
/race hologram <name> <arg> # modify track top5 scoreboard<br>
/race check <name> # check personal top records on specified track
## Feature list:
- Per player translation
- Countdown before race
- Limit players who joined the race to the start region at countdown
- Timer display during race
- Support multiple tracks in the same world & Multiverse
- Finish line message
- Top5 scoreboard for each track
- Personal bests records
- Luckperms support
## TODO:
- Configurable timezone for timestamp
