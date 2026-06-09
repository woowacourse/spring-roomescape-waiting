package roomescape.reservationwaiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationwaiting.FakeReservationWaitingRepository;
import roomescape.service.reservationwaiting.ReservationWaitingService;

class ReservationWaitingServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-06T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private static final LocalDate DATE = LocalDate.parse("2026-08-06");

    private ReservationRepository reservationRepository;
    private FakeReservationWaitingRepository reservationWaitingRepository;
    private ReservationWaitingService reservationWaitingService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        reservationWaitingRepository = new FakeReservationWaitingRepository();
        reservationWaitingService = new ReservationWaitingService(
                reservationRepository,
                reservationWaitingRepository,
                CLOCK
        );

        Reservation reservation = createReservation();
        when(reservationRepository.findByDateAndThemeIdAndTimeId(DATE, 1L, 1L))
                .thenReturn(Optional.of(reservation));
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 대기를 생성한다")
    void save() {
        ReservationWaiting waiting = reservationWaitingService.save("아루", DATE, 1L, 1L);

        assertThat(waiting.getId()).isNotNull();
        assertThat(waiting.getName()).isEqualTo("아루");
        assertThat(waiting.getReservation().getId()).isEqualTo(1L);
        assertThat(reservationWaitingRepository.existsByReservationIdAndName(1L, "아루")).isTrue();
    }

    @Test
    @DisplayName("예약이 없는 슬롯에는 대기를 생성할 수 없다")
    void saveWithoutReservation() {
        when(reservationRepository.findByDateAndThemeIdAndTimeId(DATE, 1L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.save("아루", DATE, 1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("예약자 본인은 자신의 예약에 대기할 수 없다")
    void saveByReservationOwner() {
        assertThatThrownBy(() -> reservationWaitingService.save("쿠다", DATE, 1L, 1L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("이미 같은 예약에 대기 중인 이름으로는 다시 대기할 수 없다")
    void saveDuplicatedName() {
        reservationWaitingService.save("아루", DATE, 1L, 1L);

        assertThatThrownBy(() -> reservationWaitingService.save("아루", DATE, 1L, 1L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("예약자 이름이 10자 이상이면 대기를 생성할 수 없다")
    void saveWithTooLongName() {
        assertThatThrownBy(() -> reservationWaitingService.save("일이삼사오육칠팔구십", DATE, 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Reservation createReservation() {
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.of(1L, LocalTime.parse("10:00"));

        return Reservation.of(1L, "쿠다", DATE, theme, time);
    }
}
