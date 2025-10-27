package org.setackle.backend.application.skill.outbound

import org.setackle.backend.domain.skill.model.Roadmap
import org.setackle.backend.domain.skill.vo.RoadmapSlug

/**
 * 로드맵 데이터 접근 포트 (출력 포트)
 *
 * Infrastructure 레이어에서 구현되며, 로드맵 데이터의 영속성을 담당합니다.
 */
interface RoadmapPort {
    /**
     * 모든 로드맵 조회
     *
     * @return 로드맵 목록
     */
    fun findAll(): List<Roadmap>

    /**
     * 슬러그로 로드맵 조회
     *
     * @param slug 로드맵 슬러그
     * @return 로드맵 (없으면 null)
     */
    fun findBySlug(slug: RoadmapSlug): Roadmap?

    /**
     * ID로 로드맵 조회
     *
     * @param id 로드맵 ID
     * @return 로드맵 (없으면 null)
     */
    fun findById(id: Long): Roadmap?

    /**
     * 로드맵 저장
     *
     * @param roadmap 저장할 로드맵
     * @return 저장된 로드맵
     */
    fun save(roadmap: Roadmap): Roadmap

    /**
     * 슬러그 중복 확인
     *
     * @param slug 로드맵 슬러그
     * @return 중복 여부
     */
    fun existsBySlug(slug: RoadmapSlug): Boolean

    /**
     * 로드맵 삭제
     *
     * @param id 로드맵 ID
     */
    fun deleteById(id: Long)
}
