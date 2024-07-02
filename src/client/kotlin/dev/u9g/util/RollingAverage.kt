package dev.u9g.util;

import java.util.*

class RollingAverage {
    private val events: Deque<Long> = LinkedList()
    private val lock = Any()
    private val timer = Timer(true)

    init {
        // Schedule a task to clean up outdated events every second
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                cleanUpOldEvents()
            }
        }, 1000, 1000)
    }

    fun addEvent() {
        synchronized(lock) {
            val now = System.currentTimeMillis()
            events.addLast(now)
        }
    }

    fun getRollingAverage(): Double {
        synchronized(lock) {
            cleanUpOldEvents()
            return events.size / 60.0
        }
    }

    private fun cleanUpOldEvents() {
        val oneMinuteAgo = System.currentTimeMillis() - 60000
        synchronized(lock) {
            while (events.isNotEmpty() && events.peekFirst() < oneMinuteAgo) {
                events.removeFirst()
            }
        }
    }
}
