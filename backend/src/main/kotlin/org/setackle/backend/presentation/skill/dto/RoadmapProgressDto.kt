package org.setackle.backend.presentation.skill.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.setackle.backend.application.user.inbound.*

/**
 * 진행 상태 업데이트 요청 DTO
 */
@Schema(description = "진행 상태 업데이트 요청")
data class UpdateProgressRequest(
    @field:NotBlank(message = "노드 ID는 필수입니다.")
    @Schema(
        description = "업데이트할 노드 ID",
        example = "react-basics",
        required = true,
    )
    val nodeId: String,

    @field:NotBlank(message = "상태는 필수입니다.")
    @field:Pattern(
        regexp = "^(PENDING|DONE|LEARNING|SKIPPED)$",
        message = "유효하지 않은 상태입니다. PENDING, DONE, LEARNING, SKIPPED 중 하나여야 합니다.",
    )
    @Schema(
        description = "진행 상태",
        example = "DONE",
        allowableValues = ["PENDING", "DONE", "LEARNING", "SKIPPED"],
        required = true,
    )
    val status: String,
)

/**
 * UpdateProgressRequest -> UpdateProgressCommand 변환
 */
fun UpdateProgressRequest.toCommand(userId: Long, roadmapSlug: String): UpdateProgressCommand {
    return UpdateProgressCommand(
        userId = userId,
        roadmapSlug = roadmapSlug,
        nodeId = this.nodeId,
        status = this.status,
    )
}

/**
 * 진행 상태 업데이트 응답 DTO
 */
@Schema(description = "진행 상태 업데이트 응답")
data class UpdateProgressResponse(
    @Schema(description = "사용자 ID", example = "123")
    val userId: Long,

    @Schema(description = "로드맵 ID", example = "1")
    val roadmapId: Long,

    @Schema(description = "노드 ID", example = "react-basics")
    val nodeId: String,

    @Schema(description = "이전 상태", example = "LEARNING")
    val previousStatus: String,

    @Schema(description = "현재 상태", example = "DONE")
    val currentStatus: String,

    @Schema(description = "완료한 노드 ID 목록")
    val done: Set<String>,

    @Schema(description = "학습 중인 노드 ID 목록")
    val learning: Set<String>,

    @Schema(description = "건너뛴 노드 ID 목록")
    val skipped: Set<String>,

    @Schema(description = "전체 진행률 (%)", example = "75.5")
    val progressPercent: Double,
) {
    companion object {
        /**
         * UpdateProgressResult -> UpdateProgressResponse 변환
         */
        fun from(result: UpdateProgressResult): UpdateProgressResponse {
            return UpdateProgressResponse(
                userId = result.userId,
                roadmapId = result.roadmapId,
                nodeId = result.nodeId,
                previousStatus = result.previousStatus,
                currentStatus = result.currentStatus,
                done = result.done,
                learning = result.learning,
                skipped = result.skipped,
                progressPercent = result.progressPercent,
            )
        }
    }
}

/**
 * 일괄 진행도 업데이트 요청 DTO
 */
@Schema(description = "일괄 진행도 업데이트 요청")
data class BulkUpdateProgressRequest(
    @field:NotEmpty(message = "업데이트 목록은 비어있을 수 없습니다.")
    @field:Size(max = 100, message = "한 번에 최대 100개까지 업데이트할 수 있습니다.")
    @field:Valid
    @Schema(description = "업데이트할 노드 목록 (최대 100개)", required = true)
    val updates: List<NodeStatusUpdateDto>,
)

/**
 * BulkUpdateProgressRequest -> BulkUpdateProgressCommand 변환
 */
fun BulkUpdateProgressRequest.toCommand(userId: Long, roadmapSlug: String): BulkUpdateProgressCommand {
    return BulkUpdateProgressCommand(
        userId = userId,
        roadmapSlug = roadmapSlug,
        updates = this.updates.map { NodeStatusUpdate(it.nodeId, it.status) },
    )
}

/**
 * 노드 상태 업데이트 항목 DTO
 */
