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

package me.dkim19375.schematictimer.util

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

enum class ErrorMessages(error: String) {
    NO_PERMISSION("You do not have permission!"),
    INVALID_ARG("Invalid argument!"),
    NOT_ENOUGH_PARAMS("Not enough parameters!"),
    INVALID_REGION("Invalid schematic!"),
    ;
    val component: TextComponent = LegacyComponentSerializer.legacySection()
        .deserialize(error)
        .color(NamedTextColor.RED)
}