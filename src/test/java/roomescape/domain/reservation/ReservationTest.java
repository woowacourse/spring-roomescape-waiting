package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.Fixture.DATE_1;
import static roomescape.fixture.Fixture.MEMBER_1;
import static roomescape.fixture.Fixture.RESERVATION_DETAIL_1;
import static roomescape.fixture.Fixture.RESERVATION_TIME_1;
import static roomescape.fixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainValidationException;
import roomescape.domain.reservation.detail.ReservationDetail;

class ReservationTest {
    @Test
    @DisplayName("예약을 생성한다.")
    void create() {
        assertThatCode(() -> new Reservation(RESERVATION_DETAIL_1, MEMBER_1))
                .doesNotThrowAnyException();
    }


    @Test
    @DisplayName("예약 상세가 없으면 예외가 발생한다.")
    void validateTime() {
        assertThatThrownBy(() -> new Reservation(null, MEMBER_1))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("예약 상세는 필수 값입니다.");
    }

    @Test
    @DisplayName("create 메서드로 예약을 생성한다.")
    void createReservation() {
        LocalDate reservationTime = LocalDate.of(2024, 5, 5);
        LocalDateTime now = DATE_1.minusDays(1).atTime(10, 0);

        ReservationDetail detail = new ReservationDetail(reservationTime, RESERVATION_TIME_1, THEME_1);
        Reservation reservation = Reservation.create(now, detail, MEMBER_1);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservation.getDetail()).isEqualTo(detail);
            softly.assertThat(reservation.getMember()).isEqualTo(MEMBER_1);
        });
    }

    @Test
    @DisplayName("create 메서드로 예약을 생성할 때 지나간 날짜/시간이면 예외가 발생한다.")
    void createReservationWhenPastDateTime() {
        LocalDate reservationTime = LocalDate.of(2024, 5, 5);
        LocalDateTime currentDateTime = LocalDate.of(2024, 5, 6).atTime(10, 0);

        ReservationDetail detail = new ReservationDetail(reservationTime, RESERVATION_TIME_1, THEME_1);

        assertThatThrownBy(() -> Reservation.create(currentDateTime, detail, MEMBER_1))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage(String.format("지나간 날짜/시간에 대한 예약은 불가능합니다. (예약 날짜: %s, 예약 시간: %s)",
                        reservationTime, RESERVATION_TIME_1.getStartAt()));
    }
}
