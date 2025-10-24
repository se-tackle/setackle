package org.setackle.backend.domain.skill.events

import org.setackle.backend.domain.common.event.DomainEvent
import org.setackle.backend.domain.skill.model.FlowEdge
import org.setackle.backend.domain.skill.model.FlowNode
import org.setackle.backend.domain.user.model.ProgressStatus
import org.setackle.backend.domain.user.vo.UserId
import java.time.LocalDateTime

/**
 * 노드 추가 이벤트
 *
 * 로드맵 도메인 이벤트
 * - 로드맵에 새로운 노드가 추가되었을 때 발생
 *
 * @property roadmapId 로드맵 ID
 * @property node 추가된 노드
 * @property occurredOn 이벤트 발생 시간
 */
data class NodeAddedEvent(
    val roadmapId: Long,
    val node: FlowNode,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent {
    /**
     * 추가된 노드 ID
     */
    val nodeId: String = node.id

    /**
     * 추가된 노드 타입
     */
    val nodeType: String = node.type
}

/**
 * 노드 업데이트 이벤트
 *
 * - 노드의 데이터나 위치가 변경되었을 때 발생
 *
 * @property roadmapId 로드맵 ID
 * @property nodeId 업데이트된 노드 ID
 * @property previousNode 이전 노드 상태 (선택적)
 * @property updatedNode 업데이트된 노드
 * @property occurredOn 이벤트 발생 시간
 */
data class NodeUpdatedEvent(
    val roadmapId: Long,
    val nodeId: String,
    val previousNode: FlowNode?,
    val updatedNode: FlowNode,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent {
    /**
     * 위치가 변경되었는지 확인
     */
    fun isPositionChanged(): Boolean {
        return previousNode?.position != updatedNode.position
    }

    /**
     * 데이터가 변경되었는지 확인
     */
    fun isDataChanged(): Boolean {
        return previousNode?.data != updatedNode.data
    }
}

/**
 * 노드 제거 이벤트
 *
 * - 노드가 로드맵에서 제거되었을 때 발생
 *
 * @property roadmapId 로드맵 ID
 * @property nodeId 제거된 노드 ID
 * @property removedNode 제거된 노드 정보
 * @property occurredOn 이벤트 발생 시간
 */
data class NodeRemovedEvent(
    val roadmapId: Long,
    val nodeId: String,
    val removedNode: FlowNode,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent

/**
 * 노드 연결 이벤트 (엣지 추가)
 *
 * - 두 노드가 엣지로 연결되었을 때 발생
 *
 * @property roadmapId 로드맵 ID
 * @property edge 추가된 엣지
 * @property occurredOn 이벤트 발생 시간
 */
data class NodesConnectedEvent(
    val roadmapId: Long,
    val edge: FlowEdge,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent {
    /**
     * 시작 노드 ID
     */
    val sourceNodeId: String = edge.source

    /**
     * 끝 노드 ID
     */
    val targetNodeId: String = edge.target

    /**
     * 엣지 ID
     */
    val edgeId: String = edge.id
}

/**
 * 노드 연결 해제 이벤트 (엣지 제거)
 *
 * - 두 노드 간의 연결이 해제되었을 때 발생
 *
 * @property roadmapId 로드맵 ID
 * @property edgeId 제거된 엣지 ID
 * @property removedEdge 제거된 엣지 정보
 * @property occurredOn 이벤트 발생 시간
 */
data class NodesDisconnectedEvent(
    val roadmapId: Long,
    val edgeId: String,
    val removedEdge: FlowEdge,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent

/**
 * 진행도 변경 이벤트
 *
 * - 사용자의 로드맵 진행 상태가 변경되었을 때 발생
 *
 * @property userId 사용자 ID
 * @property roadmapId 로드맵 ID
 * @property nodeId 상태가 변경된 노드 ID
 * @property previousStatus 이전 진행 상태
 * @property currentStatus 현재 진행 상태
 * @property occurredOn 이벤트 발생 시간
 */
data class ProgressChangedEvent(
    val userId: UserId,
    val roadmapId: Long,
    val nodeId: String,
    val previousStatus: ProgressStatus,
    val currentStatus: ProgressStatus,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent {
    /**
     * 상태가 실제로 변경되었는지 확인
     */
    fun isStatusChanged(): Boolean {
        return previousStatus != currentStatus
    }

    /**
     * 완료 상태로 변경되었는지 확인
     */
    fun isCompletedNow(): Boolean {
        return currentStatus == ProgressStatus.DONE && previousStatus != ProgressStatus.DONE
    }

    /**
     * 학습 시작 이벤트인지 확인
     */
    fun isStartedLearning(): Boolean {
        return currentStatus == ProgressStatus.LEARNING && previousStatus == ProgressStatus.PENDING
    }
}

/**
 * 로드맵 활성화 이벤트
 *
 * - 로드맵이 활성화되었을 때 발생
 *
 * @property roadmapId 로드맵 ID
 * @property roadmapName 로드맵 이름
 * @property occurredOn 이벤트 발생 시간
 */
data class RoadmapActivatedEvent(
    val roadmapId: Long,
    val roadmapName: String,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent

/**
 * 로드맵 비활성화 이벤트
 *
 * - 로드맵이 비활성화되었을 때 발생
 *
 * @property roadmapId 로드맵 ID
 * @property roadmapName 로드맵 이름
 * @property occurredOn 이벤트 발생 시간
 */
data class RoadmapDeactivatedEvent(
    val roadmapId: Long,
    val roadmapName: String,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent

/**
 * 진행도 초기화 이벤트
 *
 * - 사용자의 로드맵 진행 상황이 초기화되었을 때 발생
 *
 * @property userId 사용자 ID
 * @property roadmapId 로드맵 ID
 * @property resetNodeCount 초기화된 노드 개수
 * @property occurredOn 이벤트 발생 시간
 */
data class ProgressResetEvent(
    val userId: UserId,
    val roadmapId: Long,
    val resetNodeCount: Int,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
) : DomainEvent
