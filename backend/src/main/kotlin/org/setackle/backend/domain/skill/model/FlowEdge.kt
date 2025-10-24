package org.setackle.backend.domain.skill.model

/**
 * @xyflow/react 호환 엣지 (노드 간 연결선)
 *
 * FlowEdge 도메인 모델
 * - @xyflow/react의 Edge 타입과 호환
 * - 노드 간 연결 관계 표현
 *
 * @property id 엣지 고유 ID
 * @property source 시작 노드 ID
 * @property target 끝 노드 ID
 * @property type 엣지 타입 (기본값: smoothstep)
 * @property animated 애니메이션 여부 (기본값: false)
 * @property style 추가 스타일 (선택적)
 */
data class FlowEdge(
    val id: String,
    val source: String,
    val target: String,
    val type: String? = "smoothstep",
    val animated: Boolean? = false,
    val style: Map<String, Any>? = null,
) {
    init {
        require(id.isNotBlank()) { "엣지 ID는 비어있을 수 없습니다" }
        require(source.isNotBlank()) { "시작 노드 ID는 비어있을 수 없습니다" }
        require(target.isNotBlank()) { "끝 노드 ID는 비어있을 수 없습니다" }
        require(source != target) { "시작 노드와 끝 노드가 같을 수 없습니다" }
    }

    /**
     * 순환 참조 여부 확인
     * (단순 자기 자신 참조만 체크, 전체 그래프 순환 참조는 RoadmapValidationService에서 체크)
     */
    fun isSelfLoop(): Boolean = source == target

    /**
     * 특정 노드와 연결되어 있는지 확인
     */
    fun connectsTo(nodeId: String): Boolean {
        return source == nodeId || target == nodeId
    }

    /**
     * 엣지 반전 (방향 반대로)
     */
    fun reverse(): FlowEdge {
        return copy(
            id = "${id}_reversed",
            source = target,
            target = source,
        )
    }

    companion object {
        /**
         * 기본 엣지 생성
         */
        fun create(source: String, target: String): FlowEdge {
            return FlowEdge(
                id = "edge_${source}_to_$target",
                source = source,
                target = target,
            )
        }

        /**
         * 애니메이션 엣지 생성
         */
        fun createAnimated(source: String, target: String): FlowEdge {
            return FlowEdge(
                id = "edge_${source}_to_$target",
                source = source,
                target = target,
                animated = true,
            )
        }

        /**
         * 커스텀 타입 엣지 생성
         */
        fun createWithType(source: String, target: String, type: String): FlowEdge {
            return FlowEdge(
                id = "edge_${source}_to_$target",
                source = source,
                target = target,
                type = type,
            )
        }
    }
}

/**
 * 엣지 타입 상수
 * @xyflow/react에서 제공하는 기본 엣지 타입들
 */
object EdgeType {
    const val DEFAULT = "default"
    const val STRAIGHT = "straight"
    const val STEP = "step"
    const val SMOOTHSTEP = "smoothstep"
    const val BEZIER = "bezier"
}
