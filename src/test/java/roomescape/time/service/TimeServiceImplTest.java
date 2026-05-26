package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import roomescape.time.domain.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.exception.ReservationTimeConflictException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.repository.TimeRepository;

@ExtendWith(MockitoExtension.class)
class TimeServiceImplTest {

    @Mock
    private TimeRepository timeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private TimeServiceImpl timeService;

    @BeforeEach
    void setUp() {
        timeService = new TimeServiceImpl(timeRepository, reservationRepository);
    }

    @DisplayName("시간을 생성하면 저장된 시간을 반환한다.")
    @Test
    void create_정상_저장하고_반환() {
        // given
        LocalDateTime start = LocalDateTime.of(2030, 6, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 6, 1, 12, 0);
        ReservationTime saved = new ReservationTime(1L, start, end);
        when(timeRepository.save(start, end)).thenReturn(saved);

        // when
        ReservationTime result = timeService.create(start, end);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStartAt()).isEqualTo(start);
    }

    @DisplayName("전체 시간 목록을 반환한다.")
    @Test
    void findAll_전체_목록_반환() {
        // given
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalDateTime.of(2030, 6, 1, 10, 0), LocalDateTime.of(2030, 6, 1, 12, 0)),
                new ReservationTime(2L, LocalDateTime.of(2030, 6, 1, 13, 0), LocalDateTime.of(2030, 6, 1, 15, 0))
        );
        when(timeRepository.findAll()).thenReturn(times);

        // when
        List<ReservationTime> result = timeService.findAll();

        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("존재하는 id로 조회하는 경우, 해당 시간을 반환한다.")
    @Test
    void findById_정상_조회() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalDateTime.of(2030, 6, 1, 10, 0), LocalDateTime.of(2030, 6, 1, 12, 0));
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));

        // when
        ReservationTime result = timeService.findById(1L);

        // then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @DisplayName("존재하지 않는 id로 조회하는 경우, TimeNotFoundException이 발생한다.")
    @Test
    void findById_없는_id이면_예외() {
        // given
        when(timeRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> timeService.findById(999L))
                .isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("예약이 없는 시간을 삭제하는 경우, 정상적으로 삭제된다.")
    @Test
    void deleteById_정상_삭제() {
        // given
        when(reservationRepository.existsByTimeId(1L)).thenReturn(false);
        when(timeRepository.deleteById(1L)).thenReturn(true);

        // when
        timeService.deleteById(1L);

        // then
        verify(timeRepository).deleteById(1L);
    }

    @DisplayName("예약이 존재하는 시간을 삭제하는 경우, ReservationTimeConflictException이 발생한다.")
    @Test
    void deleteById_예약이_있으면_예외() {
        // given
        when(reservationRepository.existsByTimeId(1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> timeService.deleteById(1L))
                .isInstanceOf(ReservationTimeConflictException.class);
    }

    @DisplayName("존재하지 않는 id를 삭제하는 경우, TimeNotFoundException이 발생한다.")
    @Test
    void deleteById_없는_id이면_예외() {
        // given
        when(reservationRepository.existsByTimeId(999L)).thenReturn(false);
        when(timeRepository.deleteById(999L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> timeService.deleteById(999L))
                .isInstanceOf(TimeNotFoundException.class);
    }
}
