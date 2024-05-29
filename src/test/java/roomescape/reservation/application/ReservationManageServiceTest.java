package roomescape.reservation.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import roomescape.global.exception.ViolationException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationTime;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static roomescape.TestFixture.MIA_RESERVATION;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;
import static roomescape.TestFixture.USER_MIA;
import static roomescape.TestFixture.WOOTECO_THEME;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;

class ReservationManageServiceTest {
    private ReservationManageService reservationManageService;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        this.reservationRepository = mock(ReservationRepository.class);
        this.reservationManageService = new DummyReservationManageService(reservationRepository);
    }

    @ParameterizedTest
    @MethodSource("invalidReservationDate")
    @DisplayName("예약 날짜는 현재 날짜 이후이다.")
    void validateReservationDate(LocalDate invalidDate) {
        // given
        Reservation reservation = new Reservation(
                USER_MIA(), invalidDate, new ReservationTime(MIA_RESERVATION_TIME), WOOTECO_THEME(), BOOKING);

        // when & then
        assertThatThrownBy(() -> reservationManageService.create(reservation))
                .isInstanceOf(ViolationException.class);
    }

    private static Stream<LocalDate> invalidReservationDate() {
        return Stream.of(
                LocalDate.now(),
                LocalDate.now().minusDays(1L)
        );
    }

    @Test
    @DisplayName("사용자는 중복된 예약을 할 수 없다.")
    void validateDuplicatedReservation() {
        // given
        BDDMockito.given(reservationRepository.existsByDateAndTimeAndThemeAndMember(any(), any(), any(), any()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationManageService.create(MIA_RESERVATION()))
                .isInstanceOf(ViolationException.class);
    }
}
