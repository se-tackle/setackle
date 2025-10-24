package org.setackle.backend.domain.skill.service

import org.setackle.backend.domain.skill.model.FlowNode
import org.setackle.backend.domain.skill.model.Position

/**
 * 로드맵 레이아웃 도메인 서비스
 *
 * 노드의 위치를 자동으로 계산하는 레이아웃 알고리즘 제공
 * - 계층 구조 기반 자동 레이아웃
 * - 레벨별 노드 그룹화 및 배치
 * - 가독성을 위한 적절한 간격 유지
 */
class RoadmapLayoutService {
    companion object {
        private const val INITIAL_X_OFFSET = 100.0
        private const val INITIAL_Y_OFFSET = 100.0
        private const val LEVEL_X_GAP = 300.0 // 레벨 간 가로 간격
        private const val NODE_Y_GAP = 150.0 // 노드 간 세로 간격
        private const val LEVEL_Y_GAP = 200.0 // 레벨 간 추가 세로 간격
    }

    /**
     * 노드 목록에 대한 위치 자동 계산
     *
     * @param nodes 위치를 계산할 노드 목록
     * @param getParentId 노드의 부모 ID를 가져오는 함수 (계층 구조용)
     * @return 노드 ID별 계산된 Position 맵
     */
    fun <T> calculateLayout(
        nodes: List<T>,
        getId: (T) -> String,
        getParentId: (T) -> String?,
    ): Map<String, Position> {
        if (nodes.isEmpty()) {
            return emptyMap()
        }

        // 계층별 노드 그룹화
        val hierarchy = buildHierarchy(nodes, getId, getParentId)

        return calculatePositionsByLevel(hierarchy, getId)
    }

    /**
     * FlowNode 목록에 대한 위치 자동 계산 (편의 메서드)
     *
     * @param nodes FlowNode 목록
     * @param parentMap 노드 ID → 부모 ID 매핑
     * @return 노드 ID별 계산된 Position 맵
     */
    fun calculateFlowNodeLayout(
        nodes: List<FlowNode>,
        parentMap: Map<String, String>,
    ): Map<String, Position> {
        return calculateLayout(
            nodes = nodes,
            getId = { it.id },
            getParentId = { parentMap[it.id] },
        )
    }

    /**
     * 계층 구조 빌드 (레벨별 그룹화)
     *
     * @param nodes 노드 목록
     * @param getId 노드 ID 추출 함수
     * @param getParentId 부모 ID 추출 함수
     * @return 레벨별 노드 그룹 (level → nodes)
     */
    private fun <T> buildHierarchy(
        nodes: List<T>,
        getId: (T) -> String,
        getParentId: (T) -> String?,
    ): Map<Int, List<T>> {
        val nodeMap = nodes.associateBy { getId(it) }
        val levels = mutableMapOf<Int, MutableList<T>>()

        /**
         * 재귀적으로 노드의 레벨 계산
         */
        fun calculateLevel(
            node: T,
            visited: MutableSet<String> = mutableSetOf(),
        ): Int {
            val nodeId = getId(node)

            // 순환 참조 감지 (무한 재귀 방지)
            if (visited.contains(nodeId)) {
                return 0 // 루트 레벨로 간주
            }

            visited.add(nodeId)

            val parentId = getParentId(node) ?: return 0 // 루트 노드
            val parent = nodeMap[parentId] ?: return 0 // 부모 없으면 루트

            return calculateLevel(parent, visited) + 1
        }

        nodes.forEach { node ->
            val level = calculateLevel(node)
            levels.getOrPut(level) { mutableListOf() }.add(node)
        }

        return levels.toMap()
    }

    /**
     * 레벨별로 노드 위치 계산
     *
     * @param hierarchy 레벨별 노드 그룹
     * @param getId 노드 ID 추출 함수
     * @return 노드 ID별 Position 맵
     */
    private fun <T> calculatePositionsByLevel(
        hierarchy: Map<Int, List<T>>,
        getId: (T) -> String,
    ): Map<String, Position> {
        val positions = mutableMapOf<String, Position>()
        var cumulativeYOffset = INITIAL_Y_OFFSET

        // 레벨 순서대로 처리 (0부터)
        hierarchy.keys.sorted().forEach { level ->
            val levelNodes = hierarchy[level] ?: return@forEach
            val xOffset = INITIAL_X_OFFSET + (level * LEVEL_X_GAP)

            levelNodes.forEachIndexed { index, node ->
                val nodeId = getId(node)
                positions[nodeId] = Position(
                    x = xOffset,
                    y = cumulativeYOffset + (index * NODE_Y_GAP),
                )
            }

            // 다음 레벨을 위한 Y 오프셋 누적
            cumulativeYOffset += (levelNodes.size * NODE_Y_GAP) + LEVEL_Y_GAP
        }

        return positions
    }

    /**
     * 기존 노드 목록에 새로운 위치 적용
     *
     * @param nodes 원본 노드 목록
     * @param positions 적용할 위치 맵
     * @return 위치가 업데이트된 새 노드 목록
     */
    fun applyLayout(
        nodes: List<FlowNode>,
        positions: Map<String, Position>,
    ): List<FlowNode> {
        return nodes.map { node ->
            val newPosition = positions[node.id] ?: node.position
            node.copy(position = newPosition)
        }
    }

    /**
     * 노드 목록의 경계 박스 계산 (dimensions 자동 계산용)
     *
     * @param nodes 노드 목록
     * @param padding 여백 (기본 100px)
     * @return Pair<width, height>
     */
    fun calculateBounds(
        nodes: List<FlowNode>,
        padding: Double = 100.0,
    ): Pair<Int, Int> {
        if (nodes.isEmpty()) {
            return Pair(1200, 800) // 기본 크기
        }

        val maxX = nodes.maxOfOrNull { it.position.x } ?: 0.0
        val maxY = nodes.maxOfOrNull { it.position.y } ?: 0.0

        val width = (maxX + padding * 2).toInt().coerceAtLeast(1200)
        val height = (maxY + padding * 2).toInt().coerceAtLeast(800)

        return Pair(width, height)
    }
}
