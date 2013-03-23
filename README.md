MCNSAEssentials
===============

## Commands (sorted by component)
Note: only currently implemented commands are listed here

### Backpack
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/backpack &lt;player&gt;</td>
        <td>mcnsaessentials.backpack.other</td>
        <td>Allows the player to open other players backpacks.</td>
    </tr>

</table>

### Ban
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/ban &lt;player&gt; [reason]</td>
        <td>mcnsaessentials.ban.ban.forever</td>
        <td>Bans the target player until the ban is removed. The player will be kicked and banned from the server with a displayed reason if one is supplied. If not it will use a default message.</td>
    </tr>

    <tr>
        <td>/ban &lt;player&gt; [time] [reason]</td>
        <td>mcnsaessentials.ban.ban.expiry</td>
        <td>Bans the target player for a defined amount of time. The reason will be displayed if provied along with the time remaining on their ban.</td>
    </tr>

    <tr>
        <td>/unban &lt;player&gt;</td>
        <td>mcnsaessentials.ban.unban</td>
        <td>Unbans the target player.</td>
    </tr>

    <tr>
        <td>/ban &lt;ip&gt; [reason]</td>
        <td>mcnsaessentials.ban.ip.forever</td>
        <td>Bans the target player until the ban is removed. The player will be kicked and banned from the server with a displayed reason if one is supplied. If not it will use a default message.</td>
    </tr>

    <tr>
        <td>/banip &lt;ip&gt; [time] [reason]</td>
        <td>mcnsaessentials.ban.ip.expiry</td>
        <td>Bans the target player for a defined amount of time. The reason will be displayed if provied along with the time remaining on their ban.</td>
    </tr>

    <tr>
        <td>/unbanip &lt;player&gt;</td>
        <td>mcnsaessentials.ban.unip</td>
        <td>Unbans the target ip.</td>
    </tr>

</table>


### Debug
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/ping</td>
        <td>mcnsaessentials.debug.ping</td>
        <td>Pings the server to make sure it is still responsive. It will result in replying with one of several replies.</td>
    </tr>
    <tr>
        <td>/tps</td>
        <td>mcnsaessentials.debug.tps</td>
        <td>Reports the server's current Ticks Per Second.</td>
    </tr>
    <tr>
        <td>/serverinfo</td>
        <td>mcnsaessentials.debug.serverinfo</td>
        <td>Reports current server info for each world, including loaded chunks and entities.</td>
    </tr>
    <tr>
        <td>/resetmeta</td>
        <td>mcnsaessentials.debug.resetmeta</td>
        <td>Resets all metadata associated with MCNSAEssentials.</td>
    </tr>

</table>

### Freeze
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/freeze &lt;player&gt;</td>
        <td>mcnsaessentials.freeze.freeze</td>
        <td>Freezes the target player leaving them immobilized in their current location.</td>
    </tr>

    <tr>
        <td>/unfreeze &lt;player&gt;</td>
        <td>mcnsaessentials.freeze.unfreeze</td>
        <td>Unfreezes the target player leaving them immobilized in their current location.</td>
    </tr>

</table>

### Fun
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/hat</td>
        <td>mcnsaessentials.fun.hat</td>
        <td>Places the block currently in the player's hand on their head.</td>
    </tr>

    <tr>
        <td>/slap &lt;player&gt;</td>
        <td>mcnsaessentials.fun.slap</td>
        <td>Slaps the target player displacing them a few blocks. If player is left blank the command will slap the issuer.</td>
    </tr>

    <tr>
        <td>/rocket &lt;player&gt; &lt;player&gt; ... etc</td>
        <td>mcnsaessentials.fun.rocket</td>
        <td>Sends the target players flying into the air, it will end with a deadly plummet to the surface. </td>
    </tr>

    <tr>
        <td>/immolate &lt;player&gt; &lt;player&gt; ... etc</td>
        <td>mcnsaessentials.fun.immolate</td>
        <td>Sets the target players on fire. </td>
    </tr>

</table>

### Home
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/homes</td>
        <td>mcnsaessentials.home.list.self</td>
        <td>Prints out a list of the player's possible homes. </td>
    </tr>

    <tr>
        <td>/homes &lt;player&gt;</td>
        <td>mcnsaessentials.home.list.other</td>
        <td>Prints out a list of the specified player's possible homes. </td>
    </tr>

    <tr>
        <td>/sethome &lt;home name&gt;</td>
        <td>mcnsaessentials.home.set.self</td>
        <td>Sets a home to the given name for the issuer. </td>
    </tr>

    <tr>
        <td>/sethome &lt;player&gt; &lt;home name&gt;</td>
        <td>mcnsaessentials.home.set.other</td>
        <td>Sets a home to the given name for the specified player. </td>
    </tr>


</table>

### Information
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/motd</td>
        <td>mcnsaessentials.info.motd</td>
        <td>Lists the message of the day. </td>
    </tr>

    <tr>
        <td>/rules</td>
        <td>mcnsaessentials.info.rules</td>
        <td>Lists the rules of the server. </td>
    </tr>

    <tr>
        <td>/who</td>
        <td>mcnsaessentials.info.who</td>
        <td>Lists the current players online. </td>
    </tr>
</table>

### Items
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/givehead &lt;HeadType|Player&gt;</td>
        <td>mcnsaessentials.items.givehead</td>
        <td>Gives a head of either the specified monster head type or the specified player's head skin. </td>
    </tr>

    <tr>
        <td>/who</td>
        <td>mcnsaessentials.info.who</td>
        <td>Lists the current players online. </td>
    </tr>


</table>

### Kick
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/kick &lt;player&gt; [reason]</td>
        <td>mcnsaessentials.kick.kick.forever</td>
        <td>Bans the target player until the kick is removed. The player will be kicked and banned from the server with a displayed reason if one is supplied. If not it will use a default message.</td>
    </tr>

