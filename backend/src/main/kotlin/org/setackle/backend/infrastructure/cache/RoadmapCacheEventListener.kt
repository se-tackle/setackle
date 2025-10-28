package org.setackle.backend.infrastructure.cache

import org.setackle.backend.application.skill.outbound.RoadmapCachePort
import org.setackle.backend.domain.skill.events.RoadmapActivatedEvent
import org.setackle.backend.domain.skill.events.RoadmapCreatedEvent
import org.setackle.backend.domain.skill.events.RoadmapDeactivatedEvent
import org.setackle.backend.domain.skill.events.RoadmapDeletedEvent
import org.setackle.backend.domain.skill.events.RoadmapUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * 로드맵 캐시 이벤트 리스너
 * 로드맵 도메인 이벤트를 감지하여 캐시를 무효화합니다.
 *
 * 캐시 무효화 전략:
 * - 로드맵 생성: 목록 캐시만 무효화
 * - 로드맵 업데이트: 해당 로드맵 + 목록 캐시 무효화
 * - 로드맵 삭제: 해당 로드맵 + 목록 캐시 무효화
 * - 로드맵 활성화: 전체 로드맵 캐시 무효화 (목록과 개별 모두)
 * - 로드맵 비활성화: 전체 로드맵 캐시 무효화 (목록과 개별 모두)
 */
@Component
class RoadmapCacheEventListener(
    private val roadmapCachePort: RoadmapCachePort,
) {

    private val logger = LoggerFactory.getLogger(RoadmapCacheEventListener::class.java)

    /**
     * 로드맵 생성 이벤트 처리
     * 새 로드맵이 추가되면 목록 캐시를 무효화합니다.
     */
    @EventListener
    fun onRoadmapCreated(event: RoadmapCreatedEvent) {
        try {
            logger.debug("Handling RoadmapCreatedEvent: roadmapId=${event.roadmapId}, slug=${event.slug}")

            // 로드맵 목록 캐시 무효화 (모든 목록 패턴)
            roadmapCachePort.evictRoadmapsByPattern("${RoadmapCacheAdapter.ROADMAP_LIST_PREFIX}*")

            logger.info("Cache evicted for roadmap creation: slug=${event.slug}")
        } catch (e: Exception) {
            logger.error("Failed to handle RoadmapCreatedEvent", e)
        }
    }

    /**
     * 로드맵 업데이트 이벤트 처리
     * 로드맵이 수정되면 해당 로드맵과 목록 캐시를 무효화합니다.
     */
    @EventListener
    fun onRoadmapUpdated(event: RoadmapUpdatedEvent) {
        try {
            logger.debug("Handling RoadmapUpdatedEvent: roadmapId=${event.roadmapId}, slug=${event.slug}")

            // 해당 로드맵 캐시 무효화
            val roadmapKey = RoadmapCacheAdapter.roadmapKey(event.slug)
            roadmapCachePort.evictRoadmap(roadmapKey)

            // 로드맵 목록 캐시 무효화
            roadmapCachePort.evictRoadmapsByPattern("${RoadmapCacheAdapter.ROADMAP_LIST_PREFIX}*")

            logger.info("Cache evicted for roadmap update: slug=${event.slug}")
        } catch (e: Exception) {
            logger.error("Failed to handle RoadmapUpdatedEvent", e)
        }
    }

    /**
     * 로드맵 삭제 이벤트 처리
     * 로드맵이 삭제되면 해당 로드맵과 목록 캐시를 무효화합니다.
     */
    @EventListener
    fun onRoadmapDeleted(event: RoadmapDeletedEvent) {
        try {
            logger.debug("Handling RoadmapDeletedEvent: roadmapId=${event.roadmapId}, slug=${event.slug}")

            // 해당 로드맵 캐시 무효화
            val roadmapKey = RoadmapCacheAdapter.roadmapKey(event.slug)
            roadmapCachePort.evictRoadmap(roadmapKey)

            // 로드맵 목록 캐시 무효화
            roadmapCachePort.evictRoadmapsByPattern("${RoadmapCacheAdapter.ROADMAP_LIST_PREFIX}*")

            logger.info("Cache evicted for roadmap deletion: slug=${event.slug}")
        } catch (e: Exception) {
            logger.error("Failed to handle RoadmapDeletedEvent", e)
        }
    }

    /**
     * 로드맵 활성화 이벤트 처리
     * 로드맵이 활성화되면 전체 로드맵 캐시를 무효화합니다.
     */
    @EventListener
    fun onRoadmapActivated(event: RoadmapActivatedEvent) {
        try {
            logger.debug(
                "Handling RoadmapActivatedEvent: " +
                    "roadmapId=${event.roadmapId}, roadmapName=${event.roadmapName}",
            )

            // 전체 로드맵 캐시 무효화 (slug 정보가 없으므로 패턴 매칭)
            roadmapCachePort.evictRoadmapsByPattern("${RoadmapCacheAdapter.ROADMAP_PREFIX}*")

            logger.info("Cache evicted for roadmap activation: roadmapId=${event.roadmapId}")
        } catch (e: Exception) {
            logger.error("Failed to handle RoadmapActivatedEvent", e)
        }
    }

    /**
     * 로드맵 비활성화 이벤트 처리
     * 로드맵이 비활성화되면 전체 로드맵 캐시를 무효화합니다.
     */
    @EventListener
    fun onRoadmapDeactivated(event: RoadmapDeactivatedEvent) {
        try {
            logger.debug(
                "Handling RoadmapDeactivatedEvent: " +
                    "roadmapId=${event.roadmapId}, roadmapName=${event.roadmapName}",
            )

            // 전체 로드맵 캐시 무효화 (slug 정보가 없으므로 패턴 매칭)
            roadmapCachePort.evictRoadmapsByPattern("${RoadmapCacheAdapter.ROADMAP_PREFIX}*")

            logger.info("Cache evicted for roadmap deactivation: roadmapId=${event.roadmapId}")
        } catch (e: Exception) {
            logger.error("Failed to handle RoadmapDeactivatedEvent", e)
        }
    }
}
