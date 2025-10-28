package org.setackle.backend.infrastructure.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.setackle.backend.application.skill.outbound.RoadmapCachePort
import org.setackle.backend.domain.skill.model.Roadmap
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * 로드맵 캐시 어댑터 (RoadmapCachePort 구현)
 * CacheRepository를 사용하여 Redis에 로드맵 데이터를 캐싱합니다.
 *
 * TTL 전략:
 * - 개별 로드맵: 24시간 (자주 변경되지 않는 정적 데이터)
 * - 로드맵 목록: 1시간 (목록 변경 가능성 고려)
 */
@Component
class RoadmapCacheAdapter(
    private val cacheRepository: CacheRepository,
    private val objectMapper: ObjectMapper,
) : RoadmapCachePort {

    companion object {
        // 캐시 키 프리픽스
        const val ROADMAP_PREFIX = "roadmap:"
        const val ROADMAP_LIST_PREFIX = "roadmap:list:"

        // TTL 상수
        val DEFAULT_ROADMAP_TTL = Duration.ofHours(24)
        val DEFAULT_LIST_TTL = Duration.ofHours(1)

        /**
         * 로드맵 캐시 키 생성
         * @param identifier slug 또는 id
         * @return 캐시 키 (예: "roadmap:frontend-development" 또는 "roadmap:id:123")
         */
        fun roadmapKey(identifier: String): String = "$ROADMAP_PREFIX$identifier"

        /**
         * 로드맵 목록 캐시 키 생성
         * @param listType 목록 타입 (예: "all", "skill", "role")
         * @return 캐시 키 (예: "roadmap:list:all")
         */
        fun roadmapListKey(listType: String = "all"): String = "$ROADMAP_LIST_PREFIX$listType"
    }

    override fun getRoadmap(key: String): Roadmap? {
        return try {
            val cached = cacheRepository.get(key, Any::class.java) ?: return null
            // Redis에서 가져온 데이터를 Roadmap으로 변환
            objectMapper.convertValue(cached, Roadmap::class.java)
        } catch (e: Exception) {
            // 역직렬화 실패 시 캐시 무효화
            cacheRepository.delete(key)
            null
        }
    }

    override fun cacheRoadmap(key: String, roadmap: Roadmap, ttl: Duration) {
        try {
            cacheRepository.set(key, roadmap, ttl)
        } catch (e: Exception) {
            // 캐시 저장 실패는 치명적이지 않으므로 로깅만 처리
            println("Failed to cache roadmap: $key, error: ${e.message}")
        }
    }

    override fun evictRoadmap(key: String) {
        cacheRepository.delete(key)
    }

    override fun evictRoadmapsByPattern(pattern: String) {
        cacheRepository.deleteByPattern(pattern)
    }

    override fun cacheRoadmapList(key: String, roadmaps: List<Roadmap>, ttl: Duration) {
        try {
            cacheRepository.set(key, roadmaps, ttl)
        } catch (e: Exception) {
            println("Failed to cache roadmap list: $key, error: ${e.message}")
        }
    }

    override fun getRoadmapList(key: String): List<Roadmap>? {
        return try {
            val cached = cacheRepository.get(key, Any::class.java) ?: return null
            // Redis에서 가져온 데이터를 List<Roadmap>으로 변환
            objectMapper.convertValue(cached, object : TypeReference<List<Roadmap>>() {})
        } catch (e: Exception) {
            // 역직렬화 실패 시 캐시 무효화
            cacheRepository.delete(key)
            null
        }
    }
}
