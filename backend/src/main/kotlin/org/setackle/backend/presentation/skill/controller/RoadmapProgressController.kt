package org.setackle.backend.presentation.skill.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.setackle.backend.application.user.inbound.*
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.setackle.backend.infrastructure.security.CustomUserDetails
import org.setackle.backend.presentation.common.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * 로드맵 진행도 관리 API
 *
 * 사용자의 로드맵 학습 진행 상태를 관리하는 REST API 컨트롤러입니다.
 * 모든 엔드포인트는 인증이 필요합니다.
 */
@Tag(name = "Roadmap Progress", description = "로드맵 진행도 관리 API")
@RestController
@RequestMapping("/api/v1")
class RoadmapProgressController(
    private val updateRoadmapProgressUseCase: UpdateRoadmapProgressUseCase,
    private val getUserRoadmapProgressUseCase: GetUserRoadmapProgressUseCase,
    private val bulkUpdateProgressUseCase: BulkUpdateProgressUseCase,
) {

    /**
     * 특정 로드맵의 사용자 진행 상황 조회
     *
     * @param slug 로드맵 슬러그
     * @param userDetails 인증된 사용자 정보
     * @return 사용자 진행 상황
     */
    @Operation(
        summary = "진행도 조회",
        description = "사용자의 특정 로드맵 진행 상황을 조회합니다. " +
            "완료, 학습 중, 건너뛴 노드 목록과 전체 진행률이 포함됩니다.",
    )
    @GetMapping("/roadmaps/{slug}/progress")
    @ResponseStatus(HttpStatus.OK)
    fun getProgress(
        @Parameter(description = "로드맵 슬러그", example = "frontend-development")
        @PathVariable slug: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<UserProgressResult> {
        val userId = getUserId(userDetails)
        val progress = getUserRoadmapProgressUseCase.getProgress(userId, slug)
        return ApiResponse.success(progress)
    }

    /**
     * 노드 진행 상태 업데이트
     *
     * @param slug 로드맵 슬러그
     * @param request 진행 상태 업데이트 요청
     * @param userDetails 인증된 사용자 정보
     * @return 업데이트 결과
     */
    @Operation(
        summary = "진행도 업데이트",
        description = "특정 노드의 진행 상태를 업데이트합니다. " +
            "상태: PENDING(미시작), DONE(완료), LEARNING(학습중), SKIPPED(건너뜀)",
    )
    @PostMapping("/roadmaps/{slug}/progress")
    @ResponseStatus(HttpStatus.OK)
    fun updateProgress(
        @Parameter(description = "로드맵 슬러그", example = "frontend-development")
        @PathVariable slug: String,
        @Valid @RequestBody request: UpdateProgressRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<UpdateProgressResult> {
        val userId = getUserId(userDetails)
        val command = UpdateProgressCommand(
            userId = userId,
            roadmapSlug = slug,
            nodeId = request.nodeId,
            status = request.status,
        )
        val result = updateRoadmapProgressUseCase.updateProgress(command)
        return ApiResponse.success(result)
    }

    /**
     * 여러 노드의 진행 상태를 일괄 업데이트
     *
     * @param slug 로드맵 슬러그
     * @param request 일괄 업데이트 요청
     * @param userDetails 인증된 사용자 정보
     * @return 일괄 업데이트 결과
     */
    @Operation(
        summary = "일괄 진행도 업데이트",
        description = "여러 노드의 진행 상태를 한 번에 업데이트합니다. " +
            "평가 완료 후 자동 업데이트나 CSV 임포트에 활용됩니다.",
    )
    @PutMapping("/roadmaps/{slug}/progress/bulk")
    @ResponseStatus(HttpStatus.OK)
    fun bulkUpdateProgress(
        @Parameter(description = "로드맵 슬러그", example = "frontend-development")
        @PathVariable slug: String,
        @Valid @RequestBody request: BulkUpdateProgressRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<BulkUpdateProgressResult> {
        val userId = getUserId(userDetails)
        val command = BulkUpdateProgressCommand(
            userId = userId,
            roadmapSlug = slug,
            updates = request.updates,
        )
        val result = bulkUpdateProgressUseCase.bulkUpdate(command)
        return ApiResponse.success(result)
    }

    /**
     * 로드맵 진행 상황 초기화
     *
     * @param slug 로드맵 슬러그
     * @param userDetails 인증된 사용자 정보
     * @return 초기화 결과
     */
    @Operation(
        summary = "진행도 초기화",
        description = "사용자의 로드맵 진행 상황을 완전히 초기화합니다. " +
            "모든 완료/학습중/건너뜀 상태가 삭제됩니다.",
    )
    @DeleteMapping("/roadmaps/{slug}/progress")
    @ResponseStatus(HttpStatus.OK)
    fun resetProgress(
        @Parameter(description = "로드맵 슬러그", example = "frontend-development")
        @PathVariable slug: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<ResetProgressResult> {
        val userId = getUserId(userDetails)
        val result = updateRoadmapProgressUseCase.resetProgress(userId, slug)
        return ApiResponse.success(result)
    }

    /**
     * 로드맵 즐겨찾기 토글
     *
     * @param slug 로드맵 슬러그
     * @param userDetails 인증된 사용자 정보
     * @return 즐겨찾기 토글 결과
     */
    @Operation(
        summary = "즐겨찾기 토글",
        description = "로드맵을 즐겨찾기에 추가하거나 제거합니다.",
    )
    @PostMapping("/roadmaps/{slug}/favorite")
    @ResponseStatus(HttpStatus.OK)
    fun toggleFavorite(
        @Parameter(description = "로드맵 슬러그", example = "frontend-development")
        @PathVariable slug: String,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<ToggleFavoriteResult> {
        val userId = getUserId(userDetails)
        val result = updateRoadmapProgressUseCase.toggleFavorite(userId, slug)
        return ApiResponse.success(result)
    }

    /**
     * 사용자의 모든 진행 중인 로드맵 목록 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @return 진행 중인 로드맵 목록
     */
    @Operation(
        summary = "진행 중인 로드맵 목록",
        description = "사용자가 진행 중인 모든 로드맵 목록을 조회합니다.",
    )
    @GetMapping("/progress/roadmaps")
    @ResponseStatus(HttpStatus.OK)
    fun getUserRoadmaps(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<List<UserRoadmapSummary>> {
        val userId = getUserId(userDetails)
        val roadmaps = getUserRoadmapProgressUseCase.getUserRoadmaps(userId)
        return ApiResponse.success(roadmaps)
    }

    /**
     * 사용자의 즐겨찾기 로드맵 목록 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @return 즐겨찾기 로드맵 목록
     */
    @Operation(
        summary = "즐겨찾기 로드맵 목록",
        description = "사용자가 즐겨찾기한 로드맵 목록을 조회합니다.",
    )
    @GetMapping("/progress/favorites")
    @ResponseStatus(HttpStatus.OK)
    fun getFavoriteRoadmaps(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ApiResponse<List<UserRoadmapSummary>> {
        val userId = getUserId(userDetails)
        val favorites = getUserRoadmapProgressUseCase.getFavoriteRoadmaps(userId)
        return ApiResponse.success(favorites)
    }

    /**
     * 인증된 사용자의 ID 추출
     *
     * @param userDetails 사용자 인증 정보
     * @return 사용자 ID
     * @throws BusinessException 사용자 ID를 찾을 수 없는 경우
     */
    private fun getUserId(userDetails: CustomUserDetails): Long {
        return userDetails.getUserId()
            ?: throw BusinessException(
                ErrorCode.USER_NOT_FOUND,
                mapOf("reason" to "User ID not found in authentication"),
            )
    }
}

/**
 * 진행 상태 업데이트 요청
 */
data class UpdateProgressRequest(
    val nodeId: String,
    val status: String, // PENDING, DONE, LEARNING, SKIPPED
)

/**
 * 일괄 진행도 업데이트 요청
 */
data class BulkUpdateProgressRequest(
    val updates: List<NodeStatusUpdate>,
)
