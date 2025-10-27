package org.setackle.backend.application.skill.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.setackle.backend.application.skill.outbound.RoadmapCachePort
import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.domain.skill.model.Dimensions
import org.setackle.backend.domain.skill.model.Roadmap
import org.setackle.backend.domain.skill.model.RoadmapType
import org.setackle.backend.domain.skill.vo.RoadmapSlug
import java.time.LocalDateTime

/**
 * GetRoadmapService 단위 테스트
 */
class GetRoadmapServiceTest {

    private lateinit var roadmapPort: RoadmapPort
    private lateinit var roadmapCachePort: RoadmapCachePort
    private lateinit var getRoadmapService: GetRoadmapService

    @BeforeEach
    fun setUp() {
        roadmapPort = mockk()
        roadmapCachePort = mockk(relaxed = true) // 캐시는 relaxed로 설정
        getRoadmapService = GetRoadmapService(roadmapPort, roadmapCachePort)
    }

    @Test
    fun `getAllRoadmaps - 활성화된 로드맵 목록 조회 성공`() {
        // given
        val roadmap1 = createTestRoadmap(1L, "backend", "Backend", true, 1)
        val roadmap2 = createTestRoadmap(2L, "frontend", "Frontend", true, 2)
        val roadmap3 = createTestRoadmap(3L, "devops", "DevOps", false, 3) // 비활성화

        every { roadmapCachePort.getRoadmapList(any()) } returns null
        every { roadmapPort.findAll() } returns listOf(roadmap1, roadmap2, roadmap3)

        // when
        val result = getRoadmapService.getAllRoadmaps()

        // then
        assertEquals(2, result.size) // 활성화된 것만
        assertEquals("Backend", result[0].name)
        assertEquals("Frontend", result[1].name)
        verify { roadmapPort.findAll() }
    }

    @Test
    fun `getAllRoadmaps - 캐시에서 조회 성공`() {
        // given
        val cachedRoadmaps = listOf(
            createTestRoadmap(1L, "backend", "Backend", true, 1),
            createTestRoadmap(2L, "frontend", "Frontend", true, 2),
        )

        every { roadmapCachePort.getRoadmapList(any()) } returns cachedRoadmaps

        // when
        val result = getRoadmapService.getAllRoadmaps()

        // then
        assertEquals(2, result.size)
        verify(exactly = 0) { roadmapPort.findAll() } // DB 조회 안 함
    }

    @Test
    fun `getRoadmapBySlug - 슬러그로 로드맵 조회 성공`() {
        // given
        val slug = "backend-development"
        val roadmap = createTestRoadmap(1L, slug, "Backend Development", true, 1)

        every { roadmapCachePort.getRoadmap(any()) } returns null
        every { roadmapPort.findBySlug(RoadmapSlug.of(slug)) } returns roadmap

        // when
        val result = getRoadmapService.getRoadmapBySlug(slug)

        // then
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("Backend Development", result.name)
        assertEquals(slug, result.slug)
        verify { roadmapPort.findBySlug(RoadmapSlug.of(slug)) }
    }

    @Test
    fun `getRoadmapBySlug - 존재하지 않는 슬러그 조회 시 예외 발생`() {
        // given
        val slug = "non-existent"

        every { roadmapCachePort.getRoadmap(any()) } returns null
        every { roadmapPort.findBySlug(RoadmapSlug.of(slug)) } returns null

        // when & then
        val exception = assertThrows<BusinessException> {
            getRoadmapService.getRoadmapBySlug(slug)
        }

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `getRoadmapBySlug - 캐시에서 조회 성공`() {
        // given
        val slug = "backend-development"
        val cachedRoadmap = createTestRoadmap(1L, slug, "Backend Development", true, 1)

        every { roadmapCachePort.getRoadmap(any()) } returns cachedRoadmap

        // when
        val result = getRoadmapService.getRoadmapBySlug(slug)

        // then
        assertNotNull(result)
        assertEquals(1L, result.id)
        verify(exactly = 0) { roadmapPort.findBySlug(any()) } // DB 조회 안 함
    }

    @Test
    fun `getRoadmapById - ID로 로드맵 조회 성공`() {
        // given
        val id = 1L
        val roadmap = createTestRoadmap(id, "backend", "Backend", true, 1)

        every { roadmapCachePort.getRoadmap(any()) } returns null
        every { roadmapPort.findById(id) } returns roadmap

        // when
        val result = getRoadmapService.getRoadmapById(id)

        // then
        assertNotNull(result)
        assertEquals(id, result.id)
        assertEquals("Backend", result.name)
        verify { roadmapPort.findById(id) }
    }

    @Test
    fun `getRoadmapById - 존재하지 않는 ID 조회 시 예외 발생`() {
        // given
        val id = 999L

        every { roadmapCachePort.getRoadmap(any()) } returns null
        every { roadmapPort.findById(id) } returns null

        // when & then
        val exception = assertThrows<BusinessException> {
            getRoadmapService.getRoadmapById(id)
        }

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
    }

    private fun createTestRoadmap(
        id: Long,
        slug: String,
        name: String,
        isActive: Boolean,
        displayOrder: Int,
    ): Roadmap {
        return Roadmap.reconstruct(
            id = id,
            name = name,
            description = "$name Description",
            slug = slug,
            roadmapType = RoadmapType.SKILL,
            nodes = emptyList(),
            edges = emptyList(),
            dimensions = Dimensions(width = 1200, height = 800),
            displayOrder = displayOrder,
            isActive = isActive,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}
