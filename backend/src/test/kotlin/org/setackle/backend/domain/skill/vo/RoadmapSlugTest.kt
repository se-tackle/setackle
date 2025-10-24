package org.setackle.backend.domain.skill.vo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * RoadmapSlug 값 객체 단위 테스트
 */
class RoadmapSlugTest {
    @Test
    fun `정규화 - 대문자를 소문자로 변환`() {
        // when
        val slug = RoadmapSlug.of("Frontend Development")

        // then
        assertEquals("frontend-development", slug.value)
    }

    @Test
    fun `정규화 - 공백을 하이픈으로 변환`() {
        // when
        val slug = RoadmapSlug.of("Backend Developer Roadmap")

        // then
        assertEquals("backend-developer-roadmap", slug.value)
    }

    @Test
    fun `정규화 - 특수문자를 하이픈으로 치환`() {
        // when
        val slug = RoadmapSlug.of("React/Vue.js & Angular!")

        // then
        assertEquals("react-vue-js-angular", slug.value)
    }

    @Test
    fun `정규화 - 연속된 하이픈 제거`() {
        // when
        val slug = RoadmapSlug.of("Multiple   Spaces---Here")

        // then
        assertEquals("multiple-spaces-here", slug.value)
    }

    @Test
    fun `정규화 - 앞뒤 하이픈 제거`() {
        // when
        val slug = RoadmapSlug.of("  -Trim Hyphens-  ")

        // then
        assertEquals("trim-hyphens", slug.value)
    }

    @Test
    fun `빈 문자열로 생성 시 예외 발생`() {
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            RoadmapSlug.of("")
        }
        assertTrue(exception.message!!.contains("비어있을 수 없습니다"))
    }

    @Test
    fun `공백만 있는 문자열로 생성 시 예외 발생`() {
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            RoadmapSlug.of("   ")
        }
        assertTrue(exception.message!!.contains("비어있을 수 없습니다"))
    }

    @Test
    fun `최대 길이 초과 시 예외 발생`() {
        // given
        val longString = "a".repeat(101)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            RoadmapSlug.of(longString)
        }
        assertTrue(exception.message!!.contains("100자를 초과할 수 없습니다"))
    }

    @Test
    fun `예약어 사용 시 예외 발생`() {
        // given
        val reservedWords = listOf("admin", "api", "auth", "login", "logout")

        // when & then
        reservedWords.forEach { reserved ->
            val exception = assertThrows<IllegalArgumentException> {
                RoadmapSlug.of(reserved)
            }
            assertTrue(exception.message!!.contains("예약된 슬러그"))
        }
    }

    @Test
    fun `유효한 슬러그 검증`() {
        // given
        val slug = RoadmapSlug.of("valid-slug-123")

        // when
        val isValid = slug.isValid()

        // then
        assertTrue(isValid)
    }

    @Test
    fun `단어 개수 계산`() {
        // given
        val slug = RoadmapSlug.of("frontend-developer-roadmap")

        // when
        val count = slug.wordCount()

        // then
        assertEquals(3, count)
    }

    @Test
    fun `단일 단어 슬러그`() {
        // given
        val slug = RoadmapSlug.of("backend")

        // when
        val count = slug.wordCount()

        // then
        assertEquals(1, count)
    }

    @Test
    fun `ofOrNull - 유효한 값으로 생성 성공`() {
        // when
        val slug = RoadmapSlug.ofOrNull("Valid Slug")

        // then
        assertNotNull(slug)
        assertEquals("valid-slug", slug?.value)
    }

    @Test
    fun `ofOrNull - null 입력 시 null 반환`() {
        // when
        val slug = RoadmapSlug.ofOrNull(null)

        // then
        assertNull(slug)
    }

    @Test
    fun `ofOrNull - 예약어 입력 시 null 반환`() {
        // when
        val slug = RoadmapSlug.ofOrNull("admin")

        // then
        assertNull(slug)
    }

    @Test
    fun `isValidSlug - 유효한 슬러그 검증`() {
        // when
        val isValid = RoadmapSlug.isValidSlug("valid-slug-123")

        // then
        assertTrue(isValid)
    }

    @Test
    fun `isValidSlug - 예약어 검증 실패`() {
        // when
        val isValid = RoadmapSlug.isValidSlug("admin")

        // then
        assertFalse(isValid)
    }

    @Test
    fun `isValidSlug - 대문자 포함 시 검증 실패`() {
        // when
        val isValid = RoadmapSlug.isValidSlug("Invalid-Slug")

        // then
        assertFalse(isValid)
    }

    @Test
    fun `toString - value 반환`() {
        // given
        val slug = RoadmapSlug.of("test-slug")

        // when
        val string = slug.toString()

        // then
        assertEquals("test-slug", string)
    }

    @Test
    fun `동일한 값으로 생성된 슬러그는 동등하다`() {
        // given
        val slug1 = RoadmapSlug.of("Frontend Development")
        val slug2 = RoadmapSlug.of("frontend-development")

        // then
        assertEquals(slug1, slug2)
    }
}
