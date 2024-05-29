package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.BasicAcceptanceTest;
import roomescape.dto.ReservationRequest;
import roomescape.exception.RoomescapeException;

class ReservationFactoryTest extends BasicAcceptanceTest {
    @Autowired
    private ReservationFactory reservationFactory;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("존재하지 않는 예약 시간으로 예약을 생성시 예외를 반환한다.")
    @Test
    void shouldReturnIllegalArgumentExceptionWhenNotFoundReservationTime() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2024, 1, 1), 99L, 1L);

        assertThatThrownBy(() -> reservationFactory.create(1L, reservationRequest.date(),
                reservationRequest.timeId(), reservationRequest.themeId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약을 생성시 예외를 반환한다.")
    void shouldThrowIllegalArgumentExceptionWhenNotFoundTheme() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2024, 1, 1), 1L, 99L);

        assertThatThrownBy(() -> reservationFactory.create(1L, reservationRequest.date(),
                reservationRequest.timeId(), reservationRequest.themeId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }

    @DisplayName("자신이 한 예약과 동일한 예약을 하는 경우 예외를 반환한다.")
    @Test
    void shouldReturnIllegalStateExceptionWhenDuplicatedReservationCreate() {
        Reservation existReservation = reservationRepository.findAll().get(0);
        ReservationRequest reservationRequest = new ReservationRequest(
                existReservation.getDate(),
                existReservation.getTime().getId(),
                existReservation.getTheme().getId());

        assertThatThrownBy(() -> reservationFactory.create(existReservation.getMember().getId(), reservationRequest.date(), reservationRequest.timeId(), reservationRequest.themeId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage("이미 예약을 했습니다.");
    }

    @DisplayName("과거 시간을 예약하는 경우 예외를 반환한다.")
    @Test
    void shouldThrowsIllegalArgumentExceptionWhenReservationDateIsBeforeCurrentDate() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(1999, 1, 1), 1L, 1L);

        assertThatThrownBy(() -> reservationFactory.create(1L, reservationRequest.date(), reservationRequest.timeId(), reservationRequest.themeId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage("현재 시간보다 과거로 예약할 수 없습니다.");
    }
}
