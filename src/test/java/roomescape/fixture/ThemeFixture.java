package roomescape.fixture;

import roomescape.feature.theme.domain.Theme;

public enum ThemeFixture {
    VALID("테마 이름", "테마 설명", "https://example.com/theme.png"),
    VALID_ANOTHER("다른 테마", "다른 설명", "https://example.com/other.png"),
    INVALID_BLANK_NAME("", "테마 설명", "https://example.com/theme.png"),
    INVALID_URL_FORMAT("테마 이름", "테마 설명", "올바르지않은URL");

    private final String name;
    private final String description;
    private final String imageUrl;

    ThemeFixture(String name, String description, String imageUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Theme createInstance() {
        return Theme.create(name, description, imageUrl);
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
