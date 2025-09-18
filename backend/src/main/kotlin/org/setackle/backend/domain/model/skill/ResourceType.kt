package org.setackle.backend.domain.model.skill

enum class ResourceType(val displayName: String, val description: String) {
    ARTICLE("아티클", "블로그 포스트나 기술 문서"),
    VIDEO("비디오", "동영상 강의나 튜토리얼"),
    COURSE("코스", "체계적인 온라인 강좌"),
    BOOK("도서", "기술 서적이나 전자책"),
    TOOL("도구", "개발 도구나 라이브러리"),
    TUTORIAL("튜토리얼", "튜토리얼"),
    DOCUMENTATION("문서", "공식 문서나 API 레퍼런스")
}