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

import me.dkim19375.dkimcore.extension.containsIgnoreCase
import me.dkim19375.dkimcore.file.YamlFile
import me.dkim19375.schematictimer.util.toDayOfWeek
import me.mattstudios.config.annotations.Name
import org.bukkit.Bukkit
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate

data class SchematicData(
    var schematic: String = "building.schem",
    var location: BlockLocationData = BlockLocationData(),
    @Name("day-of-week")
    var dayOfWeek: String = "mondays",
    @Name("time-of-days")
    var timeOfDays: Set<String> = setOf(
        "18:00"
    ),
) {
    fun getSchematicFile(): File? = File(
        File(Bukkit.getPluginManager().getPlugin("WorldEdit")?.dataFolder, "schematics"),
        schematic
    ).takeIf(File::exists)

    fun getDayOfWeeks(): Set<DayOfWeek> = dayOfWeek.split(',')
        .map(String::trim)
        .mapNotNull(String::toDayOfWeek)
        .toSet()

    fun getMessageData(name: String, config: YamlFile): Pair<String, MessageConfigData>? = config.get(MainConfigData.MESSAGES)
        .entries
        .firstOrNull { (_, configData) ->
            configData.schematics.containsIgnoreCase(name)
        }?.toPair()

    fun isCurrent(): Boolean {
        val dayOfWeeks = getDayOfWeeks()
        if (LocalDate.now().dayOfWeek !in dayOfWeeks) {
            return false
        }
        val now = DateData.getCurrentTime()
        val dates = timeOfDays.map { DateData.fromString(it) }
        return dates.any(now::equals)
    }
}