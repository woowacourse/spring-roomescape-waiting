package roomescape.reservationTime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.ForeignKeyException;
import roomescape.common.exception.InvalidIdException;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.reservationTime.dto.admin.ReservationTimeRequest;
import roomescape.reservationTime.dto.user.AvailableReservationTimeRequest;
import roomescape.theme.domain.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    private ReservationTimeRepository timeRepository;
    private ThemeRepository themeRepository;
    private ReservationRepository reservationRepository;

    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        timeRepository = mock(ReservationTimeRepository.class);
        themeRepository = mock(ThemeRepository.class);
        reservationRepository = mock(ReservationRepository.class);

        reservationTimeService = new ReservationTimeService(timeRepository, reservationRepository, themeRepository);
    }

    @DisplayName("시간 내역을 조회하는 기능을 구현한다")
    @Test
    void findAll() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        when(timeRepository.findAll()).thenReturn(List.of(reservationTime));

        assertThat(reservationTimeService.findAll()).hasSize(1);
        verify(timeRepository, times(1)).findAll();
    }

    @DisplayName("시간 내역을 날짜와 테마로 조회 시, 테마 아이다가 존재하지 않는 경우 예외를 발생시킨다")
    @Test
    void exception_find_invalid_themeId() {
        when(themeRepository.findById(1L)).thenReturn(Optional.empty());
        AvailableReservationTimeRequest availableReservationTimeRequest = new AvailableReservationTimeRequest(
                LocalDate.now(), 1L
        );

        assertThatThrownBy(() -> reservationTimeService.findByDateAndTheme(availableReservationTimeRequest))
                .isInstanceOf(InvalidIdException.class);
    }

    @DisplayName("시간 내역을 추가 시 중복되는 시간인 경우 예외를 발생시킨다")
    @Test
    void exception_add_duplicate_timeId() {
        when(timeRepository.existsByStartAt(LocalTime.of(11, 0))).thenReturn(true);

        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(11, 0));
        assertThatThrownBy(() -> reservationTimeService.add(reservationTimeRequest))
                .isInstanceOf(DuplicateException.class);
    }

    @DisplayName("시간 내역을 삭제 시 존재하지 않는 시간 아이디인 경우 예외를 발생시킨다")
    @Test
    void exception_delete_invalid_timeId() {
        when(timeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(InvalidIdException.class);
    }

    @DisplayName("시간 내역을 삭제 시 이미 예약된 시간 아이디인 경우 예외를 발생시킨다")
    @Test
    void exception_delete_occupied_timeId() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        when(timeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));
        when(timeRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(ForeignKeyException.class);
    }
}
