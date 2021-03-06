package com.differ.differcore.service

import com.differ.differcore.utils.asMutableListOfType
import com.differ.differcore.utils.asMutableMapOfType
import com.differ.differcore.utils.isNegativeNumber
import com.differ.differcore.utils.on
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.math.absoluteValue

/**
 * Implementation of [MapTransformerService] interface.
 *
 * Service expand flattened map and vice versa.
 *
 * @author Vladislav Iusiumbeli
 * @since 1.0.0
 */
@Service
open class MapTransformerServiceImpl : MapTransformerService {
    private val log = LoggerFactory.getLogger(MapTransformerServiceImpl::class.java)

    /**
     * Flatten json data in map format.
     *
     * For example if there is such flatten map entry `{ 1 : { 2 : 4 } }`
     * it returns `{ 1.2 : 4 }`.
     *
     * @param mapToFlatten expanded json data.
     *
     * @return Flattened expanded map.
     */
    override fun flattenMap(mapToFlatten: Map<String, Any>): Map<String, Any?> =
        mapToFlatten.entries
            .stream()
            .map { it.key to it.value }
            .peek { log.debug("Pair before flatten: $it") }
            .flatMap { flatten(it, JSON_KEY_SEPARATOR) }
            .peek { log.debug("Pair after flatten: $it") }
            .collect(
                { LinkedHashMap() },
                { map, entry -> map[JSON_KEY_SEPARATOR + entry.first] = entry.second },
                { map, m -> map.putAll(m) })

    private fun flatten(entry: Pair<String, Any?>, keySeparator: String): Stream<Pair<String, Any?>> =
        with(entry.second) {
            when (this) {
                is Map<*, *> ->
                    entries.stream()
                        .flatMap { e -> flatten("${entry.first}$keySeparator${e.key}" to e.value, keySeparator) }
                is List<*> ->
                    IntStream.range(0, size)
                        .mapToObj { i -> "${entry.first}$keySeparator-$i" to this[i] }
                        .flatMap { flatten(it, keySeparator) }
                else ->
                    Stream.of(entry)
            }
        }


    /**
     * Expand flattened map to MutableMap.
     *
     * For example if there is such flatten map entry `{ 1.2 : 4 }`
     * it returns `{ 1 : { 2 : 4 } }`.
     *
     * @param flattenMap flattened json data.
     *
     * @return Expanded flattened map.
     */
    override fun expandToMapObjects(flattenMap: Map<String, Any?>): MutableMap<String, Any?> {
        log.debug("Flattened map before expand:")
        flattenMap.forEach { (key, value) -> log.debug("$key: $value") }

        val expandedMap = on(mutableMapOf<String, Any?>()) {
            flattenMap.entries.forEach { addEntry(it, this) }
        }

        log.debug("Flattened map after expand:")
        expandedMap.forEach { (key, value) -> log.debug("$key: $value") }
        return expandedMap
    }


    private fun addEntry(entry: Map.Entry<String, Any?>, jsonMap: MutableMap<String, Any?>) {
        val keyList = entry.key.substring(1).split(JSON_KEY_SEPARATOR)
        val key = keyList[0]
        val value = jsonMap[key]
        val secondKey = secondKey(keyList)
        var remainKey = joinKeyList(keyList, 1)
        var deeperMap = mutableMapOf<String, Any?>()
        when {
            value is MutableMap<*, *> -> deeperMap = value.asMutableMapOfType()!!

            keyList.size <= 2 && secondKey.isNegativeNumber() -> {
                val list = if (value is MutableList<*>) value else mutableListOf<Any>()
                populateList(list, entry.value)
                jsonMap[key] = list
                return
            }

            secondKey.isNegativeNumber() -> {
                jsonMap.putIfAbsent(key, mutableListOf(deeperMap))
                deeperMap = getExistingMapOrCreateNew(jsonMap[key] as MutableList<*>, secondKey)
                remainKey = joinKeyList(keyList, 2)
            }

            secondKey.isEmpty() -> {
                jsonMap[key] = entry.value
                return
            }

            else -> jsonMap[key] = deeperMap
        }
        addEntry(AbstractMap.SimpleEntry(remainKey, entry.value), deeperMap)
    }

    private fun getExistingMapOrCreateNew(value: MutableList<*>, secondKey: String): MutableMap<String, Any?> =
        when {
            value.size > secondKey.toInt().absoluteValue ->
                (value[secondKey.toInt().absoluteValue] as MutableMap<*, *>).asMutableMapOfType()!!
            else -> {
                val result = mutableMapOf<String, Any?>()
                populateList(value, result)
                result
            }
        }

    private fun populateList(list: MutableList<*>, value: Any?) = list.run { asMutableListOfType<Any?>()?.add(value) }

    private fun joinKeyList(keyList: List<String>, start: Int) =
        takeIf { start <= keyList.size }
            ?.let { keyList.drop(start).joinToString(JSON_KEY_SEPARATOR, JSON_KEY_SEPARATOR) }
            ?: ""

    private fun secondKey(keyList: List<String>) = takeIf { (keyList.size > 1) }?.let { keyList[1] } ?: ""

    companion object {

        /**
         * Joining json keys by this separator.
         */
        private const val JSON_KEY_SEPARATOR = "."
    }
}