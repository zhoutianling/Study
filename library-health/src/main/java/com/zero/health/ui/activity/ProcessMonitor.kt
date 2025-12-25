package com.zero.health.ui.activity


import java.util.concurrent.atomic.AtomicBoolean

/**
 * 基于 su 的进程监控器
 * - 扫描 /proc
 * - 统计进程 start / end / duration
 * - 适合实时 RecyclerView 展示
 */
class ProcessMonitor(private val scanIntervalMs: Long = 2000L,
                     private val targetPackages: Set<String>? = null) {

    // =========================
    // 对外 UI 使用的数据模型
    // =========================
    // =========================
    // 内部时间轴模型
    // =========================

    private data class Timeline(val pid: Int, val packageName: String, val startUptimeSec: Double,
                                var endUptimeSec: Double? = null, var restartCount: Int = 0) {
        fun duration(now: Double): Double = (endUptimeSec ?: now) - startUptimeSec
    }

    // =========================
    // Raw /proc 扫描结果
    // =========================

    private data class RawProc(val pid: Int, val cmdline: String, val stat: String)

    // =========================
    // 状态容器
    // =========================

    private val active = HashMap<Int, Timeline>()
    private val finished = ArrayList<Timeline>()
    private val processHistory = HashMap<String, Int>() // 记录每个包名的重启次数

    private val running = AtomicBoolean(false)
    private var workerThread: Thread? = null

    // =========================
    // Public API
    // =========================

    fun start() {
        if (running.get()) return
        running.set(true)

        workerThread = Thread {
            while (running.get()) {
                try {
                    tick()
                    Thread.sleep(scanIntervalMs)
                } catch (_: InterruptedException) {
                } catch (_: Throwable) {
                    // 防止单次异常导致线程退出
                }
            }
        }.apply { start() }
    }

    fun stop() {
        running.set(false)
        workerThread?.interrupt()
        workerThread = null
    }

    /**
     * 构建给 RecyclerView 使用的数据
     */
    fun buildUiList(): List<ProcessUiModel> {
        val now = readUptimeBySu()

        val runningList = active.values.map {
            ProcessUiModel(pid = it.pid, packageName = it.packageName,
                aliveSeconds = it.duration(now), isRunning = true, restartCount = it.restartCount)
        }

        // 只返回正在运行的进程
        return runningList.sortedByDescending { it.aliveSeconds }
    }

    // =========================
    // 核心扫描逻辑
    // =========================

    private fun tick() {
        val now = readUptimeBySu()
        val hz = readHzBySu()
        val scanned = scanProcessesBySu().associateBy { it.pid }

        // 新进程
        for (proc in scanned.values) {
            if (!active.containsKey(proc.pid)) {
                val startJiffies = parseStartJiffies(proc.stat)
                if (startJiffies <= 0) continue

                // 过滤非目标包名的进程
                if (!isTargetPackage(proc.cmdline)) continue

                val startUptime = startJiffies.toDouble() / hz

                // 检查是否为重启的进程
                var restartCount = 0
                val existingCount = processHistory[proc.cmdline]
                if (existingCount != null) {
                    restartCount = existingCount + 1
                }
                processHistory[proc.cmdline] = restartCount

                active[proc.pid] = Timeline(pid = proc.pid, packageName = proc.cmdline,
                    startUptimeSec = startUptime, restartCount = restartCount)
            }
        }

        // 已结束进程
        val it = active.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (!scanned.containsKey(entry.key)) {
                entry.value.endUptimeSec = now
                finished.add(entry.value)
                it.remove()
            }
        }
    }

    private fun isTargetPackage(packageName: String): Boolean {
        // 如果没有指定目标包名，则不过滤（返回true表示不跳过）
        if (targetPackages == null) return true

        // 检查包名是否在目标列表中
        return targetPackages.any { packageName.contains(it) }
    }

    // =========================
    // su 扫描 /proc
    // =========================

    private fun scanProcessesBySu(): List<RawProc> {
        val script = """
            for p in /proc/[0-9]*; do
              pid=${'$'}{p##*/}
              stat=$(cat ${'$'}p/stat 2>/dev/null) || continue
              cmd=$(tr '\0' ' ' < ${'$'}p/cmdline 2>/dev/null)
              [ -z "${'$'}cmd" ] && continue
              echo "${'$'}pid|${'$'}cmd|${'$'}stat"
            done
        """.trimIndent()

        return runSu(script).mapNotNull { line ->
            val first = line.indexOf('|')
            val second = line.indexOf('|', first + 1)
            if (first <= 0 || second <= first) return@mapNotNull null

            val pid = line.substring(0, first).toIntOrNull() ?: return@mapNotNull null
            val cmd = line.substring(first + 1, second).trim()
            val stat = line.substring(second + 1)

            RawProc(pid, cmd, stat)
        }
    }

    // =========================
    // /proc 解析
    // =========================

    private fun parseStartJiffies(stat: String): Long {
        val end = stat.lastIndexOf(')')
        if (end < 0) return -1

        val after = stat.substring(end + 2)
        val fields = after.split(" ")
        return fields.getOrNull(19)?.toLongOrNull() ?: -1
    }

    // =========================
    // su 工具方法
    // =========================

    private fun readUptimeBySu(): Double {
        return runSu("cat /proc/uptime").firstOrNull()?.substringBefore(" ")?.toDoubleOrNull()
            ?: 0.0
    }

    private fun readHzBySu(): Int {
        return runSu("getconf CLK_TCK").firstOrNull()?.toIntOrNull() ?: 100
    }

    private fun runSu(cmd: String): List<String> {
        val process = Runtime.getRuntime().exec("su")
        val result = ArrayList<String>()

        process.outputStream.bufferedWriter().use { w ->
            w.write(cmd)
            w.newLine()
            w.write("exit")
            w.newLine()
            w.flush()
        }

        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { result.add(it) }
        }

        process.waitFor()
        return result
    }

    /**
     * 杀死指定PID的进程
     */
    fun killProcess(pid: Int): Boolean {
        try {
            val result = runSu("kill $pid")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 杀死所有目标包名的进程
     */
    fun killAllTargetProcesses(): Map<Int, Boolean> {
        val results = mutableMapOf<Int, Boolean>()
        val currentActive = active.toMap() // 创建快照防止并发问题
        
        for ((pid, _) in currentActive) {
            results[pid] = killProcess(pid)
        }
        
        return results
    }
}

data class ProcessUiModel(val pid: Int, val packageName: String, val aliveSeconds: Double,
                          val isRunning: Boolean, val restartCount: Int = 0)

