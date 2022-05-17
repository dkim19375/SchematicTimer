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

import me.dkim19375.schematictimer.SchematicTimer
import me.dkim19375.dkimbukkitcore.data.HelpMessage
import me.dkim19375.dkimbukkitcore.data.HelpMessageFormat
import me.dkim19375.dkimbukkitcore.function.formatAll
import me.dkim19375.dkimbukkitcore.function.getMaxHelpPages
import me.dkim19375.dkimbukkitcore.function.showHelpMessage
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.permissions.Permissible
import org.bukkit.plugin.java.JavaPlugin

private val plugin by lazy { JavaPlugin.getPlugin(SchematicTimer::class.java) }

private val commands = listOf(
    HelpMessage("help [page]", "Shows the help menu", Permissions.COMMAND.perm),
    HelpMessage("reload", "Reload the config files", Permissions.RELOAD.perm),
    HelpMessage("check", "Recheck schematics for the current time", Permissions.CHECK.perm),
)

private val format = HelpMessageFormat(
    topBar = null,
    header = "${ChatColor.GREEN}%name% v%version% Help Page: %page%/%maxpages%",
    bottomBar = null
)

fun CommandSender.sendHelpMessage(
    label: String,
    page: Int = 1,
) = showHelpMessage(
    label = label,
    error = null,
    page = page,
    commands = commands,
    plugin = plugin,
    format = format
)

fun CommandSender.sendMessage(error: ErrorMessages) = sendMessage(error.component)

fun Permissible.hasPermission(permissions: Permissions): Boolean = hasPermission(permissions.perm)

fun Permissible.getMaxHelpPages(): Int = getMaxHelpPages(commands)

fun broadcastFormatted(str: String) {
    val formatted = str.formatAll()
    val miniMessage = MiniMessage.miniMessage().deserialize(
        MiniMessage.miniMessage().serialize(
            LegacyComponentSerializer.legacySection().deserialize(formatted)
        )
    )
    if (PlainTextComponentSerializer.plainText().serialize(miniMessage).trim().isBlank()) {
        return
    }
    broadcast(miniMessage)
}

fun broadcast(component: Component) {
    val receivers: List<CommandSender> = Bukkit.getOnlinePlayers() + Bukkit.getConsoleSender()
    receivers.forEach { it.sendMessage(component) }
}

private val audience by lazy { BukkitAudiences.create(plugin) }

fun CommandSender.getAudience(): Audience = audience.sender(this)

fun CommandSender.sendMessage(message: Component) = getAudience().sendMessage(message)