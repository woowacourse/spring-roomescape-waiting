package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ThemeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void 테마_이름이_비어있으면_예외(String name) {
        assertThatThrownBy(() -> new Theme(null, name, "설명", "https://thumbnail.url"))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void 테마_설명이_비어있으면_예외(String description) {
        assertThatThrownBy(() -> new Theme(null, "테마", description, "https://thumbnail.url"))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void 테마_썸네일_URL이_비어있으면_예외(String thumbnailImageUrl) {
        assertThatThrownBy(() -> new Theme(null, "테마", "설명", thumbnailImageUrl))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }
}