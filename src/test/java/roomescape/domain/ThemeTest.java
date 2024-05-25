package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.exception.ExceptionType.EMPTY_DESCRIPTION;
import static roomescape.exception.ExceptionType.EMPTY_NAME;
import static roomescape.exception.ExceptionType.EMPTY_THUMBNAIL;
import static roomescape.exception.ExceptionType.NOT_URL_BASE_THUMBNAIL;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.RoomescapeException;

class ThemeTest {

    @Test
    @DisplayName("테마 이름이 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyName() {
        assertAll(
                () -> Assertions.assertThatThrownBy(() -> new Theme(null, "description", "http://thumbnail"))
                        .isInstanceOf(RoomescapeException.class)
                        .hasMessage(EMPTY_NAME.getMessage()),
                () -> Assertions.assertThatThrownBy(() -> new Theme("", "description", "http://thumbnail"))
                        .isInstanceOf(RoomescapeException.class)
                        .hasMessage(EMPTY_NAME.getMessage())
        );
    }

    @Test
    @DisplayName("테마 설명이 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyDescription() {
        assertAll(
                () -> Assertions.assertThatThrownBy(() -> new Theme("name", null, "http://thumbnail"))
                        .isInstanceOf(RoomescapeException.class)
                        .hasMessage(EMPTY_DESCRIPTION.getMessage()),
                () -> Assertions.assertThatThrownBy(() -> new Theme("name", "", "http://thumbnail"))
                        .isInstanceOf(RoomescapeException.class)
                        .hasMessage(EMPTY_DESCRIPTION.getMessage())
        );
    }

    @Test
    @DisplayName("썸네일이 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyThumbnail() {
        assertAll(
                () -> Assertions.assertThatThrownBy(() -> new Theme("name", "description", ""))
                        .isInstanceOf(RoomescapeException.class)
                        .hasMessage(EMPTY_THUMBNAIL.getMessage()),
                () -> Assertions.assertThatThrownBy(() -> new Theme("name", "description", null))
                        .isInstanceOf(RoomescapeException.class)
                        .hasMessage(EMPTY_THUMBNAIL.getMessage())
        );
    }

    @Test
    @DisplayName("썸네일이 url 이 아닐경우 생성할 수 없는지 확인")
    void createFailWhenNotUrlThumbnail() {
        Assertions.assertThatThrownBy(() -> new Theme("name", "description", "notUrl"))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(NOT_URL_BASE_THUMBNAIL.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://thumbnail", "https://thumbnail"})
    @DisplayName("썸네일이 url인 경우 생성할 수 있는지 확인")
    void createSuccessWhenUrlThumbnail(String thumbnail) {
        assertDoesNotThrow(() -> new Theme("name", "description", thumbnail));
    }
}
