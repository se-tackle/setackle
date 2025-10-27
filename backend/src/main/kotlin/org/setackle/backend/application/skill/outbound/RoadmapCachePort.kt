package org.setackle.backend.application.skill.outbound

import org.setackle.backend.domain.skill.model.Roadmap
import java.time.Duration

/**
 * 로드맵 캐시 포트 (출력 포트)
 *
 * Infrastructure 레이어에서 구현되며, 로드맵 데이터의 캐싱을 담당합니다.
 * 성능 최적화를 위해 자주 조회되는 로드맵을 캐시합니다.
 */
interface RoadmapCachePort {
    /**
     * 캐시에서 로드맵 조회
     *
     * @param key 캐시 키 (예: "roadmap:slug:{slug}")
     * @return 캐시된 로드맵 (없으면 null)
     */
    fun getRoadmap(key: String): Roadmap?

    /**
     * 로드맵을 캐시에 저장
     *
     * @param key 캐시 키
     * @param roadmap 저장할 로드맵
     * @param ttl 캐시 유효 시간 (기본: 24시간)
     */
    fun cacheRoadmap(key: String, roadmap: Roadmap, ttl: Duration = Duration.ofHours(24))

    /**
     * 캐시에서 로드맵 삭제
     *
     * @param key 캐시 키
     */
    fun evictRoadmap(key: String)

    /**
     * 특정 패턴의 모든 캐시 삭제
     *
     * @param pattern 캐시 키 패턴 (예: "roadmap:*")
     */
    fun evictRoadmapsByPattern(pattern: String)

    /**
     * 로드맵 목록을 캐시에 저장
     *
     * @param key 캐시 키
     * @param roadmaps 저장할 로드맵 목록
     * @param ttl 캐시 유효 시간 (기본: 1시간)
     */
    fun cacheRoadmapList(key: String, roadmaps: List<Roadmap>, ttl: Duration = Duration.ofHours(1))

    /**
     * 캐시에서 로드맵 목록 조회
     *
     * @param key 캐시 키
     * @return 캐시된 로드맵 목록 (없으면 null)
     */
    fun getRoadmapList(key: String): List<Roadmap>?
}
