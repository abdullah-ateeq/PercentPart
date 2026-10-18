package com.example.percentpart.repository

import android.content.SharedPreferences
import com.example.percentpart.data.InMemoryKeyValueStore
import com.example.percentpart.data.KeyValueStore
import com.example.percentpart.data.SharedPreferencesKeyValueStore
import com.example.percentpart.model.PercentInputType
import com.example.percentpart.model.PercentPartHistory
import com.example.percentpart.model.PercentPartStatus
import org.json.JSONArray
import org.json.JSONObject

class PercentPartHistoryRepository(
    private val store: KeyValueStore = InMemoryKeyValueStore()
) {
    constructor(sharedPreferences: SharedPreferences) : this(
        SharedPreferencesKeyValueStore(sharedPreferences)
    )

    companion object {
        private const val KEY_HISTORY = "percent_part_history_v1"
        const val MAX_HISTORY_ITEMS = 10
        private const val FIELD_SEPARATOR = "\u001F"
        private const val RECORD_SEPARATOR = "\u001E"
    }

    fun loadHistory(): List<PercentPartHistory> {
        val raw = store.getString(KEY_HISTORY, null) ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return deserializeHistory(raw)
    }

    fun saveCalculation(item: PercentPartHistory): List<PercentPartHistory> {
        val current = loadHistory().toMutableList()
        // Remove existing item with same ID if present
        current.removeAll { it.id == item.id }
        // Insert newest at index 0
        current.add(0, item)
        // Enforce maximum 10 entries (removing oldest at the end)
        val trimmed = if (current.size > MAX_HISTORY_ITEMS) {
            current.take(MAX_HISTORY_ITEMS)
        } else {
            current.toList()
        }
        store.putString(KEY_HISTORY, serializeHistory(trimmed))
        return trimmed
    }

    fun deleteCalculation(id: String): List<PercentPartHistory> {
        val updated = loadHistory().filterNot { it.id == id }
        store.putString(KEY_HISTORY, serializeHistory(updated))
        return updated
    }

    fun clearHistory(): List<PercentPartHistory> {
        store.remove(KEY_HISTORY)
        return emptyList()
    }

    private fun serializeHistory(items: List<PercentPartHistory>): String {
        return items.joinToString(RECORD_SEPARATOR) { item ->
            listOf(
                item.id,
                item.partValue,
                item.wholeValue,
                item.percentage,
                item.remainingPercentage ?: "",
                item.status.name,
                item.inputType.name,
                item.currencyCode ?: "",
                item.currencySymbol ?: "",
                item.calculatedAt.toString(),
                item.currencyDecimalPlaces?.toString() ?: ""
            ).joinToString(FIELD_SEPARATOR)
        }
    }

    private fun deserializeHistory(serialized: String): List<PercentPartHistory> {
        // Support both custom delimiter format (works on JVM & Android without org.json stub issues) and JSON fallback
        if (serialized.startsWith("[")) {
            return try {
                val array = JSONArray(serialized)
                val list = mutableListOf<PercentPartHistory>()
                for (i in 0 until array.length()) {
                    val obj: JSONObject = array.getJSONObject(i)
                    list.add(
                        PercentPartHistory(
                            id = obj.getString("id"),
                            partValue = obj.getString("partValue"),
                            wholeValue = obj.getString("wholeValue"),
                            percentage = obj.getString("percentage"),
                            remainingPercentage = obj.optString("remainingPercentage").ifEmpty { null },
                            status = PercentPartStatus.valueOf(obj.getString("status")),
                            inputType = PercentInputType.valueOf(obj.getString("inputType")),
                            currencyCode = obj.optString("currencyCode").ifEmpty { null },
                            currencySymbol = obj.optString("currencySymbol").ifEmpty { null },
                            calculatedAt = obj.getLong("calculatedAt"),
                            currencyDecimalPlaces = if (obj.has("currencyDecimalPlaces")) obj.getInt("currencyDecimalPlaces") else null
                        )
                    )
                }
                list.take(MAX_HISTORY_ITEMS)
            } catch (_: Exception) {
                emptyList()
            }
        }

        return serialized.split(RECORD_SEPARATOR)
            .mapNotNull { record ->
                if (record.isBlank()) return@mapNotNull null
                val parts = record.split(FIELD_SEPARATOR)
                if (parts.size < 10) return@mapNotNull null
                try {
                    PercentPartHistory(
                        id = parts[0],
                        partValue = parts[1],
                        wholeValue = parts[2],
                        percentage = parts[3],
                        remainingPercentage = parts[4].ifEmpty { null },
                        status = PercentPartStatus.valueOf(parts[5]),
                        inputType = PercentInputType.valueOf(parts[6]),
                        currencyCode = parts[7].ifEmpty { null },
                        currencySymbol = parts[8].ifEmpty { null },
                        calculatedAt = parts[9].toLong(),
                        currencyDecimalPlaces = parts.getOrNull(10)?.toIntOrNull()
                    )
                } catch (_: Exception) {
                    null
                }
            }
            .take(MAX_HISTORY_ITEMS)
    }
}