@Schema(description = "노드 상태 업데이트 항목")
data class NodeStatusUpdateDto(
    @field:NotBlank(message = "노드 ID는 필수입니다.")
    @Schema(description = "노드 ID", example = "react-basics", required = true)
    val nodeId: String,

    @field:NotBlank(message = "상태는 필수입니다.")
    @field:Pattern(
        regexp = "^(PENDING|DONE|LEARNING|SKIPPED)$",
        message = "유효하지 않은 상태입니다. PENDING, DONE, LEARNING, SKIPPED 중 하나여야 합니다.",
    )
    @Schema(
        description = "진행 상태",
        example = "DONE",
        allowableValues = ["PENDING", "DONE", "LEARNING", "SKIPPED"],
        required = true,
    )
    val status: String,
)

/**
 * 일괄 진행도 업데이트 응답 DTO
 */
@Schema(description = "일괄 진행도 업데이트 응답")
data class BulkUpdateProgressResponse(
    @Schema(description = "사용자 ID", example = "123")
    val userId: Long,

    @Schema(description = "로드맵 ID", example = "1")
    val roadmapId: Long,

    @Schema(description = "총 업데이트 시도 개수", example = "10")
    val totalUpdated: Int,

    @Schema(description = "성공한 업데이트 개수", example = "9")
    val successCount: Int,

    @Schema(description = "실패한 업데이트 개수", example = "1")
    val failedCount: Int,

    @Schema(description = "완료한 노드 ID 목록")
    val done: Set<String>,

    @Schema(description = "학습 중인 노드 ID 목록")
    val learning: Set<String>,

    @Schema(description = "건너뛴 노드 ID 목록")
    val skipped: Set<String>,

    @Schema(description = "전체 진행률 (%)", example = "80.0")
    val progressPercent: Double,

    @Schema(description = "에러 목록 (실패한 업데이트에 대한 상세 정보)")
    val errors: List<BulkUpdateErrorDto>?,
) {
    companion object {
        /**
         * BulkUpdateProgressResult -> BulkUpdateProgressResponse 변환
         */
        fun from(result: BulkUpdateProgressResult): BulkUpdateProgressResponse {
            return BulkUpdateProgressResponse(
                userId = result.userId,
                roadmapId = result.roadmapId,
                totalUpdated = result.totalUpdated,
                successCount = result.successCount,
                failedCount = result.failedCount,
                done = result.done,
                learning = result.learning,
                skipped = result.skipped,
                progressPercent = result.progressPercent,
                errors = result.errors?.map { BulkUpdateErrorDto(it.nodeId, it.reason) },
            )
        }
    }
}

/**
 * 일괄 업데이트 오류 항목 DTO
 */
@Schema(description = "일괄 업데이트 오류 항목")
data class BulkUpdateErrorDto(
    @Schema(description = "실패한 노드 ID", example = "invalid-node")
    val nodeId: String,

    @Schema(description = "실패 이유", example = "노드를 찾을 수 없습니다")
    val reason: String,
)

/**
 * 사용자 진행 상황 응답 DTO
 */
@Schema(description = "사용자 진행 상황 응답")
data class UserProgressResponse(
    @Schema(description = "사용자 ID", example = "123")
    val userId: Long,

    @Schema(description = "로드맵 ID", example = "1")
    val roadmapId: Long,

    @Schema(description = "로드맵 이름", example = "프론트엔드 개발자")
    val roadmapName: String,

    @Schema(description = "로드맵 슬러그", example = "frontend-development")
    val roadmapSlug: String,

    @Schema(description = "완료한 노드 ID 목록")
    val done: Set<String>,

    @Schema(description = "학습 중인 노드 ID 목록")
    val learning: Set<String>,

    @Schema(description = "건너뛴 노드 ID 목록")
    val skipped: Set<String>,

    @Schema(description = "즐겨찾기 여부", example = "true")
    val isFavorite: Boolean,

    @Schema(description = "전체 노드 개수", example = "50")
    val totalNodes: Int,

    @Schema(description = "완료한 노드 개수", example = "35")
    val doneCount: Int,

    @Schema(description = "학습 중인 노드 개수", example = "10")
    val learningCount: Int,

    @Schema(description = "건너뛴 노드 개수", example = "5")
    val skippedCount: Int,

    @Schema(description = "전체 진행률 (%)", example = "70.0")
    val progressPercent: Double,
) {
    companion object {
        /**
         * UserProgressResult -> UserProgressResponse 변환
         */
        fun from(result: UserProgressResult): UserProgressResponse {
            return UserProgressResponse(
                userId = result.userId,
                roadmapId = result.roadmapId,
                roadmapName = result.roadmapName,
                roadmapSlug = result.roadmapSlug,
                done = result.done,
                learning = result.learning,
                skipped = result.skipped,
                isFavorite = result.isFavorite,
                totalNodes = result.totalNodes,
                doneCount = result.doneCount,
                learningCount = result.learningCount,
                skippedCount = result.skippedCount,
                progressPercent = result.progressPercent,
            )
        }
    }
}

