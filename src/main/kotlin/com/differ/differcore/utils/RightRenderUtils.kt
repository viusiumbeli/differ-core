package com.differ.differcore.utils

import com.google.common.collect.MapDifference
import freemarker.ext.beans.HashAdapter

class RightRenderUtils {
    companion object {
        fun shouldRenderMethod(tag: String, content: HashAdapter?): Boolean {
            if (content == null) {
                return false
            }

            return (content["tags"] as List<*>)
                .stream()
                .filter { it != null }
                .map {
                    when (it) {
                        is MapDifference.ValueDifference<*> -> tag == it.rightValue()
                        is String -> it == tag
                        else -> false
                    }
                }
                .findAny()
                .get()
        }

        fun attributeValue(attribute: Map<*, *>, value: String) =
            with(attribute[value]) {
                when (this) {
                    is MapDifference.ValueDifference<*> -> rightValue()
                    else -> this
                }
            }

        fun containsMethod(left: Map<String, Any?>, path: String, method: String): Boolean {
            val methods = ((left.values.first() as Map<*, *>)["paths"] as Map<*, *>)[path]
            return if (methods != null) {
                (methods as Map<*, *>).containsKey(method)
            } else {
                false
            }
        }
    }
}