package roomescape.service.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.enums.Role;
import roomescape.dto.reservationtime.AvailableTimeResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.exception.reservationtime.ReservationTimeAlreadyExistsException;
import roomescape.exception.reservationtime.UsingReservationTimeException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository timeRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    private final ReservationStatus status = new ReservationStatus(1L);

    @DisplayName("중복되는 시간은 생성할 수 없다")
    @Test
    void duplicateReservationTimeTest() {
        // given
        LocalTime time = LocalTime.now();
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(time);
        when(timeRepository.existsByStartAt(time)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> reservationTimeService.create(reservationTimeRequest))
                .isInstanceOf(ReservationTimeAlreadyExistsException.class);
    }

    @DisplayName("예약 되어있는 예약 시간은 삭제할 수 없다")
    @Test
    void deleteReservedTimeTest() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByTimeId(id)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> reservationTimeService.deleteById(id))
                .isInstanceOf(UsingReservationTimeException.class);
    }

    @DisplayName("존재하지 않는 예약 시간을 삭제하면 예외가 발생한다")
    @Test
    void deleteNotFoundReservationTime() {
        // given
        when(reservationRepository.existsByTimeId(anyLong())).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> reservationTimeService.deleteById(anyLong()))
                .isInstanceOf(UsingReservationTimeException.class);
    }

    @DisplayName("예약 가능한 시간을 조회한다")
    @Test
    void availableTimeTest() {
        // given
        LocalDate date = LocalDate.now();
        Long themeId = 1L;

        Member member = new Member(10L, "name", "email@email.com", "pw", Role.USER);
        Theme theme = new Theme(themeId, "theme", "description", "thumbnail");
        Reservation reservation = new Reservation(1L, date, new ReservationTime(1L, LocalTime.now()), theme, member,
                status);
        when(reservationRepository.findAllByDateAndThemeId(date, themeId)).thenReturn(List.of(reservation));
        when(timeRepository.findAll()).thenReturn(List.of(new ReservationTime(1L, LocalTime.now()),
                new ReservationTime(2L, LocalTime.now())));

        // when
        List<AvailableTimeResponse> actual = reservationTimeService.getAvailableTimes(date, themeId);

        // then
        assertThat(actual.getFirst().alreadyBooked()).isTrue();
        assertThat(actual.getLast().alreadyBooked()).isFalse();
    }
}
