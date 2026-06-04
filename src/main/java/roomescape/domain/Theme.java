package roomescape.domain;

import java.net.URI;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailImageUrl;
    private boolean isActive;

    private Theme(Long id, String name, String description, String thumbnailImageUrl, boolean isActive) {
        validateTheme(name, description, thumbnailImageUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.isActive = isActive;
    }

    public static Theme create(String name, String description, String thumbnailImageUrl) {
        return new Theme(null, name, description, thumbnailImageUrl, true);
    }

    public static Theme restore(Long id, String name, String description, String thumbnailImageUrl, boolean isActive) {
        return new Theme(Objects.requireNonNull(id, "복원 시 id 값은 필수입니다"), name, description, thumbnailImageUrl, isActive);
    }

    private static void validateTheme(String name, String description, String thumbnailImageUrl) {
        validateName(name);
        validateDescription(description);
        validateThumbnailImageUrl(thumbnailImageUrl);
    }

    private static void validateThumbnailImageUrl(String thumbnailImageUrl) {
        if (isInvalidImageUrl(thumbnailImageUrl)) {
            throw new RoomEscapeException("올바른 이미지 주소 형식이 아닙니다.");
        }
    }

    private static boolean isInvalidImageUrl(String thumbnailImageUrl) {
        try {
            URI uri = URI.create(thumbnailImageUrl);
            return uri.getScheme() == null || !uri.getScheme().startsWith("http");
        } catch (Exception e) {
            throw new RoomEscapeException("올바른 이미지 주소 형식이 아닙니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new RoomEscapeException("설명은 필수 값입니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomEscapeException("이름은 필수 값입니다.");
        }
    }

    public void deactivate() {
        if (!isActive) {
            throw new RoomEscapeException("이미 비활성화 된 테마입니다.");
        }
        this.isActive = false;
    }

    public void activate() {
        if (isActive) {
            throw new RoomEscapeException("이미 활성화 된 테마입니다.");
        }
        this.isActive = true;
    }
}
