package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.Fixtures;
import roomescape.exception.BadRequestException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("예약")
class ReservationTest {

    @DisplayName("예약은 현재보다 지난 날짜로 검증을 시도할 경우 예외가 발생한다")
    @Test
    void validateIsBeforeNow() {
        // given
        Reservation reservation = new Reservation(
                1L,
                LocalDate.now().minusDays(6),
                Fixtures.reservationTimeFixture,
                Fixtures.themeFixture
        );

        // when & then
        assertThatThrownBy(reservation::validateIsBeforeNow)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 지난 날짜는 예약할 수 없습니다.");
    }
}
