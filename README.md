# Deprioritize leave
Deprioritizes options if items above x value, or items in whitelist, are present in the current instance.

Useful for stuff like resetting instances on slow respawning mobs so you don't leave phosani with an inquistor's mace on the ground.

If you want something new added or want to contact me, go to [my discord server](https://discord.gg/AHcvPd9uru).
## Setup
Add any items you would like to whitelist to the Whitelist, or set the Value Over to whatever value you wouldn't want to leave on the ground.\
The whitelist supports wildcards, so for example "Jar of*" will include every boss jar, and *(elite) will include Clue scroll (elite) and Scroll box (elite).

## Deprioritzed object options
For deprioritized object options, add the option that shows when you hover over the leave option. Eg for the Leviathan boat it would be "Travel", for the nightmare barrier, it would be "Pass-through".\
Quick-exit, exit, quick-leave and leave are all added as examples. I might add more to the default config later.

## Deprioritized widget options
Deprioritized widget options refer to things like the inventory or spells in the spellbook.\
Spells will always just be "Cast", but inventory items may be things like "Break" for teletabs, "Nardah" for desert ammy, "Tele to POH" for construction cape, etc.

All the options are deprioritized _after_ any other plugin does their shuffling.

Without deprio leave
![img](https://github.com/user-attachments/assets/b723ecf8-c5cb-42a2-91c7-ff6c03a1c746)

With deprio leave
![img](https://github.com/user-attachments/assets/83e201ee-bb85-442d-b2cf-e08b4f453f10)
