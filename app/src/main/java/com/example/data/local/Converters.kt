package com.example.data.local

import androidx.room.TypeConverter

/**
 * Room converters for the small string collections carried by admin-authored content.
 *
 * The values are user-authored free text, so the separator has to be something a human would never
 * type. U+001F (ASCII "unit separator") exists for exactly this purpose and cannot be produced from
 * a soft keyboard; any stray occurrence is stripped on write so a round trip can never lose or
 * split a field.
 */
class Converters {

    @TypeConverter
    fun fromStringList(values: List<String>?): String {
        if (values.isNullOrEmpty()) return ""
        return values.joinToString(SEPARATOR) { it.replace(SEPARATOR, " ") }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(SEPARATOR).filter { it.isNotBlank() }
    }

    companion object {
        const val SEPARATOR = "\u001F"
    }
}
