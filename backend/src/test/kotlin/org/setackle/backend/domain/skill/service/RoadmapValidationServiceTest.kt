package org.setackle.backend.domain.skill.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.setackle.backend.domain.skill.model.FlowEdge
import org.setackle.backend.domain.skill.model.FlowNode
import org.setackle.backend.domain.skill.model.NodeData
import org.setackle.backend.domain.skill.model.Position

/**
 * RoadmapValidationService 단위 테스트
 */
class RoadmapValidationServiceTest {
    private lateinit var service: RoadmapValidationService

    @BeforeEach
    fun setUp() {
        service = RoadmapValidationService()
    }

    @Test
    fun `유효한 로드맵 검증 성공`() {
        // given
        val nodes = listOf(
            createTestNode("node1"),
            createTestNode("node2"),
        )
        val edges = listOf(
            FlowEdge("edge1", "node1", "node2"),
        )

        // when
        val result = service.validate(nodes, edges)

        // then
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `중복된 노드 ID 검증 실패`() {
        // given
        val nodes = listOf(
            createTestNode("duplicate"),
            createTestNode("duplicate"), // 중복 ID
        )
        val edges = emptyList<FlowEdge>()

        // when
        val result = service.validate(nodes, edges)

        // then
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("중복된 노드 ID") })
    }

    @Test
    fun `중복된 엣지 ID 검증 실패`() {
        // given
        val nodes = listOf(
            createTestNode("node1"),
            createTestNode("node2"),
        )
        val edges = listOf(
            FlowEdge("duplicate", "node1", "node2"),
            FlowEdge("duplicate", "node1", "node2"), // 중복 ID
        )

        // when
        val result = service.validate(nodes, edges)

        // then
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("중복된 엣지 ID") })
    }

    @Test
    fun `존재하지 않는 source 노드 검증 실패`() {
        // given
        val nodes = listOf(createTestNode("node1"))
        val edges = listOf(
            FlowEdge("edge1", "non-existent", "node1"), // source 없음
        )

        // when
        val result = service.validate(nodes, edges)

        // then
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("source 노드") && it.contains("존재하지 않습니다") })
    }

    @Test
    fun `존재하지 않는 target 노드 검증 실패`() {
        // given
        val nodes = listOf(createTestNode("node1"))
        val edges = listOf(
            FlowEdge("edge1", "node1", "non-existent"), // target 없음
        )

        // when
        val result = service.validate(nodes, edges)

        // then
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("target 노드") && it.contains("존재하지 않습니다") })
    }

    @Test
    fun `순환 참조 검증 실패`() {
        // given
        val nodes = listOf(
            createTestNode("node1"),
            createTestNode("node2"),
            createTestNode("node3"),
        )
        // 순환: node1 -> node2 -> node3 -> node1
        val edges = listOf(
            FlowEdge("edge1", "node1", "node2"),
            FlowEdge("edge2", "node2", "node3"),
            FlowEdge("edge3", "node3", "node1"),
        )

        // when
        val result = service.validate(nodes, edges)

        // then
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("순환 참조가 감지") })
    }

    @Test
    fun `순환 참조 검사 - 순환 없음`() {
        // given
        val edges = listOf(
            FlowEdge("edge1", "node1", "node2"),
            FlowEdge("edge2", "node2", "node3"),
        )

        // when
        val result = service.detectCycles(edges)

        // then
        assertFalse(result.hasCycle)
        assertTrue(result.cyclePath.isEmpty())
    }

    @Test
    fun `순환 참조 검사 - 단순 순환`() {
        // given
        // node1 -> node2 -> node1
        val edges = listOf(
            FlowEdge("edge1", "node1", "node2"),
            FlowEdge("edge2", "node2", "node1"),
        )

        // when
        val result = service.detectCycles(edges)

        // then
        assertTrue(result.hasCycle)
        assertTrue(result.cyclePath.isNotEmpty())
    }

    @Test
    fun `순환 참조 검사 - 복잡한 순환`() {
        // given
        // node1 -> node2 -> node3 -> node4 -> node2 (순환)
        val edges = listOf(
            FlowEdge("edge1", "node1", "node2"),
            FlowEdge("edge2", "node2", "node3"),
            FlowEdge("edge3", "node3", "node4"),
            FlowEdge("edge4", "node4", "node2"), // 순환 지점
        )

        // when
        val result = service.detectCycles(edges)

        // then
        assertTrue(result.hasCycle)
        assertTrue(result.cyclePath.contains("node2"))
    }

    @Test
    fun `고아 노드 찾기 - 루트 노드만 존재`() {
        // given
        val nodes = listOf(
            createTestNode("root"),
            createTestNode("child1"),
            createTestNode("child2"),
        )
        val edges = listOf(
            FlowEdge("edge1", "root", "child1"),
            FlowEdge("edge2", "root", "child2"),
        )

        // when
        val orphans = service.findOrphanNodes(nodes, edges)

        // then
        assertEquals(1, orphans.size)
        assertTrue(orphans.contains("root")) // 루트는 진입 엣지가 없음
    }

    @Test
    fun `고아 노드 찾기 - 실제 고아 노드 존재`() {
        // given
        val nodes = listOf(
            createTestNode("root"),
            createTestNode("child"),
            createTestNode("orphan"), // 연결 안 됨
        )
        val edges = listOf(
            FlowEdge("edge1", "root", "child"),
        )

        // when
        val orphans = service.findOrphanNodes(nodes, edges)

        // then
        assertEquals(2, orphans.size)
        assertTrue(orphans.contains("root"))
        assertTrue(orphans.contains("orphan"))
    }

    @Test
    fun `루트 노드 찾기`() {
        // given
        val nodes = listOf(
            createTestNode("root"),
            createTestNode("child1"),
            createTestNode("child2"),
        )
        val edges = listOf(
            FlowEdge("edge1", "root", "child1"),
            FlowEdge("edge2", "root", "child2"),
        )

        // when
        val rootNodes = service.findRootNodes(nodes, edges)

        // then
        assertEquals(1, rootNodes.size)
        assertEquals("root", rootNodes[0].id)
    }

    @Test
    fun `여러 루트 노드 찾기`() {
        // given
        val nodes = listOf(
            createTestNode("root1"),
            createTestNode("root2"),
            createTestNode("child"),
        )
        val edges = listOf(
            FlowEdge("edge1", "root1", "child"),
            // root2는 연결 없음
        )

        // when
        val rootNodes = service.findRootNodes(nodes, edges)

        // then
        assertEquals(2, rootNodes.size)
        assertTrue(rootNodes.any { it.id == "root1" })
        assertTrue(rootNodes.any { it.id == "root2" })
    }

    @Test
    fun `리프 노드 찾기`() {
        // given
        val nodes = listOf(
            createTestNode("root"),
            createTestNode("child1"),
            createTestNode("child2"),
        )
        val edges = listOf(
            FlowEdge("edge1", "root", "child1"),
            FlowEdge("edge2", "root", "child2"),
        )

        // when
        val leafNodes = service.findLeafNodes(nodes, edges)

        // then
        assertEquals(2, leafNodes.size)
        assertTrue(leafNodes.any { it.id == "child1" })
        assertTrue(leafNodes.any { it.id == "child2" })
    }

    @Test
    fun `노드 깊이 계산 - 루트 노드`() {
        // given
        val edges = listOf(
            FlowEdge("edge1", "root", "child"),
        )

        // when
        val depth = service.calculateDepth("root", edges)

        // then
        assertEquals(0, depth) // 루트는 깊이 0
    }

    @Test
    fun `노드 깊이 계산 - 자식 노드`() {
        // given
        val edges = listOf(
            FlowEdge("edge1", "root", "child"),
        )

        // when
        val depth = service.calculateDepth("child", edges)

        // then
        assertEquals(1, depth)
    }

    @Test
    fun `노드 깊이 계산 - 깊은 계층`() {
        // given
        // root -> child -> grandchild
        val edges = listOf(
            FlowEdge("edge1", "root", "child"),
            FlowEdge("edge2", "child", "grandchild"),
        )

        // when
        val depth = service.calculateDepth("grandchild", edges)

        // then
        assertEquals(2, depth)
    }

    @Test
    fun `노드 깊이 계산 - 순환 참조 시 -1 반환`() {
        // given
        val edges = listOf(
            FlowEdge("edge1", "node1", "node2"),
            FlowEdge("edge2", "node2", "node1"), // 순환
        )

        // when
        val depth = service.calculateDepth("node1", edges)

        // then
        assertEquals(-1, depth) // 순환 참조 감지
    }

    private fun createTestNode(id: String): FlowNode {
        return FlowNode(
            id = id,
            type = "default",
            position = Position(100.0, 100.0),
            data = NodeData(
                label = "Test Node $id",
                description = null,
            ),
        )
    }
}
