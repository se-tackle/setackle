package org.setackle.backend.infrastructure.cache

import org.setackle.backend.application.skill.outbound.RoadmapCachePort
import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * 로드맵 캐시 워머
 * 애플리케이션 시작 시 인기 있는 로드맵을 미리 캐시에 로딩하여 초기 응답 속도를 향상시킵니다.
 */
@Component
class RoadmapCacheWarmer(
    private val roadmapPort: RoadmapPort,
    private val roadmapCachePort: RoadmapCachePort,
) {

    private val logger = LoggerFactory.getLogger(RoadmapCacheWarmer::class.java)

    /**
     * 애플리케이션 준비 완료 시 캐시 워밍 실행
     */
    @EventListener(ApplicationReadyEvent::class)
    fun warmUpCache() {
        try {
            logger.info("Starting roadmap cache warming...")

            // 모든 활성 로드맵 조회 및 캐시
            val roadmaps = roadmapPort.findAll()
            if (roadmaps.isEmpty()) {
                logger.info("No roadmaps found to cache")
                return
            }

            // 로드맵 목록 캐시
            val listKey = RoadmapCacheAdapter.roadmapListKey()
            roadmapCachePort.cacheRoadmapList(
                listKey,
                roadmaps,
                RoadmapCacheAdapter.DEFAULT_LIST_TTL,
            )

            // 개별 로드맵 캐시 (slug 기반)
            roadmaps.forEach { roadmap ->
                val key = RoadmapCacheAdapter.roadmapKey(roadmap.slug)
                roadmapCachePort.cacheRoadmap(
                    key,
                    roadmap,
                    RoadmapCacheAdapter.DEFAULT_ROADMAP_TTL,
                )
            }

            logger.info("Successfully warmed up cache with ${roadmaps.size} roadmaps")
        } catch (e: Exception) {
            // 캐시 워밍 실패는 치명적이지 않으므로 로깅만 처리
            logger.error("Failed to warm up roadmap cache", e)
        }
    }
}
