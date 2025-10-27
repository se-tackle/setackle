package org.setackle.backend.application.user.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.setackle.backend.application.user.outbound.RoadmapProgressPort
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.domain.skill.model.Dimensions
import org.setackle.backend.domain.skill.model.FlowNode
import org.setackle.backend.domain.skill.model.NodeData
import org.setackle.backend.domain.skill.model.Position
import org.setackle.backend.domain.skill.model.Roadmap
import org.setackle.backend.domain.skill.model.RoadmapType
import org.setackle.backend.domain.skill.vo.RoadmapSlug
import org.setackle.backend.domain.user.model.ProgressStatus
import org.setackle.backend.domain.user.model.UserRoadmapProgress
import org.setackle.backend.domain.user.vo.UserId
import java.time.LocalDateTime

/**
 * GetUserRoadmapProgressService 단위 테스트
 */
class GetUserRoadmapProgressServiceTest {

    private lateinit var roadmapPort: RoadmapPort
    private lateinit var progressPort: RoadmapProgressPort
    private lateinit var getUserRoadmapProgressService: GetUserRoadmapProgressService

    @BeforeEach
    fun setUp() {
        roadmapPort = mockk()
        progressPort = mockk()
        getUserRoadmapProgressService = GetUserRoadmapProgressService(
            roadmapPort,
            progressPort,
        )
    }

