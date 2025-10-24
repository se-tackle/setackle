package org.setackle.backend.domain.skill.model

import org.setackle.backend.domain.common.model.AggregateRoot
import java.time.LocalDateTime

/**
 * 로드맵 애그리게이트 루트
 * @xyflow/react와 호환되는 시각화 데이터를 포함
 *
 * Roadmap 애그리게이트 루트 생성
 * - 노드/엣지 관리 메서드
 * - 팩토리 메서드 패턴 적용
 */
class Roadmap(
    val id: Long?,
    var name: String,
    var description: String,
    var slug: String,
    val roadmapType: RoadmapType = RoadmapType.SKILL,
    private val _nodes: MutableList<FlowNode> = mutableListOf(),
    private val _edges: MutableList<FlowEdge> = mutableListOf(),
    var dimensions: Dimensions,
    var displayOrder: Int = 0,
    var isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
) : AggregateRoot() {

    /**
     * 읽기 전용 노드 리스트
     */
    val nodes: List<FlowNode> get() = _nodes.toList()

    /**
     * 읽기 전용 엣지 리스트
     */
    val edges: List<FlowEdge> get() = _edges.toList()

    /**
     * 노드 추가
     * @param node 추가할 FlowNode
     * @throws IllegalArgumentException 이미 존재하는 노드 ID인 경우
     */
    fun addNode(node: FlowNode) {
        require(!_nodes.any { it.id == node.id }) {
            "이미 존재하는 노드 ID입니다: ${node.id}"
        }
        _nodes.add(node)
        updatedAt = LocalDateTime.now()
    }

    /**
     * 노드 제거
     * @param nodeId 제거할 노드 ID
     * @return 제거 성공 여부
     */
    fun removeNode(nodeId: String): Boolean {
        val removed = _nodes.removeIf { it.id == nodeId }
        if (removed) {
            // 해당 노드와 연결된 엣지도 제거
            _edges.removeIf { it.source == nodeId || it.target == nodeId }
            updatedAt = LocalDateTime.now()
        }
        return removed
    }

    /**
     * 엣지 추가
     * @param edge 추가할 FlowEdge
     * @throws IllegalArgumentException source 또는 target 노드가 존재하지 않는 경우
     */
    fun addEdge(edge: FlowEdge) {
        require(_nodes.any { it.id == edge.source }) {
            "존재하지 않는 source 노드입니다: ${edge.source}"
        }
        require(_nodes.any { it.id == edge.target }) {
            "존재하지 않는 target 노드입니다: ${edge.target}"
        }
        _edges.add(edge)
        updatedAt = LocalDateTime.now()
    }

    /**
     * 엣지 제거
     * @param edgeId 제거할 엣지 ID
     * @return 제거 성공 여부
     */
    fun removeEdge(edgeId: String): Boolean {
        val removed = _edges.removeIf { it.id == edgeId }
        if (removed) {
            updatedAt = LocalDateTime.now()
        }
        return removed
    }

    /**
     * 노드 찾기
     * @param nodeId 찾을 노드 ID
     * @return 찾은 노드, 없으면 null
     */
    fun findNode(nodeId: String): FlowNode? {
        return _nodes.find { it.id == nodeId }
    }

    /**
     * 진행률 계산
     * @param completedNodeIds 완료된 노드 ID 집합
     * @return 진행률 (0.0 ~ 100.0)
     */
    fun calculateProgress(completedNodeIds: Set<String>): Double {
        if (_nodes.isEmpty()) return 0.0
        val completedCount = _nodes.count { completedNodeIds.contains(it.id) }
        return (completedCount.toDouble() / _nodes.size) * 100
    }

    /**
     * 로드맵 활성화
     */
    fun activate() {
        isActive = true
        updatedAt = LocalDateTime.now()
    }

    /**
     * 로드맵 비활성화
     */
    fun deactivate() {
        isActive = false
        updatedAt = LocalDateTime.now()
    }

    companion object {
        /**
         * 새 로드맵 생성 (팩토리 메서드)
         * @param name 로드맵 이름
         * @param description 로드맵 설명
         * @param slug URL 슬러그
         * @param roadmapType 로드맵 타입 (기본: SKILL)
         * @param dimensions 캔버스 크기 (기본: 1200x800)
         * @return 새로 생성된 Roadmap 인스턴스
         */
        fun create(
            name: String,
            description: String,
            slug: String,
            roadmapType: RoadmapType = RoadmapType.SKILL,
            dimensions: Dimensions = Dimensions(1200, 800),
        ): Roadmap {
            return Roadmap(
                id = null,
                name = name,
                description = description,
                slug = slug,
                roadmapType = roadmapType,
                dimensions = dimensions,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        }

        /**
         * DB에서 재구성 (팩토리 메서드)
         * @return 재구성된 Roadmap 인스턴스
         */
        fun reconstruct(
            id: Long,
            name: String,
            description: String,
            slug: String,
            roadmapType: RoadmapType,
            nodes: List<FlowNode>,
            edges: List<FlowEdge>,
            dimensions: Dimensions,
            displayOrder: Int,
            isActive: Boolean,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
        ): Roadmap {
            return Roadmap(
                id = id,
                name = name,
                description = description,
                slug = slug,
                roadmapType = roadmapType,
                _nodes = nodes.toMutableList(),
                _edges = edges.toMutableList(),
                dimensions = dimensions,
                displayOrder = displayOrder,
                isActive = isActive,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Roadmap

        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "Roadmap(id=$id, name='$name', slug='$slug', type=$roadmapType, nodeCount=${_nodes.size}, edgeCount=${_edges.size})"
}

/**
 * 로드맵 타입
 * SKILL: 특정 기술 학습 로드맵 (React, Spring Boot 등)
 * ROLE: 직무별 로드맵 (Frontend Developer, Backend Developer 등)
 */
enum class RoadmapType(val displayName: String) {
    SKILL("스킬 로드맵"),
    ROLE("역할 로드맵"),
}
