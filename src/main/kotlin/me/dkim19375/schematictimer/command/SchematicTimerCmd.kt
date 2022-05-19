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

package me.dkim19375.schematictimer.command

import me.dkim19375.dkimcore.extension.getIgnoreCase
import me.dkim19375.schematictimer.SchematicTimer
import me.dkim19375.schematictimer.data.MainConfigData
import me.dkim19375.schematictimer.util.ErrorMessages
import me.dkim19375.schematictimer.util.Permissions
import me.dkim19375.schematictimer.util.hasPermission
import me.dkim19375.schematictimer.util.sendHelpMessage
import me.dkim19375.schematictimer.util.sendMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SchematicTimerCmd(private val plugin: SchematicTimer) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission(Permissions.COMMAND)) {
            sender.sendMessage(ErrorMessages.NO_PERMISSION)
            return true
        }
        if (args.isEmpty()) {
            sender.sendHelpMessage(label)
            return true
        }
        when (args[0]) {
            "help" -> {
                sender.sendHelpMessage(label, args.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(1) ?: 1)
            }
            "reload" -> {
                if (!sender.hasPermission(Permissions.RELOAD)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                plugin.reloadConfig()
                sender.sendMessage(Component.text("Reloaded the config file!").color(NamedTextColor.GREEN))
            }
            "check" -> {
                if (!sender.hasPermission(Permissions.CHECK)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                plugin.manager.lastTime = null
                sender.sendMessage(Component.text("Successfully checked!").color(NamedTextColor.GREEN))
            }
            "place" -> {
                if (!sender.hasPermission(Permissions.PLACE)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION)
                    return true
                }
                if (args.size < 2) {
                    sender.sendMessage(ErrorMessages.NOT_ENOUGH_PARAMS)
                    return true
                }
                val schematicName = plugin.mainConfig.get(MainConfigData.SCHEMATICS).keys.getIgnoreCase(args[1])
                val schematic = plugin.mainConfig.get(MainConfigData.SCHEMATICS)[schematicName]
                if (schematic == null || schematicName == null) {
                    sender.sendMessage(ErrorMessages.INVALID_REGION)
                    return true
                }
                plugin.manager.generate(schematic)
                sender.sendMessage(
                    Component.text("Placing the schematic $schematicName!").color(NamedTextColor.GREEN)
                )
            }
            else -> sender.sendMessage(ErrorMessages.INVALID_ARG)
        }
        return true
    }
}