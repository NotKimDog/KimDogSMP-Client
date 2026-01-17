package kimdog.kimdog_smp.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Mathematical utility functions for VeinMiner
 * Handles distance calculations, position operations, and conversions
 */
public class MathUtils {

    /**
     * Calculate distance between two block positions
     */
    public static double distance(BlockPos pos1, BlockPos pos2) {
        double dx = pos1.getX() - pos2.getX();
        double dy = pos1.getY() - pos2.getY();
        double dz = pos1.getZ() - pos2.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculate distance between two Vec3d positions
     */
    public static double distance(Vec3d pos1, Vec3d pos2) {
        return pos1.distanceTo(pos2);
    }

    /**
     * Check if two positions are adjacent (distance = 1)
     */
    public static boolean isAdjacent(BlockPos pos1, BlockPos pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) <= 1 &&
               Math.abs(pos1.getY() - pos2.getY()) <= 1 &&
               Math.abs(pos1.getZ() - pos2.getZ()) <= 1 &&
               !pos1.equals(pos2);
    }

    /**
     * Calculate taxi-cab (Manhattan) distance
     */
    public static int manhattanDistance(BlockPos pos1, BlockPos pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) +
               Math.abs(pos1.getY() - pos2.getY()) +
               Math.abs(pos1.getZ() - pos2.getZ());
    }

    /**
     * Clamp a value between min and max
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamp a double value between min and max
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Lerp (linear interpolation) between two values
     */
    public static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }

    /**
     * Normalize a value to 0-1 range
     */
    public static double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    /**
     * Calculate percentage
     */
    public static int percentage(long current, long max) {
        if (max <= 0) return 0;
        return (int) ((current * 100) / max);
    }

    /**
     * Convert BlockPos to Vec3d (center of block)
     */
    public static Vec3d blockPosToVec3d(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    /**
     * Round a double to n decimal places
     */
    public static double round(double value, int places) {
        double multiplier = Math.pow(10, places);
        return Math.round(value * multiplier) / multiplier;
    }

    /**
     * Check if a value is within range (inclusive)
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
}
