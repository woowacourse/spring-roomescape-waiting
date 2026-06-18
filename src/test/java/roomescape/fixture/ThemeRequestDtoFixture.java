package roomescape.fixture;

import roomescape.theme.web.dto.ThemeRequestDto;

public class ThemeRequestDtoFixture {

    private static final String VALID_NAME = "방탈출테마";
    private static final String VALID_THUMBNAIL_URL = "http://example.com/img.jpg";
    private static final String VALID_DESCRIPTION = "방탈출 테마 설명";
    private static final Long VALID_PRICE = 30000L;

    public static ThemeRequestDto withBlankName() {
        return new ThemeRequestDto("", VALID_THUMBNAIL_URL, VALID_DESCRIPTION, VALID_PRICE);
    }

    public static ThemeRequestDto withNameExceedingMaxLength() {
        return new ThemeRequestDto("a".repeat(41), VALID_THUMBNAIL_URL, VALID_DESCRIPTION, VALID_PRICE);
    }

    public static ThemeRequestDto withInvalidThumbnailUrl() {
        return new ThemeRequestDto(VALID_NAME, "not-a-url", VALID_DESCRIPTION, VALID_PRICE);
    }

    public static ThemeRequestDto withDescriptionExceedingMaxLength() {
        return new ThemeRequestDto(VALID_NAME, VALID_THUMBNAIL_URL, "a".repeat(201), VALID_PRICE);
    }
}
