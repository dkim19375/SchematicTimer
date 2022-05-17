/*
 *     SchematicTimer, A spigot plugin that allows you to regenerate schematics
 *     Copyright (C) 2021  dkim19375
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.dkim19375.schematictimer.data

import me.dkim19375.dkimbukkitcore.data.LocationWrapper
import me.dkim19375.dkimbukkitcore.data.toWrapper
import me.dkim19375.schematictimer.util.ConfigurationException
import org.bukkit.Bukkit
import org.bukkit.Location

data class BlockLocationData(
    var world: String = "world",
    var x: Int = 0,
    var y: Int = 80,
    var z: Int = 0,
) {
    fun toWrapper(): LocationWrapper = toLocation().toWrapper()

    fun toLocation(): Location = Location(
        Bukkit.getWorld(world) ?: throw ConfigurationException("Invalid world: $world!"),
        x.toDouble(),
        y.toDouble(),
        z.toDouble()
    )
}
