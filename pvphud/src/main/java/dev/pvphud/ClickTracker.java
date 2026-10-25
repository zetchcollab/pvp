package dev.pvphud;

import java.util.ArrayDeque;
import java.util.Deque;

/** Считает клики за последнюю секунду по переднему фронту нажатия кнопки. */
public class ClickTracker {
    private final Deque<Long> clicks = new ArrayDeque<>();
    private boolean wasDown = false;

    public void update(boolean down) {
        long now = System.currentTimeMillis();
        if (down && !wasDown) {
            clicks.addLast(now);
        }
        wasDown = down;
        long cutoff = now - 1000;
        while (!clicks.isEmpty() && clicks.peekFirst() < cutoff) {
            clicks.pollFirst();
        }
    }

    public int cps() {
        return clicks.size();
    }
}
