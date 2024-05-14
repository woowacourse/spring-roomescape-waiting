package roomescape.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidReservationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThumbnailTest {
    @DisplayName("올바르지 않은 썸네일 형식이면 예외를 발생시킨다.")
    @Test
    void invalidThumbnail() {
        assertThatThrownBy(() -> new Thumbnail("http.jpg"))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("올바르지 않은 썸네일 형식입니다.");
    }
}
