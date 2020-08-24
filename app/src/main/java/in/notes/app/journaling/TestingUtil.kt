package `in`.notes.app.journaling

import android.os.Build
import androidx.annotation.RequiresApi

object TestingUtil {
    @RequiresApi(Build.VERSION_CODES.N)
    fun fib(n: Int): Long {
        if(n == 0 || n == 1) {
            return n.toLong()
        }
        var a = 0L
        var b = 1L
        var c = 1L
        (1..n-1).forEach { i ->
            c = a + b
            a = b
            b = c
        }
        return c
    }

    /**
     * Checks if the braces are set correctly
     * e.g. "(a * b))" should return false
     */
   /* fun checkBraces(string: String): Boolean {
        return string.length { it == '(' } == string.count { it == ')' }
    }*/
}