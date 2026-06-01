package roomescape.theme.domain;

import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.common.exception.InactiveException;
import roomescape.common.exception.ValidationException;

@Getter
@EqualsAndHashCode
public class Theme {

    private final Long id;
    private final String name;
    private final String thumbnailImageUrl;
    private final String description;
    private final boolean isActive;

    private Theme(Long id, String name, String thumbnailImageUrl, String description, boolean isActive) {
        this.id = id;
        this.name = name;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.description = description;
        this.isActive = isActive;
    }

    public static Theme create(String name, String thumbnailImageUrl, String description) {
        validateRequiredFields(name, thumbnailImageUrl, description);
        return new Theme(null, name, thumbnailImageUrl, description, true);
    }

    public static Theme restore(Long id, String name, String thumbnailImageUrl, String description, boolean isActive) {
        return new Theme(id, name, thumbnailImageUrl, description, isActive);
    }

    public Theme deactivate() {
        return restore(id, name, thumbnailImageUrl, description, false);
    }

    public void validateInactive() {
        if (!isActive()) {
            throw new InactiveException("비활성화 된 테마입니다.");
        }
    }

    private static void validateRequiredFields(String name, String thumbnailImageUrl, String description) {
        validateName(name);
        validateThumbnailImageUrl(thumbnailImageUrl);
        validateDescription(description);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("이름은 필수입니다.");
        }
    }

    private static void validateThumbnailImageUrl(String thumbnailImageUrl) {
        try {
            URI uri = URI.create(thumbnailImageUrl);
            if (uri.getScheme() == null || !uri.getScheme().startsWith("http")) {
                throw new ValidationException("올바른 이미지 주소 형식이 아닙니다.");
            }
        } catch (Exception e) {
            throw new ValidationException("올바른 이미지 주소 형식이 아닙니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new ValidationException("설명은 필수입니다.");
        }
    }
}
