package roomescape.theme.dto.request;

public record ThemeRequest(String name, String description, String thumbnail) {
    public ThemeRequest {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("테마 이름이 비어있을 수 없습니다.");
        }

        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("테마 설명이 비어있을 수 없습니다.");
        }

        if (thumbnail == null || thumbnail.isEmpty()) {
            throw new IllegalArgumentException("테마 썸네일이 비어있을 수 없습니다.");
        }
    }
}
