package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ThemeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("테마 이름이 비어있으면 예외")
    void throwsExceptionWhenNameIsBlank(String name) {
        assertThatThrownBy(() -> new Theme(null, name, "설명", "https://thumbnail.url"))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("테마 설명이 비어있으면 예외")
    void throwsExceptionWhenDescriptionIsBlank(String description) {
        assertThatThrownBy(() -> new Theme(null, "테마", description, "https://thumbnail.url"))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("테마 썸네일 URL이 비어있으면 예외")
    void throwsExceptionWhenThumbnailUrlIsBlank(String thumbnailImageUrl) {
        assertThatThrownBy(() -> new Theme(null, "테마", "설명", thumbnailImageUrl))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }
}