/**
 * 사용자 로드맵 요약 응답 DTO
 */
@Schema(description = "사용자 로드맵 요약 응답")
data class UserRoadmapSummaryResponse(
    @Schema(description = "로드맵 ID", example = "1")
    val roadmapId: Long,

    @Schema(description = "로드맵 이름", example = "프론트엔드 개발자")
    val roadmapName: String,

    @Schema(description = "로드맵 슬러그", example = "frontend-development")
    val roadmapSlug: String,

    @Schema(description = "전체 진행률 (%)", example = "70.0")
    val progressPercent: Double,

    @Schema(description = "완료한 노드 개수", example = "35")
    val doneCount: Int,

    @Schema(description = "전체 노드 개수", example = "50")
    val totalNodes: Int,

    @Schema(description = "즐겨찾기 여부", example = "true")
    val isFavorite: Boolean,

    @Schema(description = "마지막 업데이트 시간 (ISO-8601)", example = "2025-01-22T10:30:00Z")
    val lastUpdated: String,
) {
    companion object {
        /**
         * UserRoadmapSummary -> UserRoadmapSummaryResponse 변환
         */
        fun from(summary: UserRoadmapSummary): UserRoadmapSummaryResponse {
            return UserRoadmapSummaryResponse(
                roadmapId = summary.roadmapId,
                roadmapName = summary.roadmapName,
                roadmapSlug = summary.roadmapSlug,
                progressPercent = summary.progressPercent,
                doneCount = summary.doneCount,
                totalNodes = summary.totalNodes,
                isFavorite = summary.isFavorite,
                lastUpdated = summary.lastUpdated,
            )
        }

        /**
         * List<UserRoadmapSummary> -> List<UserRoadmapSummaryResponse> 변환
         */
        fun fromList(summaries: List<UserRoadmapSummary>): List<UserRoadmapSummaryResponse> {
            return summaries.map { from(it) }
        }
    }
}

/**
 * 진행 상황 초기화 응답 DTO
 */
@Schema(description = "진행 상황 초기화 응답")
data class ResetProgressResponse(
    @Schema(description = "사용자 ID", example = "123")
    val userId: Long,

    @Schema(description = "로드맵 ID", example = "1")
    val roadmapId: Long,

    @Schema(description = "메시지", example = "진행 상황이 초기화되었습니다.")
    val message: String,
) {
    companion object {
        /**
         * ResetProgressResult -> ResetProgressResponse 변환
         */
        fun from(result: ResetProgressResult): ResetProgressResponse {
            return ResetProgressResponse(
                userId = result.userId,
                roadmapId = result.roadmapId,
                message = result.message,
            )
        }
    }
}

/**
 * 즐겨찾기 토글 응답 DTO
 */
@Schema(description = "즐겨찾기 토글 응답")
data class ToggleFavoriteResponse(
    @Schema(description = "사용자 ID", example = "123")
    val userId: Long,

    @Schema(description = "로드맵 ID", example = "1")
    val roadmapId: Long,

    @Schema(description = "즐겨찾기 여부", example = "true")
    val isFavorite: Boolean,
) {
    companion object {
        /**
         * ToggleFavoriteResult -> ToggleFavoriteResponse 변환
         */
        fun from(result: ToggleFavoriteResult): ToggleFavoriteResponse {
            return ToggleFavoriteResponse(
                userId = result.userId,
                roadmapId = result.roadmapId,
                isFavorite = result.isFavorite,
            )
        }
    }
}
