package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.theme.exception.ThemeErrorInformation.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.theme.exception.ThemeException;

class ThemeTest {
    private final String name = "공포";
    private final String description = "테마 설명";
    private final String defaultThumbnailUrl = "DEFAULT_THUMBNAIL_URL";
    private final Long amount = 1000L;

    @Test
    @DisplayName("유효한 값으로 테마를 등록할 수 있다.")
    void create_with_valid_field() {
        // when & then
        Assertions.assertThatCode(() -> Theme.create(name, description, defaultThumbnailUrl, amount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효한 값으로 테마를 등록하면 입력한 값을 그대로 유지한다.")
    void create_compare_fields() {
        // when
        Theme theme = Theme.create(name, description, defaultThumbnailUrl, amount);

        // then
        Assertions.assertThat(theme)
                .returns(null, Theme::getId)
                .returns(name, Theme::getName)
                .returns(description, Theme::getDescription)
                .returns(true, Theme::isActive)
                .returns(amount, Theme::getAmount);
    }

    @Test
    @DisplayName("DB로부터 유효한 값을 가져오면 테마를 생성할 수 있다.")
    void load() {
        // given
        Long loadValidId = 1L;
        boolean loadStatus = false;

        // when
        Assertions.assertThatCode(() -> Theme.load(loadValidId, name, description, description, loadStatus, amount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("DB에서 로드할 때, id가 null이면 예외가 발생한다.")
    void load_null_id() {
        // given
        Long nullId = null;
        boolean loadStatus = false;

        // when
        Assertions.assertThatThrownBy(() -> Theme.load(nullId, name, description, description, loadStatus, amount))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ID_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("등록, 로드한 테마는 id와 활성화상태를 제외한 모든 필드가 일치한다.")
    void creat_compare_load() {
        // given
        Theme createdTheme = Theme.create(name, description, description, amount);
        Theme loadedTheme = Theme.load(1L, name, description, description, true, amount);

        // when & then
        Assertions.assertThat(createdTheme)
                .usingRecursiveComparison()
                .ignoringFields("id", "isActive")
                .isEqualTo(loadedTheme);
    }

    @Test
    @DisplayName("테마 이름이 null이면 예외가 발생한다.")
    void create_null_name() {
        // given
        String nullName = null;

        // when & then
        assertThatThrownBy(() -> Theme.create(nullName, description, defaultThumbnailUrl, amount))
                .isInstanceOf(ThemeException.class)
                .hasMessage(NAME_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("테마 이름이 비어있으면 예외가 발생한다.")
    void create_empty_name() {
        // given
        String emptyName = "";

        // when & then
        assertThatThrownBy(() -> Theme.create(emptyName, description, defaultThumbnailUrl, amount))
                .isInstanceOf(ThemeException.class)
                .hasMessage(NAME_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("테마 설명이 null이면 예외가 발생한다.")
    void create_null_description() {
        // given
        String nullDescription = null;

        // when & then
        assertThatThrownBy(() -> Theme.create(name, nullDescription, defaultThumbnailUrl, amount))
                .isInstanceOf(ThemeException.class)
                .hasMessage(DESCRIPTION_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("테마 설명이 비어있으면 예외가 발생한다.")
    void create_empty_description() {
        // given
        String emptyDescription = "";

        // when & then
        assertThatThrownBy(() -> Theme.create(name, emptyDescription, defaultThumbnailUrl, amount))
                .isInstanceOf(ThemeException.class)
                .hasMessage(DESCRIPTION_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("테마 썸네일 URL이 null이면 예외가 발생한다.")
    void create_null_thumbnailUrl() {
        // given
        String nullThumbnailUrl = null;

        // when & then
        assertThatThrownBy(() -> Theme.create(name, description, nullThumbnailUrl, amount))
                .isInstanceOf(ThemeException.class)
                .hasMessage(THUMBNAIL_URL_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("테마 썸네일 URL이 비어있으면 디폴트 썸네일로 변환된다.")
    void create_empty_thumbnailUrl() {
        // given
        String emptyThumbnailUrl = "";

        // when
        Theme theme = Theme.create(name, description, emptyThumbnailUrl, amount);

        //then
        assertThat(defaultThumbnailUrl)
                .isEqualTo(theme.getThumbnailUrl());
    }

    @Test
    @DisplayName("테마 가격이 null이면 예외가 발생한다.")
    void create_null_amount() {
        // given
        Long nullAmount = null;

        // when & then
        assertThatThrownBy(() -> Theme.create(name, description, defaultThumbnailUrl, nullAmount))
                .isInstanceOf(ThemeException.class)
                .hasMessage(AMOUNT_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("테마 가격이 음수이면 예외가 발생한다.")
    void create_negative_amount() {
        // given
        Long negativeAmount = -1L;

        // when & then
        assertThatThrownBy(() -> Theme.create(name, description, defaultThumbnailUrl, negativeAmount))
                .isInstanceOf(ThemeException.class)
                .hasMessage(AMOUNT_IS_NEGATIVE.getMessage());
    }

    @Test
    @DisplayName("테마 가격이 0이면 정상적으로 등록된다.")
    void create_zero_amount() {
        // given
        Long zeroAmount = 0L;

        // when & then
        Assertions.assertThatCode(() -> Theme.create(name, description, defaultThumbnailUrl, zeroAmount))
                .doesNotThrowAnyException();
    }
}
