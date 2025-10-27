package org.setackle.backend.application.skill.service

import org.setackle.backend.application.skill.inbound.GetRoadmapUseCase
import org.setackle.backend.application.skill.inbound.RoadmapDetail
import org.setackle.backend.application.skill.inbound.RoadmapSummary
import org.setackle.backend.application.skill.outbound.RoadmapCachePort
import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.domain.skill.model.Roadmap
import org.setackle.backend.domain.skill.vo.RoadmapSlug
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture

/**
 * 로드맵 조회 서비스
 *
 * 로드맵 목록 및 상세 정보를 조회하는 유스케이스를 구현합니다.
 * 캐시를 우선적으로 조회하여 성능을 최적화합니다.
 */
@Service
@Transactional(readOnly = true)
class GetRoadmapService(
    private val roadmapPort: RoadmapPort,
    private val roadmapCachePort: RoadmapCachePort,
) : GetRoadmapUseCase {

    private val logger = LoggerFactory.getLogger(GetRoadmapService::class.java)

    override fun getAllRoadmaps(): List<RoadmapSummary> {
        logger.debug("로드맵 목록 조회 시작")

        // 1. 캐시에서 목록 조회 시도
        val cacheKey = "roadmap:list:all"
        roadmapCachePort.getRoadmapList(cacheKey)?.let { cachedRoadmaps ->
            logger.debug("캐시에서 로드맵 목록 조회 성공: {} 개", cachedRoadmaps.size)
            return toRoadmapSummaryList(cachedRoadmaps)
        }

        // 2. DB에서 조회
        val roadmaps = roadmapPort.findAll()
            .filter { it.isActive }
            .sortedBy { it.displayOrder }

        // 3. 비동기로 캐시 저장
        CompletableFuture.runAsync {
            roadmapCachePort.cacheRoadmapList(cacheKey, roadmaps)
            logger.debug("로드맵 목록 캐시 저장 완료")
        }

        logger.info("로드맵 목록 조회 완료: {} 개", roadmaps.size)
        return toRoadmapSummaryList(roadmaps)
    }

    override fun getRoadmapBySlug(slug: String): RoadmapDetail {
        logger.debug("로드맵 조회 시작 - slug: {}", slug)

        // 1. 캐시에서 조회 시도
        val cacheKey = "roadmap:slug:$slug"
        roadmapCachePort.getRoadmap(cacheKey)?.let { cachedRoadmap ->
            logger.debug("캐시에서 로드맵 조회 성공 - slug: {}", slug)
            return toRoadmapDetail(cachedRoadmap)
        }

        // 2. DB에서 조회
        val roadmapSlug = RoadmapSlug.of(slug)
        val roadmap = roadmapPort.findBySlug(roadmapSlug)
            ?: throw BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                mapOf("slug" to slug, "message" to "로드맵을 찾을 수 없습니다"),
            )

        // 3. 비동기로 캐시 저장
        CompletableFuture.runAsync {
            roadmapCachePort.cacheRoadmap(cacheKey, roadmap)
            logger.debug("로드맵 캐시 저장 완료 - slug: {}", slug)
        }

        logger.info("로드맵 조회 완료 - slug: {}, id: {}", slug, roadmap.id)
        return toRoadmapDetail(roadmap)
    }

    override fun getRoadmapById(id: Long): RoadmapDetail {
        logger.debug("로드맵 조회 시작 - id: {}", id)

        // 1. 캐시에서 조회 시도
        val cacheKey = "roadmap:id:$id"
        roadmapCachePort.getRoadmap(cacheKey)?.let { cachedRoadmap ->
            logger.debug("캐시에서 로드맵 조회 성공 - id: {}", id)
            return toRoadmapDetail(cachedRoadmap)
        }

        // 2. DB에서 조회
        val roadmap = roadmapPort.findById(id)
            ?: throw BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                mapOf("id" to id, "message" to "로드맵을 찾을 수 없습니다"),
            )

        // 3. 비동기로 캐시 저장
        CompletableFuture.runAsync {
            roadmapCachePort.cacheRoadmap(cacheKey, roadmap)
            logger.debug("로드맵 캐시 저장 완료 - id: {}", id)
        }

        logger.info("로드맵 조회 완료 - id: {}, slug: {}", id, roadmap.slug)
        return toRoadmapDetail(roadmap)
    }

    /**
     * 로드맵 목록을 요약 정보로 변환
     */
    private fun toRoadmapSummaryList(roadmaps: List<Roadmap>): List<RoadmapSummary> {
        return roadmaps.map { roadmap ->
            RoadmapSummary(
                id = roadmap.id!!,
                name = roadmap.name,
                description = roadmap.description,
                slug = roadmap.slug,
                type = roadmap.roadmapType.name,
                nodeCount = roadmap.nodes.size,
                displayOrder = roadmap.displayOrder,
            )
        }
    }

    /**
     * 도메인 엔티티를 상세 DTO로 변환
     */
    private fun toRoadmapDetail(roadmap: Roadmap): RoadmapDetail {
        return RoadmapDetail(
            id = roadmap.id!!,
            name = roadmap.name,
            description = roadmap.description,
            slug = roadmap.slug,
            type = roadmap.roadmapType.name,
            nodes = roadmap.nodes.map { node ->
                mapOf(
                    "id" to node.id,
                    "type" to node.type,
                    "position" to mapOf(
                        "x" to node.position.x,
                        "y" to node.position.y,
                    ),
                    "data" to mapOf(
                        "label" to node.data.label,
                        "description" to (node.data.description ?: ""),
                        "nodeId" to (node.data.nodeId ?: node.id),
                        "resources" to (node.data.resources ?: emptyList<Any>()),
                        "questionCount" to (node.data.questionCount ?: 0),
                    ),
                    "style" to (node.style ?: emptyMap<String, Any>()),
                    "className" to (node.className ?: ""),
                )
            },
            edges = roadmap.edges.map { edge ->
                mapOf(
                    "id" to edge.id,
                    "source" to edge.source,
                    "target" to edge.target,
                    "type" to (edge.type ?: "smoothstep"),
                    "animated" to (edge.animated ?: false),
                    "style" to (edge.style ?: emptyMap<String, Any>()),
                )
            },
            dimensions = mapOf(
                "width" to roadmap.dimensions.width,
                "height" to roadmap.dimensions.height,
            ),
        )
    }
}
