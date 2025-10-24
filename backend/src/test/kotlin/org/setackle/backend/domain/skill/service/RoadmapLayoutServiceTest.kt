package org.setackle.backend.domain.skill.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.setackle.backend.domain.skill.model.FlowNode
import org.setackle.backend.domain.skill.model.NodeData
import org.setackle.backend.domain.skill.model.Position

/**
 * RoadmapLayoutService 단위 테스트
 */
class RoadmapLayoutServiceTest {
    private lateinit var service: RoadmapLayoutService

    @BeforeEach
    fun setUp() {
        service = RoadmapLayoutService()
    }

    @Test
    fun `빈 노드 목록에 대한 레이아웃 계산`() {
        // given
        val nodes = emptyList<FlowNode>()

        // when
        val positions = service.calculateFlowNodeLayout(nodes, emptyMap())

        // then
        assertTrue(positions.isEmpty())
    }

    @Test
    fun `단일 루트 노드 레이아웃 계산`() {
        // given
        val node = createTestNode("root")
        val parentMap = emptyMap<String, String>()

        // when
        val positions = service.calculateFlowNodeLayout(listOf(node), parentMap)

        // then
        assertEquals(1, positions.size)
        val position = positions["root"]!!
        assertEquals(100.0, position.x) // INITIAL_X_OFFSET
        assertEquals(100.0, position.y) // INITIAL_Y_OFFSET
    }

    @Test
    fun `2단계 계층 구조 레이아웃 계산`() {
        // given
        val root = createTestNode("root")
        val child1 = createTestNode("child1")
        val child2 = createTestNode("child2")

        val parentMap = mapOf(
            "child1" to "root",
            "child2" to "root",
        )

        // when
        val positions = service.calculateFlowNodeLayout(
            listOf(root, child1, child2),
            parentMap,
        )

        // then
        assertEquals(3, positions.size)

        // 루트는 레벨 0
        val rootPos = positions["root"]!!
        assertEquals(100.0, rootPos.x) // INITIAL_X_OFFSET
        assertEquals(100.0, rootPos.y) // INITIAL_Y_OFFSET

        // 자식들은 레벨 1
        val child1Pos = positions["child1"]!!
        val child2Pos = positions["child2"]!!
        assertEquals(400.0, child1Pos.x) // INITIAL_X_OFFSET + LEVEL_X_GAP
        assertEquals(400.0, child2Pos.x)

        // 자식 노드들은 세로로 배치
        assertTrue(child1Pos.y < child2Pos.y)
        assertEquals(150.0, child2Pos.y - child1Pos.y) // NODE_Y_GAP
    }

    @Test
    fun `3단계 계층 구조 레이아웃 계산`() {
        // given
        val root = createTestNode("root")
        val child = createTestNode("child")
        val grandchild = createTestNode("grandchild")

        val parentMap = mapOf(
            "child" to "root",
            "grandchild" to "child",
        )

        // when
        val positions = service.calculateFlowNodeLayout(
            listOf(root, child, grandchild),
            parentMap,
        )

        // then
        assertEquals(3, positions.size)

        val rootPos = positions["root"]!!
        val childPos = positions["child"]!!
        val grandchildPos = positions["grandchild"]!!

        // X 좌표는 레벨별로 증가
        assertEquals(100.0, rootPos.x) // 레벨 0
        assertEquals(400.0, childPos.x) // 레벨 1
        assertEquals(700.0, grandchildPos.x) // 레벨 2

        // 각 레벨은 LEVEL_X_GAP(300)만큼 차이
        assertEquals(300.0, childPos.x - rootPos.x)
        assertEquals(300.0, grandchildPos.x - childPos.x)
    }

