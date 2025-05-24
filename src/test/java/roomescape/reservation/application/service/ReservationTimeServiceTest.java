package roomescape.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.application.exception.ReservationTimeAlreadyExistsException;
import roomescape.reservation.application.exception.UsingReservationTimeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.presentation.dto.AvailableTimeResponse;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.dto.ReservationTimeResponse;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository timeRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

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

    @DisplayName("예약 시간을 생성한다.")
    @Test
    void createTest() {
        // given
        LocalTime time = LocalTime.now();
        ReservationTime reservationTime = new ReservationTime(1L, time);

        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(time);

        when(timeRepository.existsByStartAt(time)).thenReturn(false);
        when(timeRepository.save(any(ReservationTime.class))).thenReturn(reservationTime);

        // when
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(reservationTimeRequest);

        // then
        assertThat(reservationTimeResponse.id()).isNotNull();
    }

    @DisplayName("모든 예약 시간을 조회할 수 있다")
    @Test
    void getAllTest() {
        // given
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0)),
                new ReservationTime(3L, LocalTime.of(12, 0))
        );
        when(timeRepository.findAll()).thenReturn(times);

        // when
        List<ReservationTimeResponse> responses = reservationTimeService.getAll();

        // then
        assertThat(responses).hasSize(3);
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

    @DisplayName("예약 시간을 삭제한다.")
    @Test
    void deleteByIdTest() {
        // given
        LocalTime time = LocalTime.now();
        ReservationTime reservationTime = new ReservationTime(1L, time);

        when(reservationRepository.existsByTimeId(anyLong())).thenReturn(false);

        // when
        reservationTimeService.deleteById(1L);

        // then
        verify(timeRepository).deleteById(reservationTime.getId());
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
                ReservationStatus.CONFIRMED, LocalDateTime.now());
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
