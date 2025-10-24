package org.setackle.backend.domain.skill.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @xyflow/react 호환 노드
 *
 * FlowNode 도메인 모델
 * - @xyflow/react의 Node 타입과 호환
 * - 로드맵 시각화를 위한 노드 정보 포함
 *
 * @property id 노드 고유 ID
 * @property type 노드 타입 (topic, subtopic, button, link 등)
 * @property position 노드 위치 (x, y 좌표)
 * @property data 노드에 표시될 데이터
 * @property style 추가 스타일 (선택적)
 * @property className CSS 클래스명 (선택적)
 */
data class FlowNode(
    val id: String,
    val type: String,
    val position: Position,
    val data: NodeData,
    val style: Map<String, Any>? = null,
    val className: String? = null,
) {
    init {
        require(id.isNotBlank()) { "노드 ID는 비어있을 수 없습니다" }
        require(type.isNotBlank()) { "노드 타입은 비어있을 수 없습니다" }
    }

    /**
     * 노드 복사 (위치 변경용)
     */
    fun withPosition(newPosition: Position): FlowNode {
        return copy(position = newPosition)
    }

    /**
     * 노드 복사 (데이터 변경용)
     */
    fun withData(newData: NodeData): FlowNode {
        return copy(data = newData)
    }
}

/**
 * 노드 위치 (x, y 좌표)
 *
 * @property x X 좌표 (픽셀)
 * @property y Y 좌표 (픽셀)
 */
data class Position(
    val x: Double,
    val y: Double,
) {
    init {
        require(x.isFinite()) { "X 좌표는 유효한 숫자여야 합니다" }
        require(y.isFinite()) { "Y 좌표는 유효한 숫자여야 합니다" }
    }

    /**
     * 위치 이동
     */
    fun move(dx: Double, dy: Double): Position {
        return Position(x + dx, y + dy)
    }

    companion object {
        val ORIGIN = Position(0.0, 0.0)
    }
}

/**
 * 노드 데이터
 *
 * @property label 노드 레이블 (표시될 텍스트)
 * @property description 노드 설명 (선택적)
 * @property nodeId 원본 노드 ID (legacy 호환용, 선택적)
 * @property resources 학습 리소스 목록 (선택적)
 * @property questionCount 관련 질문 개수 (선택적, 기본값 0)
 */
data class NodeData(
    val label: String,
    val description: String? = null,
    @JsonProperty("nodeId")
    val nodeId: String? = null,
    val resources: List<NodeResource>? = null,
    val questionCount: Int? = 0,
) {
    init {
        require(label.isNotBlank()) { "노드 레이블은 비어있을 수 없습니다" }
        if (questionCount != null) {
            require(questionCount >= 0) { "질문 개수는 0 이상이어야 합니다" }
        }
    }

    /**
     * 리소스가 있는지 확인
     */
    fun hasResources(): Boolean = !resources.isNullOrEmpty()

    /**
     * 질문이 있는지 확인
     */
    fun hasQuestions(): Boolean = (questionCount ?: 0) > 0
}

/**
 * 노드 리소스 (학습 자료)
 *
 * @property type 리소스 타입 (article, video, course, book)
 * @property title 리소스 제목
 * @property url 리소스 URL
 * @property description 리소스 설명 (선택적)
 * @property language 언어 코드 (기본값: ko)
 */
data class NodeResource(
    val type: String,
    val title: String,
    val url: String,
    val description: String? = null,
    val language: String = "ko",
) {
    init {
        require(type.isNotBlank()) { "리소스 타입은 비어있을 수 없습니다" }
        require(title.isNotBlank()) { "리소스 제목은 비어있을 수 없습니다" }
        require(url.isNotBlank()) { "리소스 URL은 비어있을 수 없습니다" }
        require(language.isNotBlank()) { "언어 코드는 비어있을 수 없습니다" }
    }

    /**
     * 리소스 타입 검증
     */
    fun isValidType(): Boolean {
        return type in listOf("article", "video", "course", "book", "documentation", "tutorial")
    }

    companion object {
        /**
         * 아티클 리소스 생성
         */
        fun article(title: String, url: String, description: String? = null, language: String = "ko"): NodeResource {
            return NodeResource("article", title, url, description, language)
        }

        /**
         * 비디오 리소스 생성
         */
        fun video(title: String, url: String, description: String? = null, language: String = "ko"): NodeResource {
            return NodeResource("video", title, url, description, language)
        }

        /**
         * 강의 리소스 생성
         */
        fun course(title: String, url: String, description: String? = null, language: String = "ko"): NodeResource {
            return NodeResource("course", title, url, description, language)
        }

        /**
         * 책 리소스 생성
         */
        fun book(title: String, url: String, description: String? = null, language: String = "ko"): NodeResource {
            return NodeResource("book", title, url, description, language)
        }
    }
}
