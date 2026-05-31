package roomescape.theme.domain;

import lombok.Builder;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private Theme(Long id, String name, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public static Theme of(String name, String description, String imageUrl) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마 이름은 필수입니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 필수입니다.");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("테마 이미지 URL은 필수입니다.");
        }
        return Theme.builder()
                .id(null)
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .build();
    }

    public static Theme restore(Long id, String name, String description, String imageUrl) {
        return Theme.builder()
                .id(id)
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .build();
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
}
