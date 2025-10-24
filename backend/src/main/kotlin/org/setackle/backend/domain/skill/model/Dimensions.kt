package org.setackle.backend.domain.skill.model

/**
 * 로드맵 캔버스 크기
 *
 * Dimensions 모델
 * - 로드맵 시각화 캔버스의 너비와 높이
 * - @xyflow/react의 viewport 크기 정의
 *
 * @property width 캔버스 너비 (픽셀)
 * @property height 캔버스 높이 (픽셀)
 */
data class Dimensions(
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0) { "너비는 0보다 커야 합니다: $width" }
        require(height > 0) { "높이는 0보다 커야 합니다: $height" }
        require(width <= MAX_WIDTH) { "너비는 ${MAX_WIDTH}px를 초과할 수 없습니다: $width" }
        require(height <= MAX_HEIGHT) { "높이는 ${MAX_HEIGHT}px를 초과할 수 없습니다: $height" }
    }

    /**
     * 가로세로 비율 계산
     */
    fun aspectRatio(): Double = width.toDouble() / height.toDouble()

    /**
     * 캔버스 면적 계산
     */
    fun area(): Int = width * height

    /**
     * 가로 방향 여부
     */
    fun isLandscape(): Boolean = width > height

    /**
     * 세로 방향 여부
     */
    fun isPortrait(): Boolean = height > width

    /**
     * 정사각형 여부
     */
    fun isSquare(): Boolean = width == height

    /**
     * 크기 조정 (비율 유지)
     */
    fun scale(factor: Double): Dimensions {
        require(factor > 0) { "배율은 0보다 커야 합니다: $factor" }
        val newWidth = (width * factor).toInt().coerceIn(MIN_WIDTH, MAX_WIDTH)
        val newHeight = (height * factor).toInt().coerceIn(MIN_HEIGHT, MAX_HEIGHT)
        return Dimensions(newWidth, newHeight)
    }

    /**
     * 너비 조정 (비율 유지)
     */
    fun withWidth(newWidth: Int): Dimensions {
        require(newWidth > 0) { "너비는 0보다 커야 합니다: $newWidth" }
        val ratio = aspectRatio()
        val newHeight = (newWidth / ratio).toInt()
        return Dimensions(newWidth, newHeight)
    }

    /**
     * 높이 조정 (비율 유지)
     */
    fun withHeight(newHeight: Int): Dimensions {
        require(newHeight > 0) { "높이는 0보다 커야 합니다: $newHeight" }
        val ratio = aspectRatio()
        val newWidth = (newHeight * ratio).toInt()
        return Dimensions(newWidth, newHeight)
    }

    companion object {
        // 최소/최대 크기 제약
        const val MIN_WIDTH = 600
        const val MIN_HEIGHT = 400
        const val MAX_WIDTH = 10000
        const val MAX_HEIGHT = 10000

        // 기본 크기
        val DEFAULT = Dimensions(1200, 800)
        val SMALL = Dimensions(800, 600)
        val MEDIUM = Dimensions(1200, 800)
        val LARGE = Dimensions(1600, 1000)
        val EXTRA_LARGE = Dimensions(2400, 1600)

        // 일반적인 비율
        val RATIO_16_9 = Dimensions(1600, 900)
        val RATIO_4_3 = Dimensions(1200, 900)
        val RATIO_3_2 = Dimensions(1200, 800)
        val SQUARE = Dimensions(1000, 1000)

        /**
         * 안전한 생성 메서드 (범위 검증 후 기본값 반환)
         */
        fun ofOrDefault(width: Int, height: Int): Dimensions {
            return try {
                Dimensions(width, height)
            } catch (e: IllegalArgumentException) {
                DEFAULT
            }
        }

        /**
         * 최소 크기 적용
         */
        fun withMinimum(width: Int, height: Int): Dimensions {
            return Dimensions(
                width.coerceAtLeast(MIN_WIDTH),
                height.coerceAtLeast(MIN_HEIGHT),
            )
        }
    }

    override fun toString(): String = "$width x $height"
}
