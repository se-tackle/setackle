package org.setackle.backend.application.user.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.setackle.backend.application.user.inbound.BulkUpdateProgressCommand
import org.setackle.backend.application.user.inbound.NodeStatusUpdate
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
 * BulkUpdateProgressService 단위 테스트
 */
class BulkUpdateProgressServiceTest {

    private lateinit var roadmapPort: RoadmapPort
    private lateinit var progressPort: RoadmapProgressPort
    private lateinit var eventPort: ProgressEventPort
    private lateinit var bulkUpdateProgressService: BulkUpdateProgressService

    @BeforeEach
    fun setUp() {
        roadmapPort = mockk()
        progressPort = mockk()
        eventPort = mockk(relaxed = true)
        bulkUpdateProgressService = BulkUpdateProgressService(
            roadmapPort,
            progressPort,
            eventPort,
        )
    }

    @Test
    fun `bulkUpdate - 여러 노드 일괄 업데이트 성공`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val updates = listOf(
            NodeStatusUpdate("node-1", "DONE"),
            NodeStatusUpdate("node-2", "LEARNING"),
            NodeStatusUpdate("node-3", "SKIPPED"),
        )
        val command = BulkUpdateProgressCommand(
            userId = userId,
            roadmapSlug = roadmapSlug,
            updates = updates,
        )

