package org.setackle.backend.domain.skill.service

import org.setackle.backend.domain.skill.model.FlowEdge
import org.setackle.backend.domain.skill.model.FlowNode

/**
 * 로드맵 검증 도메인 서비스
 *
 * 로드맵 구조의 무결성을 검증하는 서비스
 * - 순환 참조 검사
 * - 고아 노드 검사 (참조되지 않는 노드)
 * - 엣지 연결 무결성 검증
 * - 중복 ID 검사
 */
class RoadmapValidationService {
    /**
     * 로드맵 전체 검증
     *
     * @param nodes 노드 목록
     * @param edges 엣지 목록
     * @return 검증 결과
     * @throws RoadmapValidationException 검증 실패 시
     */
    fun validate(
        nodes: List<FlowNode>,
        edges: List<FlowEdge>,
    ): ValidationResult {
        val errors = mutableListOf<String>()

        // 1. 중복 ID 검사
        val duplicateNodeIds = findDuplicateNodeIds(nodes)
        if (duplicateNodeIds.isNotEmpty()) {
            errors.add("중복된 노드 ID가 있습니다: ${duplicateNodeIds.joinToString()}")
        }

        val duplicateEdgeIds = findDuplicateEdgeIds(edges)
        if (duplicateEdgeIds.isNotEmpty()) {
            errors.add("중복된 엣지 ID가 있습니다: ${duplicateEdgeIds.joinToString()}")
        }

        // 2. 엣지 연결 무결성 검증
        val edgeErrors = validateEdgeConnections(nodes, edges)
        errors.addAll(edgeErrors)

        // 3. 순환 참조 검사
        val cycleResult = detectCycles(edges)
        if (cycleResult.hasCycle) {
            errors.add("순환 참조가 감지되었습니다: ${cycleResult.cyclePath.joinToString(" -> ")}")
        }

        // 4. 고아 노드 검사 (옵션: 루트가 아닌데 진입 엣지가 없는 노드)
        val orphanNodes = findOrphanNodes(nodes, edges)
        if (orphanNodes.isNotEmpty() && orphanNodes.size < nodes.size) {
            // 모든 노드가 고아면 단일 루트가 없는 것이므로 경고만
            errors.add("진입 엣지가 없는 노드가 있습니다 (여러 루트 또는 고아 노드): ${orphanNodes.joinToString()}")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
        )
    }

    /**
     * 중복 노드 ID 찾기
     */
    private fun findDuplicateNodeIds(nodes: List<FlowNode>): List<String> {
        return nodes
            .groupBy { it.id }
            .filter { it.value.size > 1 }
            .keys
            .toList()
    }

    /**
     * 중복 엣지 ID 찾기
     */
    private fun findDuplicateEdgeIds(edges: List<FlowEdge>): List<String> {
        return edges
            .groupBy { it.id }
            .filter { it.value.size > 1 }
            .keys
            .toList()
    }

    /**
     * 엣지 연결 무결성 검증
     * - source와 target 노드가 실제 존재하는지 확인
     */
    private fun validateEdgeConnections(
        nodes: List<FlowNode>,
        edges: List<FlowEdge>,
    ): List<String> {
        val nodeIds = nodes.map { it.id }.toSet()
        val errors = mutableListOf<String>()

        edges.forEach { edge ->
            if (!nodeIds.contains(edge.source)) {
                errors.add("엣지 '${edge.id}'의 source 노드 '${edge.source}'가 존재하지 않습니다")
            }
            if (!nodeIds.contains(edge.target)) {
                errors.add("엣지 '${edge.id}'의 target 노드 '${edge.target}'가 존재하지 않습니다")
            }
        }

        return errors
    }

