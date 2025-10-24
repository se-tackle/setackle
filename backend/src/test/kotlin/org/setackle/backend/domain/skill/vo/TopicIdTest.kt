package org.setackle.backend.domain.skill.vo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * TopicId 값 객체 단위 테스트
 */
class TopicIdTest {
    @Test
    fun `양수 값으로 TopicId 생성 성공`() {
        // when
        val topicId = TopicId.of(123L)

        // then
        assertEquals(123L, topicId.value)
    }

    @Test
    fun `0 이하 값으로 생성 시 예외 발생`() {
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            TopicId.of(0L)
        }
        assertTrue(exception.message!!.contains("양수여야 합니다"))
    }

    @Test
    fun `음수 값으로 생성 시 예외 발생`() {
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            TopicId.of(-1L)
        }
        assertTrue(exception.message!!.contains("양수여야 합니다"))
    }

    @Test
    fun `ofOrNull - 양수 값으로 생성 성공`() {
        // when
        val topicId = TopicId.ofOrNull(456L)

        // then
        assertNotNull(topicId)
        assertEquals(456L, topicId?.value)
    }

    @Test
    fun `ofOrNull - null 입력 시 null 반환`() {
        // when
        val topicId = TopicId.ofOrNull(null)

        // then
        assertNull(topicId)
    }

    @Test
    fun `ofOrNull - 0 이하 값 입력 시 null 반환`() {
        // when
        val topicId1 = TopicId.ofOrNull(0L)
        val topicId2 = TopicId.ofOrNull(-1L)

        // then
        assertNull(topicId1)
        assertNull(topicId2)
    }

    @Test
    fun `newTopic - null 반환 (DB 저장 전)`() {
        // when
        val topicId = TopicId.newTopic()

        // then
        assertNull(topicId)
    }

    @Test
    fun `toString - value를 문자열로 반환`() {
        // given
        val topicId = TopicId.of(789L)

        // when
        val string = topicId.toString()

        // then
        assertEquals("789", string)
    }

    @Test
    fun `동일한 값으로 생성된 TopicId는 동등하다`() {
        // given
        val topicId1 = TopicId.of(100L)
        val topicId2 = TopicId.of(100L)

        // then
        assertEquals(topicId1, topicId2)
    }

    @Test
    fun `다른 값으로 생성된 TopicId는 다르다`() {
        // given
        val topicId1 = TopicId.of(100L)
        val topicId2 = TopicId.of(200L)

        // then
        assertTrue(topicId1 != topicId2)
    }
}
