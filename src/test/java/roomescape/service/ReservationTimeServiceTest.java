package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationTime;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundReservationTimeException;
import roomescape.persistence.ReservationRepository;
import roomescape.persistence.ReservationTimeRepository;
import roomescape.service.param.CreateReservationTimeParam;
import roomescape.service.result.ReservationTimeResult;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    ReservationRepository reservationRepository;
    @Mock
    ReservationTimeRepository reservationTimeRepository;
    @InjectMocks
    ReservationTimeService reservationTimeService;

    @Test
    void 예약_시간을_생성할_수_있다() {
        // given
        ReservationTime saved = new ReservationTime(1L, LocalTime.of(12, 1));
        when(reservationTimeRepository.save(any(ReservationTime.class))).thenReturn(saved);

        // when
        Long createdId = reservationTimeService.create(new CreateReservationTimeParam(LocalTime.of(12, 1)));

        // then
        assertThat(createdId).isEqualTo(1L);
    }

    @Test
    void id에_해당하는_예약_시간을_찾을_수_있다() {
        // given
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 1));
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));

        // when
        ReservationTimeResult reservationTimeResult = reservationTimeService.findById(1L);

        // then
        assertThat(reservationTimeResult).isEqualTo(new ReservationTimeResult(1L, LocalTime.of(12, 1)));
    }

    @Test
    void id에_해당하는_예약_시간이_없는경우_예외가_발생한다() {
        // given
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationTimeService.findById(1L))
                .isInstanceOf(NotFoundReservationTimeException.class)
                .hasMessageContaining("1에 해당하는 reservation_time 튜플이 없습니다.");
    }

    @Test
    void 전체_예약_시간을_조회할_수_있다() {
        // given
        List<ReservationTime> times = List.of(new ReservationTime(1L, LocalTime.of(12, 1)));
        when(reservationTimeRepository.findAll()).thenReturn(times);

        // when
        List<ReservationTimeResult> reservationTimeResults = reservationTimeService.findAll();

        // then
        assertThat(reservationTimeResults).isEqualTo(List.of(
                new ReservationTimeResult(1L, LocalTime.of(12, 1))
        ));
    }

    @Test
    void time_id를_사용하는_예약이_존재하면_예외를_던진다() {
        // given
        when(reservationRepository.existsByTimeId(1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(DeletionNotAllowedException.class)
                .hasMessageContaining("해당 예약 시간에 예약이 존재합니다.");
    }
}