    @Test
    fun `getProgress - 진행 상황 조회 성공`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val roadmap = createTestRoadmap(1L, roadmapSlug, listOf("node-1", "node-2", "node-3"))
        val progress = UserRoadmapProgress.create(UserId.of(userId), 1L)
        progress.updateNodeProgress("node-1", ProgressStatus.DONE)
        progress.updateNodeProgress("node-2", ProgressStatus.LEARNING)

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), 1L) } returns progress

        // when
        val result = getUserRoadmapProgressService.getProgress(userId, roadmapSlug)

        // then
        assertEquals(userId, result.userId)
        assertEquals(1L, result.roadmapId)
        assertEquals(3, result.totalNodes)
        assertEquals(1, result.doneCount)
        assertEquals(1, result.learningCount)
        assertTrue(result.done.contains("node-1"))
        assertTrue(result.learning.contains("node-2"))
        verify { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) }
    }

    @Test
    fun `getProgress - 진행 상황이 없을 때 빈 데이터 반환`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val roadmap = createTestRoadmap(1L, roadmapSlug, listOf("node-1", "node-2"))

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), 1L) } returns null

        // when
        val result = getUserRoadmapProgressService.getProgress(userId, roadmapSlug)

        // then
        assertEquals(userId, result.userId)
        assertEquals(1L, result.roadmapId)
        assertEquals(2, result.totalNodes)
        assertEquals(0, result.doneCount)
        assertEquals(0, result.learningCount)
        assertEquals(0.0, result.progressPercent)
        assertTrue(result.done.isEmpty())
        assertTrue(result.learning.isEmpty())
        assertFalse(result.isFavorite)
    }

    @Test
    fun `getProgress - 존재하지 않는 로드맵 시 예외 발생`() {
        // given
        val userId = 1L
        val roadmapSlug = "non-existent"

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns null

        // when & then
        val exception = assertThrows<BusinessException> {
            getUserRoadmapProgressService.getProgress(userId, roadmapSlug)
        }

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `getProgress - 진행률 계산 정확성 검증`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val roadmapId = 1L
        val roadmap = createTestRoadmap(roadmapId, roadmapSlug, listOf("node-1", "node-2", "node-3", "node-4"))
        val progress = UserRoadmapProgress.create(UserId.of(userId), roadmapId)
        progress.updateNodeProgress("node-1", ProgressStatus.DONE)
        progress.updateNodeProgress("node-2", ProgressStatus.DONE)

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns progress

        // when
        val result = getUserRoadmapProgressService.getProgress(userId, roadmapSlug)

        // then
        assertEquals(4, result.totalNodes)
        assertEquals(2, result.doneCount)
        assertEquals(50.0, result.progressPercent) // 2/4 = 50%
    }

    @Test
    fun `getUserRoadmaps - 사용자의 모든 로드맵 조회 성공`() {
        // given
        val userId = 1L
        val progress1 = UserRoadmapProgress.create(UserId.of(userId), 1L)
        progress1.updateNodeProgress("node-1", ProgressStatus.DONE)
        val progress2 = UserRoadmapProgress.create(UserId.of(userId), 2L)
        progress2.updateNodeProgress("node-1", ProgressStatus.LEARNING)

        val roadmap1 = createTestRoadmap(1L, "backend", listOf("node-1", "node-2"))
        val roadmap2 = createTestRoadmap(2L, "frontend", listOf("node-1", "node-2", "node-3"))

        every { progressPort.findByUser(UserId.of(userId)) } returns listOf(progress1, progress2)
        every { roadmapPort.findById(1L) } returns roadmap1
        every { roadmapPort.findById(2L) } returns roadmap2

        // when
        val result = getUserRoadmapProgressService.getUserRoadmaps(userId)

        // then
        assertEquals(2, result.size)
        assertEquals("backend", result[0].roadmapSlug)
        assertEquals("frontend", result[1].roadmapSlug)
        assertEquals(50.0, result[0].progressPercent) // 1/2 done
        assertEquals(0.0, result[1].progressPercent) // 0/3 done (LEARNING은 미완료)
    }

    @Test
    fun `getUserRoadmaps - 로드맵이 삭제된 경우 제외`() {
        // given
        val userId = 1L
        val progress1 = UserRoadmapProgress.create(UserId.of(userId), 1L)
        val progress2 = UserRoadmapProgress.create(UserId.of(userId), 999L) // 삭제된 로드맵

        val roadmap1 = createTestRoadmap(1L, "backend", listOf("node-1"))

        every { progressPort.findByUser(UserId.of(userId)) } returns listOf(progress1, progress2)
        every { roadmapPort.findById(1L) } returns roadmap1
        every { roadmapPort.findById(999L) } returns null // 삭제됨

        // when
        val result = getUserRoadmapProgressService.getUserRoadmaps(userId)

        // then
        assertEquals(1, result.size)
        assertEquals("backend", result[0].roadmapSlug)
    }

    @Test
    fun `getFavoriteRoadmaps - 즐겨찾기 로드맵만 조회`() {
        // given
        val userId = 1L
        val progress1 = UserRoadmapProgress.create(UserId.of(userId), 1L)
        progress1.isFavorite = true
        val progress2 = UserRoadmapProgress.create(UserId.of(userId), 2L)
        progress2.isFavorite = true

        val roadmap1 = createTestRoadmap(1L, "backend", listOf("node-1"))
        val roadmap2 = createTestRoadmap(2L, "frontend", listOf("node-1"))

        every { progressPort.findFavoritesByUser(UserId.of(userId)) } returns listOf(progress1, progress2)
        every { roadmapPort.findById(1L) } returns roadmap1
        every { roadmapPort.findById(2L) } returns roadmap2

        // when
        val result = getUserRoadmapProgressService.getFavoriteRoadmaps(userId)

        // then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun `getFavoriteRoadmaps - 즐겨찾기가 없을 때 빈 목록 반환`() {
        // given
        val userId = 1L

        every { progressPort.findFavoritesByUser(UserId.of(userId)) } returns emptyList()

        // when
        val result = getUserRoadmapProgressService.getFavoriteRoadmaps(userId)

        // then
        assertTrue(result.isEmpty())
    }

    private fun createTestRoadmap(
        id: Long,
        slug: String,
        nodeIds: List<String>,
    ): Roadmap {
        val nodes = nodeIds.map { nodeId ->
            FlowNode(
                id = nodeId,
                type = "default",
                position = Position(x = 0.0, y = 0.0),
                data = NodeData(label = nodeId),
            )
        }

        return Roadmap.reconstruct(
            id = id,
            name = "Test Roadmap",
            description = "Test Description",
            slug = slug,
            roadmapType = RoadmapType.SKILL,
            nodes = nodes,
            edges = emptyList(),
            dimensions = Dimensions(width = 1200, height = 800),
            displayOrder = 1,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}
