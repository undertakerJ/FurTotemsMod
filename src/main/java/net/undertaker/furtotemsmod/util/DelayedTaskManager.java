package net.undertaker.furtotemsmod.util;

import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.undertaker.furtotemsmod.FurTotemsMod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID)
public class DelayedTaskManager {
    private static final Map<UUID, DelayedTask> delayedTasks = new ConcurrentHashMap<>();

    public static void addTask(UUID id, int delayTicks, Runnable task) {
        delayedTasks.put(id, new DelayedTask(delayTicks, task));
    }

    public static void cancelTask(UUID id) {
        delayedTasks.remove(id);
    }

    public static boolean isTaskCancelled(UUID id) {
        return !delayedTasks.containsKey(id);
    }
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        delayedTasks.entrySet().removeIf(entry -> {
            DelayedTask task = entry.getValue();

            if (task.tick()) {
                task.run();
                return true;
            }
            return false;
        });
    }
    public static long getTimeRemaining(BlockPos pos) {
        DelayedTask task = delayedTasks.get(pos);
        return task != null ? task.getRemainingTicks() : -1;
    }

    private static class DelayedTask {
        private int remainingTicks;
        private final Runnable task;

        public DelayedTask(int delayTicks, Runnable task) {
            this.remainingTicks = delayTicks;
            this.task = task;
        }
        public boolean tick() {
            return --remainingTicks <= 0;
        }
        public void run() {
            task.run();
        }
        public int getRemainingTicks() {
            return remainingTicks;
        }
    }
}