</table>

### Kits
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/kits</td>
        <td>mcnsaessentials.kit.kits</td>
        <td>Displays the current kits available to the player.</td>
    </tr>

</table>

### Mail
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>To be added</td>
        <td>To be added</td>
        <td>To be added</td>
    </tr>

</table>
### Playermode
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/gamemode &lt;s|c|a&gt;</td>
        <td>mcnsaessentials.playermode.gamemode.self</td>
        <td>Changes the player's game mode to the specified mode. S - Survival, C - Creative, A- Adventure.</td>
    </tr>

    <tr>
        <td>/gamemode &lt;s/c/a&gt; [player]</td>
        <td>mcnsaessentials.playermode.gamemode.others</td>
        <td>Changes the specified player's game mode to the specified mode. S - Survival, C - Creative, A- Adventure. If left blank it will change the issuer's game mode.</td>
    </tr>

    <tr>
        <td>/god</td>
        <td>mcnsaessentials.playermode.god.self</td>
        <td>Toggle's the player's gamemode to invulnerable.</td>
    </tr>

    <tr>
        <td>/god &lt;player&gt;</td>
        <td>mcnsaessentials.playermode.god.others</td>
        <td>Toggle's the target player's gamemode to invulnerable.</td>
    </tr>

    <tr>
        <td>/ungod</td>
        <td>mcnsaessentials.playermode.god.self</td>
        <td>Toggle's the player's gamemode to vulnerable.</td>
    </tr>


    <tr>
        <td>/ungod &lt;player&gt;</td>
        <td>mcnsaessentials.playermode.god.others</td>
        <td>Toggle's the target player's gamemode to vulnerable.</td>
    </tr>
</table>

### Rank
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/promote &lt;player&gt;</td>
        <td>mcnsaessentials.rank.promote</td>
        <td>Promotes a player to the next rank on the specified player's current rank ladder.</td>
    </tr>

    <tr>
        <td>/demote &lt;player&gt;</td>
        <td>mcnsaessentials.rank.demote</td>
        <td>Demotes a player to the previous rank on the specified player's current rank ladder.</td>
    </tr>

    <tr>
        <td>/hotdog &lt;player&gt;</td>
        <td>mcnsaessentials.rank.hotdog</td>
        <td>Hotdogs(Peons) demotes the player to the lowest rank. Normaly used for punishment.</td>
    </tr>

</table>

### Spawn
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/spawn</td>
        <td>mcnsaessentials.spawn.spawn</td>
        <td>Teleports the player to the designated spawn.</td>
    </tr>

    <tr>
        <td>/setspawn [world] [x,y,z]</td>
        <td>mcnsaessentials.spawn.set</td>
        <td>Sets the spawn to the current location of the player unless otherwise specified by optional arguements.</td>
    </tr>

</table>

### Teleport
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/tp &lt;player&gt;</td>
        <td>mcnsaessentials.teleport.self</td>
        <td>Teleports the issuer to the specified player.</td>
    </tr>

    <tr>
        <td>/tp &lt;player&gt; &lt;player&gt;</td>
        <td>mcnsaessentials.teleport.other</td>
        <td>Teleports the specified player to the second specified player.</td>
    </tr>

    <tr>
        <td>/bring &lt;player&gt;</td>
        <td>mcnsaessentials.teleport.teleport.bring</td>
        <td>Brings the specified player to the issuer's location.</td>
    </tr>

    <tr>
        <td>/tp [world] &lt;X Y Z&gt;</td>
        <td>mcnsaessentials.teleport.selfcoords</td>
        <td>Teleports the issuer to the specified coordinates.</td>
    </tr>

    <tr>
        <td>/tp &lt;player&gt; [world] &lt;X Y Z&gt;</td>
        <td>mcnsaessentials.teleport.othercoords</td>
        <td>Teleports the specified player to the specified coordinates.</td>
    </tr>

</table>

### Teleport History
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/back</td>
        <td>mcnsaessentials.teleporthistory.back</td>
        <td>Returns the player to the location they last teleported from.</td>
    </tr>
</table>

### Time
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/thetime [world]</td>
        <td>mcnsaessentials.time.tell</td>
        <td>Tells you the current time of your current or specified world.</td>
    </tr>

    <tr>
        <td>/settime [world]</td>
        <td>mcnsaessentials.time.set</td>
        <td>Sets the time of your current or specified world.</td>
    </tr>
</table>

### Vanish
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/vanish</td>
        <td>mcnsaessentials.vanish.self</td>
        <td>Turns the player invisible.</td>
    </tr>

    <tr>
        <td>/vanish &lt;player&gt;</td>
        <td>mcnsaessentials.vanish.others</td>
        <td>Turns the specified player invisible.</td>
    </tr>

</table>

### Warp
<table>
    <tr>
        <th>Command</th>
        <th>Permissions</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>/warps</td>
        <td>mcnsaessentials.warp.warps</td>
        <td>Lists the available warps.</td>
    </tr>

    <tr>
        <td>/setwarp &lt;Warp Name&gt; [public|private]</td>
        <td>mcnsaessentials.warp.set</td>
        <td>Sets a warp at current location to the specified name. The warps can be made either for the public or as private warps.</td>
    </tr>

    <tr>
        <td>/delwarp &lt;Warp Name&gt;</td>
        <td>mcnsaessentials.warp.del</td>
        <td>Deletes the specified warp.</td>
    </tr>

    <tr>
        <td>/warp &lt;Warp Name&gt; </td>
        <td>mcnsaessentials.warp.warp.self</td>
        <td>Warps the player to a specified warp.</td>
    </tr>


</table>
