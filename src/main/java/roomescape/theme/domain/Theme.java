package roomescape.theme.domain;

import java.util.Objects;
import org.springframework.util.StringUtils;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;

    private Theme(Long id, String name, String description, String imageUrl) {
        validateName(name);
        validateDescription(description);
        validateImageUrl(imageUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public static Theme of(Long id, String name, String description, String imageUrl) {
        return new Theme(id, name, description, imageUrl);
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("테마엔 이름이 존재해야 합니다.");
        }
    }

    private void validateDescription(String description) {
        if (!StringUtils.hasText(description)) {
            throw new IllegalArgumentException("테마엔 설명이 존재해야 합니다.");
        }
    }

    private void validateImageUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            throw new IllegalArgumentException("테마엔 이미지가 존재해야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id) && Objects.equals(name, theme.name)
                && Objects.equals(description, theme.description) && Objects.equals(imageUrl,
                theme.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, imageUrl);
    }
}
