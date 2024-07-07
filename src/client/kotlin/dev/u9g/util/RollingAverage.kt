package dev.u9g.util

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
        }, 200, 200)
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
            return events.size / 1.0
        }
    }

    private fun cleanUpOldEvents() {
        val inLastInterval = System.currentTimeMillis() - 1000
        synchronized(lock) {
            while (events.isNotEmpty() && events.peekFirst() < inLastInterval) {
                events.removeFirst()
            }
        }
    }
}
