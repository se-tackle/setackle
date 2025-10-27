package org.setackle.backend.application.user.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.setackle.backend.application.user.inbound.UpdateProgressCommand
import org.setackle.backend.application.user.outbound.ProgressEventPort
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
import org.setackle.backend.domain.user.model.UserRoadmapProgress
import org.setackle.backend.domain.user.vo.UserId
import java.time.LocalDateTime

/**
 * UpdateRoadmapProgressService 단위 테스트
 */
class UpdateRoadmapProgressServiceTest {

    private lateinit var roadmapPort: RoadmapPort
    private lateinit var progressPort: RoadmapProgressPort
    private lateinit var eventPort: ProgressEventPort
    private lateinit var updateRoadmapProgressService: UpdateRoadmapProgressService

    @BeforeEach
    fun setUp() {
        roadmapPort = mockk()
        progressPort = mockk()
        eventPort = mockk(relaxed = true)
        updateRoadmapProgressService = UpdateRoadmapProgressService(
            roadmapPort,
            progressPort,
            eventPort,
        )
    }

    @Test
    fun `updateProgress - 진행 상태 업데이트 성공`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val nodeId = "node-1"
        val command = UpdateProgressCommand(
            userId = userId,
            roadmapSlug = roadmapSlug,
            nodeId = nodeId,
            status = "DONE",
        )

        val roadmap = createTestRoadmap(1L, roadmapSlug, listOf(nodeId))
        val progress = UserRoadmapProgress.create(UserId.of(userId), 1L)

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), 1L) } returns progress
        every { progressPort.save(any()) } returns progress

        // when
        val result = updateRoadmapProgressService.updateProgress(command)

        // then
        assertEquals(userId, result.userId)
        assertEquals(1L, result.roadmapId)
        assertEquals(nodeId, result.nodeId)
        assertEquals("DONE", result.currentStatus)
        assertTrue(result.done.contains(nodeId))
        verify { progressPort.save(any()) }
    }

    @Test
    fun `updateProgress - 존재하지 않는 로드맵 시 예외 발생`() {
        // given
        val command = UpdateProgressCommand(
            userId = 1L,
            roadmapSlug = "non-existent",
            nodeId = "node-1",
            status = "DONE",
        )

        every { roadmapPort.findBySlug(any()) } returns null

        // when & then
        val exception = assertThrows<BusinessException> {
            updateRoadmapProgressService.updateProgress(command)
        }

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `updateProgress - 존재하지 않는 노드 시 예외 발생`() {
        // given
        val command = UpdateProgressCommand(
            userId = 1L,
            roadmapSlug = "backend",
            nodeId = "non-existent-node",
            status = "DONE",
        )

        val roadmap = createTestRoadmap(1L, "backend", listOf("node-1"))

        every { roadmapPort.findBySlug(any()) } returns roadmap

        // when & then
        val exception = assertThrows<BusinessException> {
            updateRoadmapProgressService.updateProgress(command)
        }

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `updateProgress - 유효하지 않은 상태 시 예외 발생`() {
        // given
        val userId = 1L
        val roadmapId = 1L
        val command = UpdateProgressCommand(
            userId = userId,
            roadmapSlug = "backend",
            nodeId = "node-1",
            status = "INVALID_STATUS",
        )

        val roadmap = createTestRoadmap(roadmapId, "backend", listOf("node-1"))
        val progress = UserRoadmapProgress.create(UserId.of(userId), roadmapId)

        every { roadmapPort.findBySlug(RoadmapSlug.of("backend")) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns progress

        // when & then
        val exception = assertThrows<BusinessException> {
            updateRoadmapProgressService.updateProgress(command)
        }

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.errorCode)
    }

    @Test
    fun `updateProgress - 진행 상황이 없을 때 자동 생성`() {
        // given
        val userId = 1L
        val roadmapId = 1L
        val command = UpdateProgressCommand(
            userId = userId,
            roadmapSlug = "backend",
            nodeId = "node-1",
            status = "DONE",
        )

        val roadmap = createTestRoadmap(roadmapId, "backend", listOf("node-1"))

        every { roadmapPort.findBySlug(RoadmapSlug.of("backend")) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns null
        every { progressPort.save(any()) } returnsArgument 0

        // when
        val result = updateRoadmapProgressService.updateProgress(command)

        // then
        assertEquals(userId, result.userId)
        assertTrue(result.done.contains("node-1"))
        verify { progressPort.save(any()) }
    }

    @Test
    fun `resetProgress - 진행 상태 초기화 성공`() {
        // given
        val userId = 1L
        val roadmapId = 1L
        val roadmapSlug = "backend"

        val roadmap = createTestRoadmap(roadmapId, roadmapSlug, listOf("node-1"))
        val progress = UserRoadmapProgress.create(UserId.of(userId), roadmapId)
        progress.updateNodeProgress("node-1", org.setackle.backend.domain.user.model.ProgressStatus.DONE)

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns progress
        every { progressPort.save(any()) } returns progress

        // when
        val result = updateRoadmapProgressService.resetProgress(userId, roadmapSlug)

        // then
        assertEquals(userId, result.userId)
        assertEquals(roadmapId, result.roadmapId)
        verify { progressPort.save(any()) }
    }

    @Test
    fun `resetProgress - 진행 상황이 없을 때 예외 발생`() {
        // given
        val userId = 1L
        val roadmapId = 1L
        val roadmapSlug = "backend"
        val roadmap = createTestRoadmap(roadmapId, roadmapSlug, listOf())

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns null

        // when & then
        val exception = assertThrows<BusinessException> {
            updateRoadmapProgressService.resetProgress(userId, roadmapSlug)
        }

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `toggleFavorite - 즐겨찾기 토글 성공`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val roadmapId = 1L

        val roadmap = createTestRoadmap(roadmapId, roadmapSlug, listOf())
        val progress = UserRoadmapProgress.create(UserId.of(userId), roadmapId)
        val initialFavoriteState = progress.isFavorite

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns progress
        every { progressPort.save(any()) } answers { firstArg() }

        // when
        val result = updateRoadmapProgressService.toggleFavorite(userId, roadmapSlug)

        // then
        assertEquals(userId, result.userId)
        assertEquals(1L, result.roadmapId)
        assertEquals(!initialFavoriteState, result.isFavorite)
        verify { progressPort.save(any()) }
    }

    @Test
    fun `toggleFavorite - 진행 상황이 없을 때 자동 생성`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val roadmapId = 1L
        val roadmap = createTestRoadmap(roadmapId, roadmapSlug, listOf())

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns null
        every { progressPort.save(any()) } returnsArgument 0

        // when
        val result = updateRoadmapProgressService.toggleFavorite(userId, roadmapSlug)

        // then
        assertTrue(result.isFavorite) // 새로 생성된 후 토글되어 true
        verify { progressPort.save(any()) }
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
