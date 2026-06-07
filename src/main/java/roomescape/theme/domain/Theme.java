package roomescape.theme.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;
import roomescape.global.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = {"name", "description"})
public class Theme {

    @With
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailImgUrl;

    @Builder
    public Theme(Long id, String name, String description, String thumbnailImgUrl) {
        this.id = id;
        this.name = requireName(name);
        this.description = requireDescription(description);
        this.thumbnailImgUrl = requireThumbnailImgUrl(thumbnailImgUrl);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomEscapeException("테마 이름은 비어있을 수 없습니다.");
        }
        return name;
    }

    private static String requireDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new RoomEscapeException("테마 설명은 비어있을 수 없습니다.");
        }
        return description;
    }

    private static String requireThumbnailImgUrl(String thumbnailImgUrl) {
        if (thumbnailImgUrl == null || thumbnailImgUrl.isBlank()) {
            throw new RoomEscapeException("썸네일 이미지 URL은 비어있을 수 없습니다.");
        }
        return thumbnailImgUrl;
    }
}
