package org.setackle.backend.presentation.skill.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.setackle.backend.application.skill.inbound.GetRoadmapTopicUseCase
import org.setackle.backend.application.skill.inbound.GetRoadmapUseCase
import org.setackle.backend.application.skill.inbound.RoadmapDetail
import org.setackle.backend.application.skill.inbound.RoadmapSummary
import org.setackle.backend.application.skill.inbound.RoadmapTopicDetail
import org.setackle.backend.presentation.common.ApiResponse
import org.setackle.backend.presentation.common.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 로드맵 조회 API
 *
 * 로드맵 목록, 상세 정보, 토픽 정보를 제공하는 REST API 컨트롤러입니다.
 * 모든 엔드포인트는 인증 없이 접근 가능합니다 (공개 API).
 */
@Tag(name = "Roadmap", description = "로드맵 조회 API")
@RestController
@RequestMapping("/api/v1/roadmaps")
class RoadmapController(
    private val getRoadmapUseCase: GetRoadmapUseCase,
    private val getRoadmapTopicUseCase: GetRoadmapTopicUseCase,
) {

    /**
     * 모든 활성화된 로드맵 목록 조회
     *
     * @return 로드맵 요약 정보 리스트
     */
    @Operation(
        summary = "로드맵 목록 조회",
        description = "활성화된 모든 로드맵의 요약 정보를 조회합니다. 인증 없이 접근 가능합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "성공",
            ),
        ],
    )
    @SecurityRequirements(value = []) // 인증 불필요
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllRoadmaps(): ApiResponse<List<RoadmapSummary>> {
        val roadmaps = getRoadmapUseCase.getAllRoadmaps()
        return ApiResponse.success(roadmaps)
    }

    /**
     * 슬러그로 로드맵 상세 조회
     *
     * @param slug 로드맵 슬러그 (예: "frontend-development")
     * @return 로드맵 상세 정보 (노드, 엣지 포함)
     */
    @Operation(
        summary = "로드맵 상세 조회",
        description = "슬러그를 사용하여 특정 로드맵의 상세 정보를 조회합니다. " +
            "노드(토픽), 엣지(연결), 차트 크기 정보가 포함됩니다. 인증 없이 접근 가능합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "성공",
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "로드맵을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @SecurityRequirements(value = []) // 인증 불필요
    @GetMapping("/{slug}")
    @ResponseStatus(HttpStatus.OK)
    fun getRoadmapBySlug(
        @Parameter(description = "로드맵 슬러그", example = "frontend-development")
        @PathVariable slug: String,
    ): ApiResponse<RoadmapDetail> {
        val roadmap = getRoadmapUseCase.getRoadmapBySlug(slug)
        return ApiResponse.success(roadmap)
    }

    /**
     * 로드맵 토픽 상세 조회
     *
     * @param slug 로드맵 슬러그
     * @param nodeId 노드 ID
     * @return 토픽 상세 정보 (학습 리소스, 선행 학습 포함)
     */
    @Operation(
        summary = "토픽 상세 조회",
        description = "로드맵 내의 특정 토픽(노드)에 대한 상세 정보를 조회합니다. " +
            "학습 리소스, 문제 개수, 선행 학습 요구사항이 포함됩니다. 인증 없이 접근 가능합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "성공",
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "로드맵 또는 토픽을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @SecurityRequirements(value = []) // 인증 불필요
    @GetMapping("/{slug}/topics/{nodeId}")
    @ResponseStatus(HttpStatus.OK)
    fun getRoadmapTopic(
        @Parameter(description = "로드맵 슬러그", example = "frontend-development")
        @PathVariable slug: String,
        @Parameter(description = "토픽 노드 ID", example = "react-basics")
        @PathVariable nodeId: String,
    ): ApiResponse<RoadmapTopicDetail> {
        val topic = getRoadmapTopicUseCase.getTopic(slug, nodeId)
        return ApiResponse.success(topic)
    }
}
