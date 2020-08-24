package `in`.notes.app.journaling

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TestingUtilTest{

    @Test
    fun `fibo test`(){
        val result = TestingUtil.fib(6)

        assertThat(result).isEqualTo(8)
    }
}