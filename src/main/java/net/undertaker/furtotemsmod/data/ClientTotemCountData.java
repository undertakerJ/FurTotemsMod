package net.undertaker.furtotemsmod.data;

public class ClientTotemCountData {
    private static int smallTotems = 0;
    private static int bigTotems = 0;

    public static void updateCounts(int newSmallTotems, int newBigTotems) {
        smallTotems = newSmallTotems;
        bigTotems = newBigTotems;
    }

    public static int getSmallTotems() {
        return smallTotems;
    }

    public static int getBigTotems() {
        return bigTotems;
    }
}
