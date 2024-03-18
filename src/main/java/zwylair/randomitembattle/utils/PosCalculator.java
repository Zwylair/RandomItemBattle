package zwylair.randomitembattle.utils;

import net.minecraft.util.math.Vec3d;

public class PosCalculator {
    public static int posToCoord(double number) {
        if (number < 0) { return (int) number - 1; } else { return (int) number; }
    }

    public static Vec3d roundPosition(Vec3d position) {
        double x = (int) position.getX();
        if (position.getX() < 0) { x -= 0.5; } else { x += 0.5; }
        double z = (int) position.getZ();
        if (position.getZ() < 0) { z -= 0.5; } else { z += 0.5; }

        return new Vec3d(x, position.getY(), z);
    }
}
