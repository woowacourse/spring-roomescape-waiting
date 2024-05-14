package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;
import roomescape.application.dto.ReservationRequest;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@ServiceTest
class ReservationFactoryTest {

    @Autowired
    private ReservationFactory reservationFactory;

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @DisplayName("존재하지 않는 예약 시간으로 예약을 생성시 예외를 반환한다.")
    @Test
    void shouldReturnIllegalArgumentExceptionWhenNotFoundReservationTime() {
        ReservationRequest request = ReservationRequestFixture.of(99L, 1L);

        assertThatCode(() -> reservationFactory.create(1L, request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_TIME);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약을 생성시 예외를 반환한다.")
    void shouldThrowIllegalArgumentExceptionWhenNotFoundTheme() {
        ReservationRequest request = ReservationRequestFixture.of(1L, 99L);

        assertThatCode(() -> reservationFactory.create(1L, request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_THEME);
    }

    @DisplayName("중복된 예약을 하는 경우 예외를 반환한다.")
    @Test
    void shouldReturnIllegalStateExceptionWhenDuplicatedReservationCreate() {
        Reservation existReservation = reservationQueryRepository.findAll().get(0);
        ReservationRequest reservationRequest = ReservationRequestFixture.of(existReservation.getDate(),
                existReservation.getTime().getId(),
                existReservation.getTheme().getId());

        assertThatCode(() -> reservationFactory.create(1L, reservationRequest))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.DUPLICATED_RESERVATION);
    }

    @DisplayName("과거 시간을 예약하는 경우 예외를 반환한다.")
    @Test
    void shouldThrowsIllegalArgumentExceptionWhenReservationDateIsBeforeCurrentDate() {
        ReservationRequest reservationRequest = ReservationRequestFixture.of(LocalDate.of(1999, 1, 1), 1L, 1L);

        assertThatCode(() -> reservationFactory.create(1L, reservationRequest))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }
}
