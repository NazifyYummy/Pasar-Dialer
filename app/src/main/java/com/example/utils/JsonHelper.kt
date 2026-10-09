package com.example.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonHelper {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun cleanPythonJson(input: String): String {
        var cleaned = input.trim()
        
        // Python booleans and None values
        cleaned = cleaned.replace("True", "true")
        cleaned = cleaned.replace("False", "false")
        cleaned = cleaned.replace("None", "null")
        
        // Check if list starts or is surrounded by single quotes but otherwise structure uses single quotes.
        // A simple pass of replacing ' with " works robustly for standard fields without nested single-quotes in text.
        cleaned = cleaned.replace("'", "\"")
        
        return cleaned
    }

    fun parseImportedData(rawInput: String): List<ParsedItem> {
        val cleaned = cleanPythonJson(rawInput)
        val listType = Types.newParameterizedType(List::class.java, Map::class.java)
        val adapter = moshi.adapter<List<Map<String, Any>>>(listType)
        
        val rawList = try {
            adapter.fromJson(cleaned)
        } catch (e: Exception) {
            // Fallback to raw string just in case it was already valid or needs direct adapter parsing
            try {
                adapter.fromJson(rawInput)
            } catch (ex: Exception) {
                null
            }
        } ?: return emptyList()

        return rawList.mapNotNull { itemMap ->
            // Try different variants of 'number'
            val number = (itemMap["number"] ?: itemMap["phone"] ?: itemMap["phoneNumber"])?.toString()?.trim() ?: return@mapNotNull null
            
            // Extract or parse other_informations
            val otherInfoRaw = itemMap["other_informations"] ?: itemMap["other_information"] ?: itemMap["other_info"] ?: itemMap["info"]
            val otherInfoMap = when (otherInfoRaw) {
                is Map<*, *> -> {
                    otherInfoRaw.entries.associate { entry ->
                        entry.key.toString() to entry.value.toString()
                    }
                }
                else -> emptyMap()
            }
            ParsedItem(number = number, otherInformations = otherInfoMap)
        }
    }

    fun mapToJson(map: Map<String, String>): String {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        return moshi.adapter<Map<String, String>>(type).toJson(map)
    }

    fun jsonToMap(json: String): Map<String, String> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        return try {
            moshi.adapter<Map<String, String>>(type).fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

data class ParsedItem(
    val number: String,
    val otherInformations: Map<String, String>
)
