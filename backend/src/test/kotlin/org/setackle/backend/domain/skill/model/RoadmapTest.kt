package org.setackle.backend.domain.skill.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Roadmap 애그리게이트 루트 단위 테스트
 */
class RoadmapTest {
    @Test
    fun `create 팩토리 메서드로 새 로드맵 생성`() {
        // given
        val name = "Backend Development"
        val description = "백엔드 개발 로드맵"
        val slug = "backend-development"

        // when
        val roadmap = Roadmap.create(
            name = name,
            description = description,
            slug = slug,
        )

        // then
        assertNull(roadmap.id) // 신규 생성 시 ID는 null
        assertEquals(name, roadmap.name)
        assertEquals(description, roadmap.description)
        assertEquals(slug, roadmap.slug)
        assertTrue(roadmap.isActive)
        assertEquals(0, roadmap.nodes.size)
        assertEquals(0, roadmap.edges.size)
    }

    @Test
    fun `노드 추가 성공`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        val node = createTestNode("node-1")

        // when
        roadmap.addNode(node)

        // then
        assertEquals(1, roadmap.nodes.size)
        assertEquals(node, roadmap.nodes[0])
    }

    @Test
    fun `중복된 노드 ID 추가 시 예외 발생`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        val node1 = createTestNode("node-1")
        val node2 = createTestNode("node-1") // 같은 ID
        roadmap.addNode(node1)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            roadmap.addNode(node2)
        }
        assertTrue(exception.message!!.contains("이미 존재하는 노드 ID"))
    }

    @Test
    fun `엣지 추가 성공`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        val node1 = createTestNode("node-1")
        val node2 = createTestNode("node-2")
        roadmap.addNode(node1)
        roadmap.addNode(node2)

        val edge = FlowEdge(
            id = "edge-1-2",
            source = "node-1",
            target = "node-2",
        )

        // when
        roadmap.addEdge(edge)

        // then
        assertEquals(1, roadmap.edges.size)
        assertEquals(edge, roadmap.edges[0])
    }

    @Test
    fun `존재하지 않는 source 노드로 엣지 추가 시 예외 발생`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        val node = createTestNode("node-1")
        roadmap.addNode(node)

        val edge = FlowEdge(
            id = "edge-1-2",
            source = "non-existent", // 존재하지 않는 노드
            target = "node-1",
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            roadmap.addEdge(edge)
        }
        assertTrue(exception.message!!.contains("존재하지 않는 source 노드"))
    }

    @Test
    fun `존재하지 않는 target 노드로 엣지 추가 시 예외 발생`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        val node = createTestNode("node-1")
        roadmap.addNode(node)

        val edge = FlowEdge(
            id = "edge-1-2",
            source = "node-1",
            target = "non-existent", // 존재하지 않는 노드
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            roadmap.addEdge(edge)
        }
        assertTrue(exception.message!!.contains("존재하지 않는 target 노드"))
    }

    @Test
    fun `노드 찾기 성공`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        val node = createTestNode("node-1")
        roadmap.addNode(node)

        // when
        val found = roadmap.findNode("node-1")

        // then
        assertNotNull(found)
        assertEquals(node, found)
    }

    @Test
    fun `존재하지 않는 노드 찾기 시 null 반환`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")

        // when
        val found = roadmap.findNode("non-existent")

        // then
        assertNull(found)
    }

    @Test
    fun `노드 제거 성공`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        val node = createTestNode("node-1")
        roadmap.addNode(node)

        // when
        val removed = roadmap.removeNode("node-1")

        // then
        assertTrue(removed)
        assertEquals(0, roadmap.nodes.size)
    }

    @Test
    fun `노드 제거 시 관련 엣지도 함께 제거`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        val node1 = createTestNode("node-1")
        val node2 = createTestNode("node-2")
        val node3 = createTestNode("node-3")
        roadmap.addNode(node1)
        roadmap.addNode(node2)
        roadmap.addNode(node3)

        val edge1 = FlowEdge("edge-1-2", "node-1", "node-2")
        val edge2 = FlowEdge("edge-2-3", "node-2", "node-3")
        roadmap.addEdge(edge1)
        roadmap.addEdge(edge2)

        // when
        roadmap.removeNode("node-2")

        // then
        assertEquals(2, roadmap.nodes.size) // node-1, node-3 남음
        assertEquals(0, roadmap.edges.size) // edge-1-2, edge-2-3 모두 제거됨
    }

    @Test
    fun `엣지 제거 성공`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        val node1 = createTestNode("node-1")
        val node2 = createTestNode("node-2")
        roadmap.addNode(node1)
        roadmap.addNode(node2)

        val edge = FlowEdge("edge-1-2", "node-1", "node-2")
        roadmap.addEdge(edge)

        // when
        val removed = roadmap.removeEdge("edge-1-2")

        // then
        assertTrue(removed)
        assertEquals(0, roadmap.edges.size)
    }

    @Test
    fun `로드맵 활성화 및 비활성화`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        assertTrue(roadmap.isActive) // 기본값: 활성화

        // when
        roadmap.deactivate()

        // then
        assertFalse(roadmap.isActive)

        // when
        roadmap.activate()

        // then
        assertTrue(roadmap.isActive)
    }

    @Test
    fun `노드 개수 확인`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        roadmap.addNode(createTestNode("node-1"))
        roadmap.addNode(createTestNode("node-2"))
        roadmap.addNode(createTestNode("node-3"))

        // when
        val count = roadmap.nodes.size

        // then
        assertEquals(3, count)
    }

    @Test
    fun `엣지 개수 확인`() {
        // given
        val roadmap = Roadmap.create("Test", "Test", "test")
        roadmap.addNode(createTestNode("node-1"))
        roadmap.addNode(createTestNode("node-2"))
        roadmap.addNode(createTestNode("node-3"))

        roadmap.addEdge(FlowEdge("edge-1-2", "node-1", "node-2"))
        roadmap.addEdge(FlowEdge("edge-2-3", "node-2", "node-3"))

        // when
        val count = roadmap.edges.size

        // then
        assertEquals(2, count)
    }

    private fun createTestNode(id: String): FlowNode {
        return FlowNode(
            id = id,
            type = "default",
            position = Position(100.0, 100.0),
            data = NodeData(
                label = "Test Node",
                description = "Test Description",
            ),
        )
    }
}