        val roadmap = createTestRoadmap(1L, roadmapSlug, listOf("node-1", "node-2", "node-3"))
        val progress = UserRoadmapProgress.create(UserId.of(userId), 1L)

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), 1L) } returns progress
        every { progressPort.save(any()) } returns progress

        // when
        val result = bulkUpdateProgressService.bulkUpdate(command)

        // then
        assertEquals(3, result.totalUpdated)
        assertEquals(3, result.successCount)
        assertEquals(0, result.failedCount)
        assertTrue(result.done.contains("node-1"))
        assertTrue(result.learning.contains("node-2"))
        assertTrue(result.skipped.contains("node-3"))
        assertNull(result.errors)
        verify { progressPort.save(any()) }
    }

    @Test
    fun `bulkUpdate - 부분 실패 시 성공한 것만 반영`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val roadmapId = 1L
        val updates = listOf(
            NodeStatusUpdate("node-1", "DONE"),
            NodeStatusUpdate("non-existent", "DONE"), // 존재하지 않는 노드
            NodeStatusUpdate("node-3", "LEARNING"),
        )
        val command = BulkUpdateProgressCommand(
            userId = userId,
            roadmapSlug = roadmapSlug,
            updates = updates,
        )

        val roadmap = createTestRoadmap(roadmapId, roadmapSlug, listOf("node-1", "node-3"))
        val progress = UserRoadmapProgress.create(UserId.of(userId), roadmapId)

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns progress
        every { progressPort.save(any()) } returns progress

        // when
        val result = bulkUpdateProgressService.bulkUpdate(command)

        // then
        assertEquals(3, result.totalUpdated)
        assertEquals(2, result.successCount)
        assertEquals(1, result.failedCount)
        assertTrue(result.done.contains("node-1"))
        assertTrue(result.learning.contains("node-3"))
        assertNotNull(result.errors)
        assertEquals(1, result.errors!!.size)
        assertEquals("non-existent", result.errors!![0].nodeId)
    }

    @Test
    fun `bulkUpdate - 유효하지 않은 상태 값 포함 시 부분 실패`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val roadmapId = 1L
        val updates = listOf(
            NodeStatusUpdate("node-1", "DONE"),
            NodeStatusUpdate("node-2", "INVALID_STATUS"), // 유효하지 않은 상태
            NodeStatusUpdate("node-3", "LEARNING"),
        )
        val command = BulkUpdateProgressCommand(
            userId = userId,
            roadmapSlug = roadmapSlug,
            updates = updates,
        )

        val roadmap = createTestRoadmap(roadmapId, roadmapSlug, listOf("node-1", "node-2", "node-3"))
        val progress = UserRoadmapProgress.create(UserId.of(userId), roadmapId)

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns progress
        every { progressPort.save(any()) } returns progress

        // when
        val result = bulkUpdateProgressService.bulkUpdate(command)

        // then
        assertEquals(3, result.totalUpdated)
        assertEquals(2, result.successCount)
        assertEquals(1, result.failedCount)
        assertNotNull(result.errors)
        assertEquals("node-2", result.errors!![0].nodeId)
    }

    @Test
    fun `bulkUpdate - 빈 업데이트 목록 시 예외 발생`() {
        // given
        val command = BulkUpdateProgressCommand(
            userId = 1L,
            roadmapSlug = "backend",
            updates = emptyList(),
        )

        // when & then
        val exception = assertThrows<BusinessException> {
            bulkUpdateProgressService.bulkUpdate(command)
        }

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.errorCode)
    }

    @Test
    fun `bulkUpdate - 최대 크기 초과 시 예외 발생`() {
        // given
        val updates = (1..101).map { NodeStatusUpdate("node-$it", "DONE") }
        val command = BulkUpdateProgressCommand(
            userId = 1L,
            roadmapSlug = "backend",
            updates = updates,
        )

        // when & then
        val exception = assertThrows<BusinessException> {
            bulkUpdateProgressService.bulkUpdate(command)
        }

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.errorCode)
        assertNotNull(exception.details["message"])
        assertTrue((exception.details["message"] as String).contains("최대"))
    }

    @Test
    fun `bulkUpdate - 존재하지 않는 로드맵 시 예외 발생`() {
        // given
        val command = BulkUpdateProgressCommand(
            userId = 1L,
            roadmapSlug = "non-existent",
            updates = listOf(NodeStatusUpdate("node-1", "DONE")),
        )

        every { roadmapPort.findBySlug(any()) } returns null

        // when & then
        val exception = assertThrows<BusinessException> {
            bulkUpdateProgressService.bulkUpdate(command)
        }

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `bulkUpdate - 진행 상황이 없을 때 자동 생성`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val roadmapId = 1L
        val command = BulkUpdateProgressCommand(
            userId = userId,
            roadmapSlug = roadmapSlug,
            updates = listOf(NodeStatusUpdate("node-1", "DONE")),
        )

        val roadmap = createTestRoadmap(roadmapId, roadmapSlug, listOf("node-1"))

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns null
        every { progressPort.save(any()) } returnsArgument 0

        // when
        val result = bulkUpdateProgressService.bulkUpdate(command)

        // then
        assertEquals(1, result.successCount)
        assertTrue(result.done.contains("node-1"))
        verify { progressPort.save(any()) }
    }

    @Test
    fun `bulkUpdate - 동일 노드 여러 번 업데이트 시 마지막 상태 반영`() {
        // given
        val userId = 1L
        val roadmapSlug = "backend"
        val roadmapId = 1L
        val updates = listOf(
            NodeStatusUpdate("node-1", "LEARNING"),
            NodeStatusUpdate("node-1", "DONE"), // 같은 노드를 DONE으로 재업데이트
        )
        val command = BulkUpdateProgressCommand(
            userId = userId,
            roadmapSlug = roadmapSlug,
            updates = updates,
        )

        val roadmap = createTestRoadmap(roadmapId, roadmapSlug, listOf("node-1"))
        val progress = UserRoadmapProgress.create(UserId.of(userId), roadmapId)

        every { roadmapPort.findBySlug(RoadmapSlug.of(roadmapSlug)) } returns roadmap
        every { progressPort.findByUserAndRoadmap(UserId.of(userId), roadmapId) } returns progress
        every { progressPort.save(any()) } returns progress

        // when
        val result = bulkUpdateProgressService.bulkUpdate(command)

        // then
        assertEquals(2, result.successCount)
        assertTrue(result.done.contains("node-1"))
        assertTrue(result.learning.isEmpty()) // LEARNING 상태는 덮어씌워짐
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
