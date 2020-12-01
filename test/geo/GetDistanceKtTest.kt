package geo

import com.twoilya.lonelyboardgamer.geo.getDistance
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GetDistanceKtTest {
    @Test
    fun `Small box and points in middle`() {
        assertEquals(
            13.21,
            getDistance((50.0 to 50.0), (50.1 to 50.1))
        )
    }

    @Test
    fun `Big box and points in middle`() {
        assertEquals(
            792.79,
            getDistance((50.0 to 53.0), (57.1 to 54.1))
        )
    }

    @Test
    fun `Small box and points on side`() {
        assertEquals(
            7.15,
            getDistance((50.0 to 50.0), (50.0 to 50.1))
        )
    }

    @Test
    fun `Big box and points on side`() {
        assertEquals(
            1432.3,
            getDistance((50.0 to 50.0), (50.0 to 70.1))
        )
    }
}