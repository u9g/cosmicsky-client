package dev.u9g.features

import dev.u9g.events.ItemStackCooldownProgressCallback

object PetCooldowns {
    // cooldowns in minutes
    private val knownCooldowns = mapOf(
        "LLAMA" to 24 * 60,
        //"FARMER" to 1, // Has no active ability
        "BATTLE_PIG" to 5,
        "MINER_MATT" to 10,
        //"SLAYER_SAM" to 4, // Has no active ability
        "CHAOS_COW" to 5,
        "BLACKSMITH_BRANDON" to 20,
        "FISHERMAN_FRED" to 10,
        "ALCHEMIST_ALEX" to 2 * 60,
        "BLOOD_SHEEP" to 5,
        "MERCHANT" to 15,
        "DIRE_WOLF" to 15
    )

    init {
        ItemStackCooldownProgressCallback.event.register {
            if (Settings.enableMod) {
                it.item.nbt?.getString("petType")?.let { petType ->
                    knownCooldowns[petType]?.let { petCooldown ->
                        val lastUsed = it.item.nbt?.getLong("lastUsed") ?: 0L
                        val lastUsedMinutesAgo = (System.currentTimeMillis() - lastUsed) / 1000 / 60
                        it.overrideProgress(1 - (lastUsedMinutesAgo.toFloat() / petCooldown).coerceAtMost(1f))
                    }
                }
            }
        }
    }
}