    @Test
    fun `복잡한 계층 구조 레이아웃 계산`() {
        // given
        // root
        //  ├─ child1
        //  │   ├─ grandchild1
        //  │   └─ grandchild2
        //  └─ child2
        val nodes = listOf(
            createTestNode("root"),
            createTestNode("child1"),
            createTestNode("child2"),
            createTestNode("grandchild1"),
            createTestNode("grandchild2"),
        )

        val parentMap = mapOf(
            "child1" to "root",
            "child2" to "root",
            "grandchild1" to "child1",
            "grandchild2" to "child1",
        )

        // when
        val positions = service.calculateFlowNodeLayout(nodes, parentMap)

        // then
        assertEquals(5, positions.size)

        // 레벨별 X 좌표 확인
        assertEquals(100.0, positions["root"]!!.x) // 레벨 0
        assertEquals(400.0, positions["child1"]!!.x) // 레벨 1
        assertEquals(400.0, positions["child2"]!!.x) // 레벨 1
        assertEquals(700.0, positions["grandchild1"]!!.x) // 레벨 2
        assertEquals(700.0, positions["grandchild2"]!!.x) // 레벨 2
    }

    @Test
    fun `순환 참조가 있는 경우 무한 재귀 방지`() {
        // given
        val node1 = createTestNode("node1")
        val node2 = createTestNode("node2")

        // 순환 참조: node1 -> node2 -> node1
        val parentMap = mapOf(
            "node1" to "node2",
            "node2" to "node1",
        )

        // when
        val positions = service.calculateFlowNodeLayout(
            listOf(node1, node2),
            parentMap,
        )

        // then
        assertEquals(2, positions.size)
        // 순환 참조 시에도 위치가 계산됨 (무한 재귀 발생하지 않음)
        assertTrue(positions.containsKey("node1"))
        assertTrue(positions.containsKey("node2"))
        // 실제 위치는 처리 순서에 따라 다를 수 있으므로 존재 여부만 확인
    }

    @Test
    fun `기존 노드에 새 위치 적용`() {
        // given
        val nodes = listOf(
            createTestNode("node1"),
            createTestNode("node2"),
        )

        val newPositions = mapOf(
            "node1" to Position(200.0, 300.0),
            "node2" to Position(400.0, 500.0),
        )

        // when
        val updatedNodes = service.applyLayout(nodes, newPositions)

        // then
        assertEquals(2, updatedNodes.size)
        assertEquals(Position(200.0, 300.0), updatedNodes[0].position)
        assertEquals(Position(400.0, 500.0), updatedNodes[1].position)
    }

    @Test
    fun `위치가 없는 노드는 기존 위치 유지`() {
        // given
        val originalPosition = Position(100.0, 100.0)
        val node = createTestNode("node1", originalPosition)

        val newPositions = emptyMap<String, Position>()

        // when
        val updatedNodes = service.applyLayout(listOf(node), newPositions)

        // then
        assertEquals(1, updatedNodes.size)
        assertEquals(originalPosition, updatedNodes[0].position)
    }

    @Test
    fun `노드 목록의 경계 박스 계산`() {
        // given
        val nodes = listOf(
            createTestNode("node1", Position(100.0, 100.0)),
            createTestNode("node2", Position(500.0, 300.0)),
            createTestNode("node3", Position(800.0, 600.0)),
        )

        // when
        val (width, height) = service.calculateBounds(nodes, padding = 100.0)

        // then
        // maxX = 800, padding = 100 → width = 800 + 200 = 1000
        // maxY = 600, padding = 100 → height = 600 + 200 = 800
        assertTrue(width >= 1000)
        assertTrue(height >= 800)
    }

    @Test
    fun `빈 노드 목록의 경계 박스는 기본 크기 반환`() {
        // given
        val nodes = emptyList<FlowNode>()

        // when
        val (width, height) = service.calculateBounds(nodes)

        // then
        assertEquals(1200, width)
        assertEquals(800, height)
    }

    private fun createTestNode(
        id: String,
        position: Position = Position(0.0, 0.0),
    ): FlowNode {
        return FlowNode(
            id = id,
            type = "default",
            position = position,
            data = NodeData(
                label = "Test Node $id",
                description = null,
            ),
        )
    }
}
