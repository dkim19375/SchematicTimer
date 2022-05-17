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

import me.mattstudios.config.SettingsHolder
import me.mattstudios.config.annotations.Comment
import me.mattstudios.config.annotations.Path
import me.mattstudios.config.properties.Property

object MainConfigData : SettingsHolder {
    @Path("schematics")
    val SCHEMATICS = Property.create(SchematicData::class.java, mapOf(
        "example" to SchematicData()
    ))

    @Path("messages")
    val MESSAGES = Property.create(MessageConfigData::class.java, mapOf(
        "all" to MessageConfigData()
    ))

    @Path("blocks-per-tick")
    @Comment("The amount of blocks to clear per tick (20 ticks = 1 second)")
    val BLOCKS_PER_TICK = Property.create(50000)
}