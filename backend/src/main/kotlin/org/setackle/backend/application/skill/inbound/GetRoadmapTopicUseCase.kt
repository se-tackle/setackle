package org.setackle.backend.application.skill.inbound

/**
 * 로드맵 토픽 상세 조회 UseCase
 *
 * 로드맵의 특정 토픽(노드)에 대한 상세 정보와 학습 리소스를 조회합니다.
 */
interface GetRoadmapTopicUseCase {
    /**
     * 토픽 상세 정보 조회
     *
     * @param roadmapSlug 로드맵 슬러그
     * @param nodeId 노드 ID
     * @return 토픽 상세 정보
     * @throws BusinessException 로드맵 또는 토픽을 찾을 수 없는 경우
     */
    fun getTopic(roadmapSlug: String, nodeId: String): RoadmapTopicDetail

    /**
     * 토픽의 학습 리소스 목록 조회
     *
     * @param roadmapSlug 로드맵 슬러그
     * @param nodeId 노드 ID
     * @return 학습 리소스 목록
     * @throws BusinessException 로드맵 또는 토픽을 찾을 수 없는 경우
     */
    fun getTopicResources(roadmapSlug: String, nodeId: String): List<TopicResource>
}

/**
 * 토픽 상세 정보
 */
data class RoadmapTopicDetail(
    val roadmapId: Long,
    val roadmapName: String,
    val nodeId: String,
    val title: String,
    val description: String?,
    val resources: List<TopicResource>,
    val questionCount: Int,
    val prerequisites: List<String>, // 선행 학습 노드 ID 목록
)

/**
 * 토픽 학습 리소스
 */
data class TopicResource(
    val type: String, // article, video, course, book, documentation
    val title: String,
    val url: String,
    val description: String?,
    val language: String, // ko, en
    val isPremium: Boolean = false,
)
