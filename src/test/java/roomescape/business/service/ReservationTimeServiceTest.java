package roomescape.business.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.business.dto.ReservableReservationTimeDto;
import roomescape.business.dto.ReservationTimeDto;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.repository.ReservationTimes;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.StartTime;
import roomescape.exception.business.DuplicatedException;
import roomescape.exception.business.InvalidCreateArgumentException;
import roomescape.exception.business.NotFoundException;
import roomescape.exception.business.RelatedEntityExistException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimes reservationTimes;

    @Mock
    private Reservations reservations;

    @InjectMocks
    private ReservationTimeService sut;

    @Test
    void 예약_시간을_추가하고_반환한다() {
        // given
        LocalTime time = LocalTime.of(10, 0);

        when(reservationTimes.existByTime(time)).thenReturn(false);
        when(reservationTimes.existBetween(any(LocalTime.class), any(LocalTime.class))).thenReturn(false);

        // when
        ReservationTimeDto result = sut.addAndGet(time);

        // then
        assertThat(result).isNotNull();
        assertThat(result.startTime().value()).isEqualTo(time);
        verify(reservationTimes).existByTime(time);
        verify(reservationTimes).existBetween(any(LocalTime.class), any(LocalTime.class));
        verify(reservationTimes).save(any(ReservationTime.class));
    }

    @Test
    void 중복된_시간으로_예약_시간_추가_시_예외가_발생한다() {
        // given
        LocalTime time = LocalTime.of(10, 0);

        when(reservationTimes.existByTime(time)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(time))
                .isInstanceOf(DuplicatedException.class);

        verify(reservationTimes).existByTime(time);
        verify(reservationTimes, never()).existBetween(any(LocalTime.class), any(LocalTime.class));
        verify(reservationTimes, never()).save(any(ReservationTime.class));
    }

    @Test
    void 시간_간격이_겹치는_예약_시간_추가_시_예외가_발생한다() {
        // given
        LocalTime time = LocalTime.of(10, 0);

        when(reservationTimes.existByTime(time)).thenReturn(false);
        when(reservationTimes.existBetween(any(LocalTime.class), any(LocalTime.class))).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(time))
                .isInstanceOf(InvalidCreateArgumentException.class);

        verify(reservationTimes).existByTime(time);
        verify(reservationTimes).existBetween(any(LocalTime.class), any(LocalTime.class));
        verify(reservationTimes, never()).save(any(ReservationTime.class));
    }

    @Test
    void 모든_예약_시간을_조회할_수_있다() {
        // given
        List<ReservationTime> dataTimes = Arrays.asList(
                ReservationTime.restore("time-id-1", LocalTime.of(10, 0)),
                ReservationTime.restore("time-id-2", LocalTime.of(14, 0))
        );

        List<ReservationTimeDto> expectedTimes = List.of(
                new ReservationTimeDto(Id.create("time-id-1"), new StartTime(LocalTime.of(10, 0))),
                new ReservationTimeDto(Id.create("time-id-2"), new StartTime(LocalTime.of(14, 0)))
        );

        when(reservationTimes.findAll()).thenReturn(dataTimes);

        // when
        List<ReservationTimeDto> result = sut.getAll();

        // then
        assertThat(result).isEqualTo(expectedTimes);
        verify(reservationTimes).findAll();
    }

    @Test
    void 날짜와_테마_ID로_이용_가능한_예약_시간을_조회할_수_있다() {
        // given
        LocalDate date = LocalDate.now();
        Id themeId = Id.create("theme-id");
        List<ReservationTime> availableTimes = Arrays.asList(
                ReservationTime.restore("time-id-3", LocalTime.of(11, 0)),
                ReservationTime.restore("time-id-4", LocalTime.of(15, 0))
        );
        List<ReservationTime> notAvailableTimes = Arrays.asList(
                ReservationTime.restore("time-id-5", LocalTime.of(12, 0)),
                ReservationTime.restore("time-id-6", LocalTime.of(16, 0))
        );
        List<ReservableReservationTimeDto> expectedAvailableTimes = Arrays.asList(
                new ReservableReservationTimeDto(Id.create("time-id-3"), new StartTime(LocalTime.of(11, 0)), true),
                new ReservableReservationTimeDto(Id.create("time-id-4"), new StartTime(LocalTime.of(15, 0)), true),
                new ReservableReservationTimeDto(Id.create("time-id-5"), new StartTime(LocalTime.of(12, 0)), false),
                new ReservableReservationTimeDto(Id.create("time-id-6"), new StartTime(LocalTime.of(16, 0)), false)
        );

        when(reservationTimes.findAvailableByDateAndThemeId(date, themeId))
                .thenReturn(availableTimes);
        when(reservationTimes.findNotAvailableByDateAndThemeId(date, themeId))
                .thenReturn(notAvailableTimes);

        // when
        List<ReservableReservationTimeDto> result = sut.getAllByDateAndThemeId(date, themeId.value());

        // then
        assertThat(result).containsExactlyElementsOf(expectedAvailableTimes);
        verify(reservationTimes).findAvailableByDateAndThemeId(date, themeId);
        verify(reservationTimes).findNotAvailableByDateAndThemeId(date, themeId);
    }

    @Test
    void 예약_시간을_삭제할_수_있다() {
        // given
        Id timeId = Id.create("time-id");

        when(reservations.existByTimeId(timeId)).thenReturn(false);
        when(reservationTimes.existById(timeId)).thenReturn(true);

        // when
        sut.delete(timeId.value());

        // then
        verify(reservations).existByTimeId(timeId);
        verify(reservationTimes).existById(timeId);
        verify(reservationTimes).deleteById(timeId);
    }

    @Test
    void 존재하지_않는_예약_시간_삭제_시_예외가_발생한다() {
        // given
        Id timeId = Id.create("non-existing-id");

        when(reservations.existByTimeId(timeId)).thenReturn(false);
        when(reservationTimes.existById(timeId)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> sut.delete(timeId.value()))
                .isInstanceOf(NotFoundException.class);

        verify(reservations).existByTimeId(timeId);
        verify(reservationTimes).existById(timeId);
        verify(reservationTimes, never()).deleteById(timeId);
    }

    @Test
    void 예약이_연결된_예약_시간_삭제_시_예외가_발생한다() {
        // given
        Id timeId = Id.create("time-with-reservations");

        when(reservations.existByTimeId(timeId)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> sut.delete(timeId.value()))
                .isInstanceOf(RelatedEntityExistException.class);

        verify(reservations).existByTimeId(timeId);
        verify(reservationTimes, never()).existById(timeId);
        verify(reservationTimes, never()).deleteById(timeId);
    }
}