    /**
     * 순환 참조 검사 (Depth-First Search)
     *
     * @param edges 엣지 목록
     * @return 순환 참조 검사 결과
     */
    fun detectCycles(edges: List<FlowEdge>): CycleDetectionResult {
        // 인접 리스트 구성 (source → targets)
        val adjacencyList = edges.groupBy({ it.source }, { it.target })

        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()

        /**
         * DFS로 순환 참조 탐지
         */
        fun dfs(
            nodeId: String,
            path: List<String>,
        ): CycleDetectionResult {
            if (recursionStack.contains(nodeId)) {
                // 순환 참조 발견
                val cycleStartIndex = path.indexOf(nodeId)
                val cyclePath = if (cycleStartIndex >= 0) {
                    path.subList(cycleStartIndex, path.size) + nodeId
                } else {
                    path + nodeId
                }
                return CycleDetectionResult(hasCycle = true, cyclePath = cyclePath)
            }

            if (visited.contains(nodeId)) {
                return CycleDetectionResult(hasCycle = false)
            }

            visited.add(nodeId)
            recursionStack.add(nodeId)

            // 인접 노드 탐색
            val neighbors = adjacencyList[nodeId] ?: emptyList()
            for (neighbor in neighbors) {
                val result = dfs(neighbor, path + nodeId)
                if (result.hasCycle) {
                    return result
                }
            }

            recursionStack.remove(nodeId)
            return CycleDetectionResult(hasCycle = false)
        }

        // 모든 노드에서 DFS 시작 (연결되지 않은 컴포넌트 대응)
        val allNodeIds = (edges.map { it.source } + edges.map { it.target }).toSet()
        for (nodeId in allNodeIds) {
            if (!visited.contains(nodeId)) {
                val result = dfs(nodeId, emptyList())
                if (result.hasCycle) {
                    return result
                }
            }
        }

        return CycleDetectionResult(hasCycle = false)
    }

    /**
     * 고아 노드 찾기 (진입 엣지가 없는 노드)
     *
     * @param nodes 노드 목록
     * @param edges 엣지 목록
     * @return 고아 노드 ID 목록
     */
    fun findOrphanNodes(
        nodes: List<FlowNode>,
        edges: List<FlowEdge>,
    ): List<String> {
        val nodeIds = nodes.map { it.id }.toSet()
        val targetNodeIds = edges.map { it.target }.toSet()

        // 진입 엣지가 없는 노드 = 루트 노드 or 고아 노드
        return nodeIds.filter { !targetNodeIds.contains(it) }
    }

    /**
     * 루트 노드 찾기 (진입 엣지가 없는 노드)
     *
     * @param nodes 노드 목록
     * @param edges 엣지 목록
     * @return 루트 노드 목록
     */
    fun findRootNodes(
        nodes: List<FlowNode>,
        edges: List<FlowEdge>,
    ): List<FlowNode> {
        val targetNodeIds = edges.map { it.target }.toSet()
        return nodes.filter { !targetNodeIds.contains(it.id) }
    }

    /**
     * 리프 노드 찾기 (진출 엣지가 없는 노드)
     *
     * @param nodes 노드 목록
     * @param edges 엣지 목록
     * @return 리프 노드 목록
     */
    fun findLeafNodes(
        nodes: List<FlowNode>,
        edges: List<FlowEdge>,
    ): List<FlowNode> {
        val sourceNodeIds = edges.map { it.source }.toSet()
        return nodes.filter { !sourceNodeIds.contains(it.id) }
    }

    /**
     * 노드의 깊이 계산 (루트로부터의 거리)
     *
     * @param nodeId 노드 ID
     * @param edges 엣지 목록
     * @return 깊이 (루트 = 0, 도달 불가 = -1)
     */
    fun calculateDepth(
        nodeId: String,
        edges: List<FlowEdge>,
    ): Int {
        // 역방향 인접 리스트 (target → source)
        val reverseAdjacency = edges.groupBy({ it.target }, { it.source })

        val visited = mutableSetOf<String>()

        fun dfs(
            currentId: String,
            depth: Int,
        ): Int {
            if (visited.contains(currentId)) {
                return -1 // 순환 참조 또는 이미 방문
            }

            visited.add(currentId)

            val parents = reverseAdjacency[currentId] ?: emptyList()
            if (parents.isEmpty()) {
                return depth // 루트 도달
            }

            val parentDepths = parents.map { dfs(it, depth + 1) }
            return parentDepths.maxOrNull() ?: -1
        }

        return dfs(nodeId, 0)
    }
}

/**
 * 검증 결과
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
) {
    fun throwIfInvalid() {
        if (!isValid) {
            throw RoadmapValidationException(errors.joinToString("\n"))
        }
    }
}

/**
 * 순환 참조 검사 결과
 */
data class CycleDetectionResult(
    val hasCycle: Boolean,
    val cyclePath: List<String> = emptyList(),
)

/**
 * 로드맵 검증 예외
 */
class RoadmapValidationException(message: String) : IllegalStateException(message)
