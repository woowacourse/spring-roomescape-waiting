package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import roomescape.domain.ReservationTime;
import roomescape.exception.BusinessException;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaReservationWaitingRepository;

class ReservationTimeServiceTest {

    private final JpaReservationTimeRepository reservationTimeRepository = mock();
    private final JpaReservationRepository reservationRepository = mock();
    private final JpaReservationWaitingRepository reservationWaitingRepository = mock();
    private final ReservationTimeService service = new ReservationTimeService(
            reservationTimeRepository,
            reservationRepository,
            reservationWaitingRepository);

    @Test
    void 전체_시간_조회_테스트() {
        // given
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalTime.of(8, 0)),
                new ReservationTime(2L, LocalTime.of(10, 0)));
        when(reservationTimeRepository.findAll())
                .thenReturn(times);

        // when
        List<ReservationTime> result = service.findAll();

        // then
        assertThat(result).isEqualTo(times);
    }

    @Test
    void 시간_생성_테스트() {
        // given
        Long id = 1L;
        LocalTime startAt = LocalTime.of(8, 0);
        ReservationTime time = new ReservationTime(id, startAt);

        when(reservationTimeRepository.save(any(ReservationTime.class)))
                .thenReturn(new ReservationTime(id, startAt));
        when(reservationTimeRepository.findById(id))
                .thenReturn(Optional.of(time));

        // when
        ReservationTime result = service.create(startAt);

        // then
        ArgumentCaptor<ReservationTime> captor = ArgumentCaptor.forClass(ReservationTime.class);

        assertAll(
                () -> assertThat(result.getId()).isEqualTo(id),
                () -> assertThat(result.getStartAt()).isEqualTo(startAt));

        verify(reservationTimeRepository, times(1)).save(captor.capture());
        ReservationTime captured = captor.getValue();

        assertAll(
                () -> assertThat(captured.getId()).isNull(),
                () -> assertThat(captured.getStartAt()).isEqualTo(startAt));
    }

    @Test
    void 시간_삭제_테스트() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByTime_Id(id))
                .thenReturn(false);
        when(reservationWaitingRepository.existsByTime_Id(id))
                .thenReturn(false);

        // when
        service.delete(id);

        // then
        verify(reservationTimeRepository).deleteById(id);
    }

    @Test
    void 예약이_존재하는_시간은_삭제시_예외_발생() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByTime_Id(id))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");

        verify(reservationTimeRepository, never()).deleteById(anyLong());
    }

    @Test
    void 예약_대기가_존재하는_시간은_삭제시_예외_발생() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByTime_Id(id))
                .thenReturn(false);
        when(reservationWaitingRepository.existsByTime_Id(id))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("예약 대기가 존재하는 시간은 삭제할 수 없습니다.");

        verify(reservationTimeRepository, never()).deleteById(anyLong());
    }
}
