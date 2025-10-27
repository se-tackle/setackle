package org.setackle.backend.application.skill.outbound

import org.setackle.backend.domain.common.event.DomainEvent

/**
 * 로드맵 도메인 이벤트 발행 포트 (출력 포트)
 *
 * Infrastructure 레이어에서 구현되며, 로드맵 관련 도메인 이벤트를 발행합니다.
 * Spring의 ApplicationEventPublisher를 활용하여 이벤트 기반 아키텍처를 지원합니다.
 */
interface RoadmapEventPort {
    /**
     * 로드맵 이벤트 발행
     *
     * @param event 발행할 도메인 이벤트
     */
    fun publish(event: DomainEvent)

    /**
     * 여러 이벤트를 일괄 발행
     *
     * @param events 발행할 이벤트 목록
     */
    fun publishAll(events: List<DomainEvent>)
}
