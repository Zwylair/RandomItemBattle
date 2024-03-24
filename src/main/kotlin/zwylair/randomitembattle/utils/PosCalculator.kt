package zwylair.randomitembattle.utils

import net.minecraft.util.math.Vec3d

class PosCalculator {
    companion object {
        fun posToCoord(number: Double): Int {
            return if (number < 0) (number - 1).toInt() else number.toInt()
        }

        fun roundPosition(position: Vec3d): Vec3d {
            var x = position.x.toInt().toDouble()
            x += if (position.x < 0) -0.5 else 0.5
            var z = position.z.toInt().toDouble()
            z += if (position.z < 0) -0.5 else 0.5
            return Vec3d(x, position.y, z)
        }
    }
}
