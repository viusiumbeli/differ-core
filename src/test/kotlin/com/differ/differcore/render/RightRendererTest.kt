package com.differ.differcore.render

import com.google.common.collect.MapDifference
import freemarker.ext.beans.HashAdapter
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.Spy

internal class RightRendererTest {
    @Spy
    lateinit var rightRenderer: RightRenderer


    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
    }


    @Test
    fun versionSelected_whenListIsEmptyAndVersionIsNotNull_thenReturnNull() =
        assertThat(rightRenderer.versionSelected(emptyList(), "1"), nullValue())

    @Test
    fun versionSelected_whenListIsEmptyAndVersionIsNull_thenReturnNull() =
        assertThat(rightRenderer.versionSelected(emptyList(), null), nullValue())

    @Test
    fun versionSelected_whenListWithOneElementAndContainsVersion_thenReturnValue() =
        assertThat(rightRenderer.versionSelected(listOf("1"), "1"), equalTo("1"))

    @Test
    fun versionSelected_whenListWithOneElementAndNotContainsVersion_thenReturnFirst() =
        assertThat(rightRenderer.versionSelected(listOf("1"), "2"), equalTo("1"))

    @Test
    fun versionSelected_whenListWithOneElementAndVersionIsNull_thenReturnFirst() =
        assertThat(rightRenderer.versionSelected(listOf("1"), null), equalTo("1"))

    @Test
    fun versionSelected_whenListWithMultipleElementAndVersionIsNull_thenReturnFirst() =
        assertThat(rightRenderer.versionSelected(listOf("1", "2", "3"), null), equalTo("1"))

    @Test
    fun versionSelected_whenListWithMultipleElementAndContainsVersion_thenReturnVersion() =
        assertThat(rightRenderer.versionSelected(listOf("1", "2", "3"), "2"), equalTo("2"))

    @Test
    fun versionSelected_whenListWithMultipleElementAndNotContainsVersion_thenReturnFirst() =
        assertThat(rightRenderer.versionSelected(listOf("1", "2", "3"), null), equalTo("1"))


    @Test
    fun isFullyRemovedMethod_shouldThrowException() {
        assertThrows(UnsupportedOperationException::class.java) {
            rightRenderer.isFullyRemovedMethod(mapOf(), mapOf(), "", "")
        }
    }


    @Test
    fun attributeValue_whenMapNotContainsAttribute_shouldReturnNull() =
        assertThat(rightRenderer.attributeValue(mapOf<String, String>(), "1"), nullValue())

    @Test
    fun attributeValue_whenMapContainsStringAttribute_shouldReturnValue() =
        assertThat(rightRenderer.attributeValue(mapOf("1" to "2"), "1") as String, equalTo("2"))

    @Test
    fun attributeValue_whenMapContainsValueDifferenceAttribute_shouldReturnRightValue() {
        val valueDifference = mock(MapDifference.ValueDifference::class.java)
        doReturn("3").`when`(valueDifference).rightValue()
        assertThat(rightRenderer.attributeValue(mapOf("1" to valueDifference), "1") as String, equalTo("3"))
    }


    @Test
    fun shouldRenderMethod_whenContentIsNull_shouldReturnFalse() =
        assertThat(rightRenderer.shouldRenderMethod("tags", null), `is`(false))

    @Test
    fun shouldRenderMethod_whenContentNotContainsTagsKey_shouldReturnFalse() {
        val hashAdapter = mock(HashAdapter::class.java)
        doReturn(null).`when`(hashAdapter)["tags"]
        assertThat(rightRenderer.shouldRenderMethod("tags", hashAdapter), `is`(false))
    }

    @Test
    fun shouldRenderMethod_whenContentTagValueIsOnlyNull_shouldReturnFalse() {
        val hashAdapter = mock(HashAdapter::class.java)
        doReturn(true).`when`(hashAdapter).containsKey("tags")
        doReturn(listOf(null)).`when`(hashAdapter)["tags"]
        assertThat(rightRenderer.shouldRenderMethod("tag", hashAdapter), `is`(false))
    }

    @Test
    fun shouldRenderMethod_whenContentNotContainsTag_shouldReturnFalse() {
        val hashAdapter = mock(HashAdapter::class.java)
        doReturn(true).`when`(hashAdapter).containsKey("tags")
        doReturn(listOf(null, "asdf")).`when`(hashAdapter)["tags"]
        assertThat(rightRenderer.shouldRenderMethod("tag", hashAdapter), `is`(false))
    }

    @Test
    fun shouldRenderMethod_whenContentContainsTag_shouldReturnTrue() {
        val hashAdapter = mock(HashAdapter::class.java)
        doReturn(true).`when`(hashAdapter).containsKey("tags")
        doReturn(listOf(null, "tag")).`when`(hashAdapter)["tags"]
        assertThat(rightRenderer.shouldRenderMethod("tag", hashAdapter), `is`(true))
    }

    @Test
    fun shouldRenderMethod_whenContentContainsValueDifferenceTag_shouldReturnTrue() {
        val valueDifference = mock(MapDifference.ValueDifference::class.java)
        doReturn("tag").`when`(valueDifference).rightValue()

        val hashAdapter = mock(HashAdapter::class.java)
        doReturn(true).`when`(hashAdapter).containsKey("tags")
        doReturn(listOf(null, valueDifference)).`when`(hashAdapter)["tags"]
        assertThat(rightRenderer.shouldRenderMethod("tag", hashAdapter), `is`(true))
    }

}