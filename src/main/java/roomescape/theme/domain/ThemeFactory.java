package roomescape.theme.domain;

import org.springframework.stereotype.Component;

@Component
public class ThemeFactory {

    public Theme create(String name, String description, String imageUrl) {
        validate(name, description, imageUrl);
        return Theme.restore(null, name, description, imageUrl);
    }

    private void validate(String name, String description, String imageUrl) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마 이름은 필수입니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 필수입니다.");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("테마 이미지 URL은 필수입니다.");
        }
    }
}
