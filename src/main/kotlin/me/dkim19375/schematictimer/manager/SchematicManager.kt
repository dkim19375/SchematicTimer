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

package me.dkim19375.schematictimer.manager

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.util.SideEffect
import com.sk89q.worldedit.util.SideEffectSet
import com.sk89q.worldedit.world.block.BaseBlock
import me.dkim19375.dkimbukkitcore.data.LocationWrapper
import me.dkim19375.dkimbukkitcore.function.logInfo
import me.dkim19375.dkimcore.extension.concurrentDequeOf
import me.dkim19375.dkimcore.extension.setDecimalPlaces
import me.dkim19375.dkimcore.file.YamlFile
import me.dkim19375.schematictimer.SchematicTimer
import me.dkim19375.schematictimer.data.DateData
import me.dkim19375.schematictimer.data.MainConfigData
import me.dkim19375.schematictimer.data.MessageConfigData
import me.dkim19375.schematictimer.data.SchematicData
import me.dkim19375.schematictimer.util.broadcastFormatted
import me.dkim19375.schematictimer.util.getCalendarValue
import org.bukkit.Bukkit
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Deque
import java.util.logging.Level

class SchematicManager(private val plugin: SchematicTimer) {
    private val config: YamlFile
        get() = plugin.mainConfig
    var lastTime: DateData? = null
    private val blocksToSet = concurrentDequeOf<Pair<Pair<LocationWrapper, Boolean>, BaseBlock>>()

    init {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            val regions = config.get(MainConfigData.SCHEMATICS)
            val time = DateData.getCurrentTime()
            if (lastTime == time) {
                return@Runnable
            }
            lastTime = time
            val sentMessages = mutableMapOf<String, Set<Int>>()
            for ((name, region) in regions) {
                if (!region.enabled) {
                    continue
                }
                val messages = region.getMessageData(name, config)
                if (region.isCurrent()) {
                    generate(region, messages, sentMessages)
                    continue
                }
                messages ?: continue
                fun getTimeSet(): Set<Int> = sentMessages[messages.first] ?: emptySet()
                val times = mutableListOf<Pair<DateData, Calendar>>()
                for (timeOfDays in region.timeOfDays) {
                    val dateData = DateData.fromString(timeOfDays)
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = dateData.toEpochMillis()
                    val dayOfWeeks = region.getDayOfWeeks()
                    if (dayOfWeeks.isEmpty()) {
                        times.add(dateData to calendar.clone() as Calendar)
                        continue
                    }
                    for (dayOfWeek in dayOfWeeks) {
                        val clonedCalendar = calendar.clone() as Calendar
                        clonedCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek.getCalendarValue())
                        times.add(dateData to clonedCalendar)
                    }
                }
                val dayOfWeek = LocalDateTime.now().dayOfWeek
                for ((warningTime, warning) in messages.second.warnings) {
                    val warningMins = warningTime.toInt()
                    val currentTimeMins = time.toEpochMinutes()
                    val newTime = DateData.fromEpochMinutes(currentTimeMins + warningMins.toLong())
                    val match = times.firstOrNull { (dateData, calendar) ->
                        dateData == newTime && LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(
                                calendar.also { it.add(Calendar.MINUTE, -warningMins) }.timeInMillis
                            ), ZoneId.systemDefault()
                        ).dayOfWeek == dayOfWeek
                    }
                    if (match != null) {
                        if (warningMins !in getTimeSet()) {
                            broadcastFormatted(warning)
                            sentMessages[messages.first] = getTimeSet() + warningMins
                        }
                    }
                }
            }
        }, 10L, 10L)
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val start = System.nanoTime()
            val amountToSet = plugin.mainConfig.get(MainConfigData.BLOCKS_PER_TICK)
            val blocks = blocksToSet.takeMutable(amountToSet).ifEmpty { return@Runnable }
            for ((pair, baseBlock) in blocks) {
                val loc = pair.first
                val update = pair.second
                val world = BukkitAdapter.adapt(loc.world)
                val blockVector3 = BlockVector3.at(loc.x, loc.y, loc.z)
                val sideEffects = SideEffectSet.defaults()
                    .with(SideEffect.UPDATE, if (update) SideEffect.State.ON else SideEffect.State.OFF)
                world.setBlock(blockVector3, baseBlock, sideEffects)
            }
            val end = System.nanoTime()
            val time = ((end.toDouble() - start) / 1000000).setDecimalPlaces(3)
            logInfo(
                text = "Cleared ${blocks.size} blocks ${
                    if (blocksToSet.isNotEmpty()) {
                        "(${blocksToSet.size} remaining in queue) "
                    } else {
                        ""
                    }
                }in $time ms"
            )
        }, 1L, 1L)
    }

    private fun <T> Deque<T>.takeMutable(amount: Int): List<T> {
        val list = mutableListOf<T>()
        while (isNotEmpty() && list.size < amount) {
            list.add(removeFirst())
        }
        return list
    }

    fun generate(
        schematicData: SchematicData,
        messages: Pair<String, MessageConfigData>? = null,
        sentMessages: MutableMap<String, Set<Int>>? = null,
    ) {
        if (messages != null) {
            val set = sentMessages?.get(messages.first) ?: emptySet()
            if (-1 !in set) {
                messages.second.message.let { message ->
                    broadcastFormatted(message)
                    sentMessages?.put(messages.first, set + -1)
                }
            }
        }
        val start = System.nanoTime()
        val file = schematicData.getSchematicFile()
        val clipboardFormat = file?.let(ClipboardFormats::findByFile)
        if (clipboardFormat == null) {
            logInfo(
                text = "Could not generate schematic ${schematicData.schematic} because it is invalid or doesn't exist!",
                level = Level.SEVERE
            )
            return
        }
        val clipboard = clipboardFormat.getReader(file.inputStream()).use(ClipboardReader::read)
        val configOrigin = schematicData.location.toWrapper()
        clipboard.region.world = BukkitAdapter.adapt(configOrigin.world)
        val edgeX = setOf(clipboard.minimumPoint.x, clipboard.maximumPoint.x)
        val edgeY = setOf(clipboard.minimumPoint.y, clipboard.maximumPoint.y)
        val edgeZ = setOf(clipboard.minimumPoint.z, clipboard.maximumPoint.z)
        var amount = 0
        val configOriginVector = BlockVector3.at(configOrigin.x, configOrigin.y, configOrigin.z)
        val diff = configOriginVector.subtract(clipboard.origin)
        logInfo(
            """
                configOriginVector: $configOriginVector
                clipboard.origin: ${clipboard.origin}
                diff: $diff
            """.trimIndent()
        )
        clipboard.region.first().let { blockVector3 ->
            val newVector = blockVector3.add(diff)
            logInfo("blockVector3: $blockVector3, newVector: $newVector")
        }
        blocksToSet.addAll(clipboard.region.map { blockVector3 ->
            amount++
            val newVector = blockVector3.add(diff)
            val loc = LocationWrapper(configOrigin.world, newVector.blockX, newVector.blockY, newVector.blockZ)
            val update = blockVector3.blockX in edgeX || blockVector3.blockY in edgeY || blockVector3.blockZ in edgeZ
            (loc to update) to clipboard.getFullBlock(blockVector3)
        })
        val end = System.nanoTime()
        val time = ((end.toDouble() - start) / 1000000).setDecimalPlaces(3)
        logInfo("Queued $amount blocks in $time ms (ASYNC)")
    }

}