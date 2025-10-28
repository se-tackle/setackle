package org.setackle.backend.application.skill.service

import org.setackle.backend.application.skill.inbound.GetRoadmapTopicUseCase
import org.setackle.backend.application.skill.inbound.RoadmapTopicDetail
import org.setackle.backend.application.skill.inbound.TopicResource
import org.setackle.backend.application.skill.outbound.RoadmapPort
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.domain.skill.model.FlowNode
import org.setackle.backend.domain.skill.vo.RoadmapSlug
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 로드맵 토픽 상세 조회 서비스
 *
 * 로드맵의 특정 토픽(노드)에 대한 상세 정보를 제공합니다.
 */
@Service
@Transactional(readOnly = true)
class GetRoadmapTopicService(
    private val roadmapPort: RoadmapPort,
) : GetRoadmapTopicUseCase {

    private val logger = LoggerFactory.getLogger(GetRoadmapTopicService::class.java)

    /**
     * 토픽 상세 정보 조회
     *
     * @param roadmapSlug 로드맵 슬러그
     * @param nodeId 노드 ID
     * @return 토픽 상세 정보
     * @throws BusinessException 로드맵 또는 토픽을 찾을 수 없는 경우
     */
    override fun getTopic(roadmapSlug: String, nodeId: String): RoadmapTopicDetail {
        logger.debug("Getting topic detail: roadmapSlug=$roadmapSlug, nodeId=$nodeId")

        // 로드맵 조회
        val slug = RoadmapSlug.of(roadmapSlug)
        val roadmap = roadmapPort.findBySlug(slug)
            ?: throw BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                mapOf("roadmapSlug" to roadmapSlug),
            )

        // 노드 검색
        val node = roadmap.findNode(nodeId)
            ?: throw BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                mapOf("nodeId" to nodeId, "roadmapSlug" to roadmapSlug),
            )

        // 선행 학습 노드 ID 추출 (엣지의 target이 현재 노드인 경우의 source 노드들)
        val prerequisites = roadmap.edges
            .filter { it.target == nodeId }
            .map { it.source }

        // 토픽 상세 정보 생성
        val topicDetail = RoadmapTopicDetail(
            roadmapId = roadmap.id!!,
            roadmapName = roadmap.name,
            nodeId = node.id,
            title = node.data.label,
            description = node.data.description,
            resources = extractResources(node),
            questionCount = 0, // TODO: 평가 시스템 연동 시 구현
            prerequisites = prerequisites,
        )

        logger.info("Topic detail retrieved: roadmapSlug=$roadmapSlug, nodeId=$nodeId")
        return topicDetail
    }

    /**
     * 토픽의 학습 리소스 목록 조회
     *
     * @param roadmapSlug 로드맵 슬러그
     * @param nodeId 노드 ID
     * @return 학습 리소스 목록
     * @throws BusinessException 로드맵 또는 토픽을 찾을 수 없는 경우
     */
    override fun getTopicResources(roadmapSlug: String, nodeId: String): List<TopicResource> {
        logger.debug("Getting topic resources: roadmapSlug=$roadmapSlug, nodeId=$nodeId")

        val slug = RoadmapSlug.of(roadmapSlug)
        val roadmap = roadmapPort.findBySlug(slug)
            ?: throw BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                mapOf("roadmapSlug" to roadmapSlug),
            )

        val node = roadmap.findNode(nodeId)
            ?: throw BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                mapOf("nodeId" to nodeId, "roadmapSlug" to roadmapSlug),
            )

        val resources = extractResources(node)

        logger.info("Retrieved ${resources.size} resources for nodeId=$nodeId")
        return resources
    }

    /**
     * 노드에서 학습 리소스 추출
     *
     * @param node 플로우 노드
     * @return 학습 리소스 목록
     */
    private fun extractResources(node: FlowNode): List<TopicResource> {
        // 노드 데이터에서 리소스 추출
        return node.data.resources?.map { resource ->
            TopicResource(
                type = resource.type,
                title = resource.title,
                url = resource.url,
                description = resource.description,
                language = resource.language,
                isPremium = false, // TODO: 프리미엄 리소스 플래그 추가 시 구현
            )
        } ?: emptyList()
    }
}
