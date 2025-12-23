package com.zero.health.ui.activity

/**
 * @date:2025/12/23 19:47
 * @path:com.zero.health.ui.activity.ProcessMonitor
 */
import android.system.Os
import android.system.OsConstants
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class ProcessMonitor(private val scanIntervalMs: Long = 2000L) {

    // ======================
    // 数据模型
    // ======================

    data class ProcessTimeline(val pid: Int, val packageName: String, val startUptimeSec: Double,
                               @Volatile var endUptimeSec: Double? = null) {
        fun durationSec(now: Double): Double = (endUptimeSec ?: now) - startUptimeSec
    }

    data class ProcessUiModel(val pid: Int, val packageName: String, val aliveSeconds: Double,
                              val isRunning: Boolean)

    private data class ScanInfo(val pid: Int, val packageName: String, val startUptimeSec: Double)

    // ======================
    // 内部状态
    // ======================

    private val activeProcesses = ConcurrentHashMap<Int, ProcessTimeline>()
    private val finishedProcesses = CopyOnWriteArrayList<ProcessTimeline>()

    @Volatile
    private var running = false

    private var workerThread: Thread? = null

    // ======================
    // 对外 API
    // ======================

    fun start() {
        if (running) return
        running = true

        workerThread = Thread {
            while (running) {
                try {
                    tick()
                    Thread.sleep(scanIntervalMs)
                } catch (_: InterruptedException) {
                } catch (_: Throwable) {
                }
            }
        }.apply { start() }
    }

    fun stop() {
        running = false
        workerThread?.interrupt()
        workerThread = null
    }

    fun buildUiList(): List<ProcessUiModel> {
        val now = readUptimeSeconds()

        val runningList = activeProcesses.values.map {
            ProcessUiModel(pid = it.pid, packageName = it.packageName,
                aliveSeconds = it.durationSec(now), isRunning = true)
        }

        val finishedList = finishedProcesses.map {
            ProcessUiModel(pid = it.pid, packageName = it.packageName,
                aliveSeconds = it.durationSec(now), isRunning = false)
        }

        return (runningList + finishedList).sortedByDescending { it.aliveSeconds }
    }

    fun clearFinished() {
        finishedProcesses.clear()
    }

    // ======================
    // 核心扫描逻辑
    // ======================

    private fun tick() {
        val nowUptime = readUptimeSeconds()
        val scanned = scanProcesses()

        // 新进程
        scanned.values.forEach { info ->
            activeProcesses.putIfAbsent(info.pid,
                ProcessTimeline(pid = info.pid, packageName = info.packageName,
                    startUptimeSec = info.startUptimeSec))
        }

        // 已结束进程
        val iterator = activeProcesses.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!scanned.containsKey(entry.key)) {
                entry.value.endUptimeSec = nowUptime
                finishedProcesses.add(entry.value)
                iterator.remove()
            }
        }
    }

    // ======================
    // 扫描 /proc
    // ======================

    private fun scanProcesses(): Map<Int, ScanInfo> {
        val result = HashMap<Int, ScanInfo>()

        val hz = Os.sysconf(OsConstants._SC_CLK_TCK)

        File("/proc").listFiles()?.forEach { file ->
            val pid = file.name.toIntOrNull() ?: return@forEach

            try {
                if (!isAppProcess(pid)) return@forEach

                val pkg = readPackageName(pid) ?: return@forEach
                val startJiffies = readProcessStartJiffies(pid)
                if (startJiffies <= 0) return@forEach

                val startUptime = startJiffies.toDouble() / hz

                result[pid] = ScanInfo(pid = pid, packageName = pkg, startUptimeSec = startUptime)
            } catch (_: Throwable) {
                // 进程可能在扫描过程中结束
            }
        }
        return result
    }

    // ======================
    // /proc 工具方法
    // ======================

    private fun readUptimeSeconds(): Double {
        val text = File("/proc/uptime").readText()
        return text.substringBefore(" ").toDouble()
    }

    private fun readProcessStartJiffies(pid: Int): Long {
        val stat = File("/proc/$pid/stat").readText()
        val end = stat.lastIndexOf(')')
        if (end < 0) return -1

        val after = stat.substring(end + 2)
        val fields = after.split(" ")

        // starttime is 22nd field
        return fields[19].toLong()
    }

    private fun readPackageName(pid: Int): String? {
        val file = File("/proc/$pid/cmdline")
        if (!file.exists()) return null

        val bytes = file.readBytes()
        val end = bytes.indexOf(0)
        if (end <= 0) return null

        return String(bytes, 0, end)
    }

    private fun isAppProcess(pid: Int): Boolean {
        val status = File("/proc/$pid/status")
        if (!status.exists()) return false

        val uidLine = status.readLines().firstOrNull { it.startsWith("Uid:") } ?: return false

        val uid = uidLine.split("\\s+".toRegex())[1].toInt()
        return uid >= 10000
    }
}
