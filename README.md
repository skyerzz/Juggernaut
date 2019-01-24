# Juggernaut
Juggernaut gamemode for minecraft, made for the Hypixel Events server

A game idea by Mythyryn, made by skyerzz.


Juggernaut is an asymmetrical arena game (Such as Dead by Daylight, EVOLvE and Deathgarden). The Juggernaut is fighting against 6 survivors in a closed off arena. The game constantly changes the dynamics as each team progresses through the game.

## Gameplay
When the game starts, a random player is selected to be the "Juggernaut". The Juggernaut starts with fully equipped strong armor and a weapon, but is slower than the survivors. The survivors start without any items. The end goal of the game is to kill off the other team.

### Juggernaut
The juggernaut starts the game noticably slower than the survivors, making a direct charge attack towards them near impossible to succeed. As the game progresses, the juggernaut gains speed gradually till a point where he is faster than the survivors, and can chase them down.

### Survivors
The 6 players remaining, labelled the "survivors", have to avoid the juggernaut for the first part of the game. The main objective of the survivors is to look around the map for locked chests, which are placed in randmo spots. They will have to unlock the chest, and claim the item inside of them to claim the equipment in them, and have a better chance against the juggernaut.

### Healing
An important fact to notice about this game is that natural healing is turned off. Any damage you take is permanent untill you fulfill the actions to get healed. (For survivors, another survivor has to heal you. For the juggernaut, you have to stop and heal yourself.)

## Mechanics

### The map
The map is generated at the start of each game, put together by pre-built pieces. This causes the map to be different every time you play the game. Each pre-built part of the map has a spot where a chest can spawn, and a spot where a player can spawn, with a safeguard built in so the juggernaut will not spawn close to the survivors at the start of the game.

Due to the generated map, you cannot learn the positions of certain game objects, and therefore new players will not have a big disadvantage over seasoned players.

Another advantage of using a generated map is that the size of the map can be changed easily by just changing the maximum size of the map, thereby increasing the total tiles.


### Chests

Whilst the chests are mainly for the survivors to advance in the game, the juggernaut can use them to his advantage too. The Juggernaut can stick near a chest to have more chance of running into a survivor who wants to unlock it, and so eliminate said survivor.
When a chest is opened, it will shoot a firework into the air to all players who have not claimed the chest yet (including the juggernaut) to indicate its location. A chest will only close and respawn with its next tier level when all living survivors have claimed the item from the chest. When a chest opens or closes, it will play a sound effect to all players so everyone knows a chest has progressed.

**Survivors**

Survivors can open chests by sneaking in the visible radius of said chest. When doing this, the progress bar above the chest will fill up, letting you know the approximate unlocking percentage of the chest. The more survivors are sneaking near a chest, the faster it will open (linear).

When a chest is unlocked, it will display the item inside of it. All survivors have to claim this item by sneaking near the chest for a few seconds, the progress for this is visible in their experience bar. When the chest is claimed, the item will automatically equip for the player.

**Juggernaut**

The juggernaut can not open chests, as he does not want to help the survivors win. He can however, lock chests up if they have not been fully opened. 

### Speed
A large mechanic of this game is the player's speed. Sprinting is disabled to stop minecraft vanilla mechanics breaking ours, and instead the base speed has been raised by 30% to be equivalent as vanilla sprinting speeds.

At the start of the game, the juggernaut has 85% of the speed of survivors. As time passes, the juggernaut's speed will increase gradually, till a maximum of 115%. This gives the survivors some time to gear up.

The juggernaut can be slown down by being hit by a survivor, which slows him down by a flat 2%. There are some perks for the survivors which can also help slow the juggernaut down.

The juggernaut's speed and time till the next speed increment is shown to him by using the experience bar & level.

### Multi-Tasking/Focus
To not make any of the players too strong by being able to do multiple things at once, all players have a 'Focus'. When doing just one thing, all focus is directed to that objective. However, when doing multiple things at the same time (For example, as a survivor, heal another player *and* unlock a chest), the focus will be divided over the tasks, making all of them less effective.

## Perks

More perks may be added in the future. Currently coded perks are listed here

### Survivors
- Ender Pearl
  - Gives the survivor a one-time enderpearl (no damage when using the pearl)
- Snow Ball
  - Is available to the survivors only when the juggernaut has 100% or higher speed. When used, this perk has a 20 second cooldown.
  - Throws a snowball. If the juggernaut is hit by this snowball, he loses a flat 5% of his speed.
- Speed Boost
  - Available instantly, this gives the survivor 50% extra speed for 2 seconds when used. This perk has a 30 second cooldown before it can be used again.

### Juggernaut
- Zombie Watcher
  - Spawns a Zombie Watcher on the location of the juggernaut. The Zombie Watcher will go after any survivors in its radius, whcih are in line of sight. When no survivors are in line of sight or in his radius, he will return back to the spawning location.
  - When the zombie is killed, a cooldown is applied before he can be spawned in again.
