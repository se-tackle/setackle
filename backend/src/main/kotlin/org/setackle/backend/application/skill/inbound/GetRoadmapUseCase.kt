package org.setackle.backend.application.skill.inbound

/**
 * 로드맵 조회 UseCase
 *
 * 활성화된 로드맵 목록, 특정 로드맵 상세 정보를 조회합니다.
 */
interface GetRoadmapUseCase {
    /**
     * 모든 활성화된 로드맵 목록 조회
     *
     * @return 로드맵 요약 정보 리스트
     */
    fun getAllRoadmaps(): List<RoadmapSummary>

    /**
     * 슬러그로 로드맵 상세 조회
     *
     * @param slug 로드맵 슬러그 (예: "frontend-development")
     * @return 로드맵 상세 정보
     * @throws BusinessException 로드맵을 찾을 수 없는 경우 (ErrorCode.ROADMAP_NOT_FOUND)
     */
    fun getRoadmapBySlug(slug: String): RoadmapDetail

    /**
     * ID로 로드맵 상세 조회
     *
     * @param id 로드맵 ID
     * @return 로드맵 상세 정보
     * @throws BusinessException 로드맵을 찾을 수 없는 경우 (ErrorCode.ROADMAP_NOT_FOUND)
     */
    fun getRoadmapById(id: Long): RoadmapDetail
}

/**
 * 로드맵 요약 정보 (목록 조회 결과)
 */
data class RoadmapSummary(
    val id: Long,
    val name: String,
    val description: String,
    val slug: String,
    val type: String, // RoadmapType.name (SKILL, ROLE)
    val nodeCount: Int,
    val displayOrder: Int,
)

/**
 * 로드맵 상세 정보 (상세 조회 결과)
 */
data class RoadmapDetail(
    val id: Long,
    val name: String,
    val description: String,
    val slug: String,
    val type: String, // RoadmapType.name (SKILL, ROLE)
    val nodes: List<Any>, // FlowNode JSON 형태
    val edges: List<Any>, // FlowEdge JSON 형태
    val dimensions: Map<String, Int>, // width, height
)
