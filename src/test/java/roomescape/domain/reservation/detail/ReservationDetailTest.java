package roomescape.domain.reservation.detail;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.Fixture.DATE_1;
import static roomescape.fixture.Fixture.RESERVATION_TIME_1;
import static roomescape.fixture.Fixture.THEME_1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainValidationException;

class ReservationDetailTest {

    @Test
    @DisplayName("예약 상세를 생성한다.")
    void create() {
        assertThatCode(() -> new ReservationDetail(DATE_1, RESERVATION_TIME_1, THEME_1))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("날짜가 없으면 예외가 발생한다.")
    void validateDate() {
        assertThatThrownBy(() -> new ReservationDetail(null, RESERVATION_TIME_1, THEME_1))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("예약 날짜는 필수 값입니다.");
    }

    @Test
    @DisplayName("예약 시간이 없으면 예외가 발생한다.")
    void validateTime() {
        assertThatThrownBy(() -> new ReservationDetail(DATE_1, null, THEME_1))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("예약 시간은 필수 값입니다.");
    }

    @Test
    @DisplayName("테마가 없으면 예외가 발생한다.")
    void validateTheme() {
        assertThatThrownBy(() -> new ReservationDetail(DATE_1, RESERVATION_TIME_1, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("테마는 필수 값입니다.");
    }
}
