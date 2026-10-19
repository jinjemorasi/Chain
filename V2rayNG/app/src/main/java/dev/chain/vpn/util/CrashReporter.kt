package dev.chain.vpn.util

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Captures uncaught exceptions to a file so a crash report can be shown
 * (and copied) on the next launch. Installed once per process from
 * [dev.chain.vpn.AngApplication.onCreate].
 */
object CrashReporter {
    private const val FILE_NAME = "chain_last_crash.txt"
    private const val MAX_CHARS = 24_000

    fun install(app: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeCrash(app.applicationContext, thread, throwable)
            } catch (_: Exception) {
                // Never break the crash chain from inside the handler.
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun readLastCrash(app: Context): String? {
        return try {
            val file = File(app.applicationContext.filesDir, FILE_NAME)
            if (file.exists()) file.readText().takeIf { it.isNotBlank() } else null
        } catch (_: Exception) {
            null
        }
    }

    fun clearLastCrash(app: Context) {
        try {
            File(app.applicationContext.filesDir, FILE_NAME).delete()
        } catch (_: Exception) {
        }
    }

    private fun writeCrash(app: Context, thread: Thread, throwable: Throwable) {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val report = buildString {
            appendLine("Chain crash @ $stamp")
            appendLine("Thread: ${thread.name}")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} / Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("Package: ${app.packageName}")
            appendLine()
            appendLine(Log.getStackTraceString(throwable))
        }.take(MAX_CHARS)
        File(app.filesDir, FILE_NAME).writeText(report)
    }
}
