package roomescape.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.ReservationTestFixture;
import roomescape.reservation.model.dto.ReservationDetails;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;

class ReservationTest {

    @DisplayName("예약을 생성할 수 있다")
    @Test
    void createFutureSuccess() {
        //given
        ReservationDetails details = new ReservationDetails(
                LocalDate.now().plusDays(10),
                new ReservationTime(LocalTime.now().plusHours(1)),
                new ReservationTheme("테마 이름", "테마 설명", "테마 url"),
                ReservationTestFixture.getUserFixture()
                );

        //when
        Reservation reservation = Reservation.createFuture(details);

        //then
        assertThat(reservation.getDate()).isEqualTo(details.date());
        assertThat(reservation.getMember().getName()).isEqualTo(details.member().getName());
    }

    @DisplayName("예약 생성시 예약 시간이 과거 시간이면 예외를 발생시킨다")
    @Test
    void createFutureExceptionIfPastGetTime() {
        //given
        ReservationDetails details = new ReservationDetails(
                LocalDate.now().minusDays(1),
                new ReservationTime(LocalTime.of(10, 0)),
                new ReservationTheme("테마 이름", "테마 설명", "테마 url"),
                ReservationTestFixture.getUserFixture()
        );

        //when & then
        assertThatThrownBy(() -> Reservation.createFuture(details))
                .isInstanceOf(InvalidReservationTimeException.class);
    }
}
