package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.business.dto.ReservableReservationTimeDto;
import roomescape.business.dto.ReservationTimeDto;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.repository.ReservationRepository;
import roomescape.business.model.repository.ReservationTimeRepository;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.StartTime;
import roomescape.exception.business.DuplicatedException;
import roomescape.exception.business.InvalidCreateArgumentException;
import roomescape.exception.business.NotFoundException;
import roomescape.exception.business.RelatedEntityExistException;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationTimeService sut;

    @Test
    void 예약_시간을_추가하고_반환한다() {
        // given
        LocalTime time = LocalTime.of(10, 0);

        when(reservationTimeRepository.existByTime(time)).thenReturn(false);
        when(reservationTimeRepository.existBetween(any(LocalTime.class), any(LocalTime.class))).thenReturn(false);

        // when
        ReservationTimeDto result = sut.addAndGet(time);

        // then
        assertThat(result).isNotNull();
        assertThat(result.startTime().value()).isEqualTo(time);
        verify(reservationTimeRepository).existByTime(time);
        verify(reservationTimeRepository).existBetween(any(LocalTime.class), any(LocalTime.class));
        verify(reservationTimeRepository).save(any(ReservationTime.class));
    }

    @Test
    void 중복된_시간으로_예약_시간_추가_시_예외가_발생한다() {
        // given
        LocalTime time = LocalTime.of(10, 0);

        when(reservationTimeRepository.existByTime(time)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(time))
                .isInstanceOf(DuplicatedException.class);

        verify(reservationTimeRepository).existByTime(time);
        verify(reservationTimeRepository, never()).existBetween(any(LocalTime.class), any(LocalTime.class));
        verify(reservationTimeRepository, never()).save(any(ReservationTime.class));
    }

    @Test
    void 시간_간격이_겹치는_예약_시간_추가_시_예외가_발생한다() {
        // given
        LocalTime time = LocalTime.of(10, 0);

        when(reservationTimeRepository.existByTime(time)).thenReturn(false);
        when(reservationTimeRepository.existBetween(any(LocalTime.class), any(LocalTime.class))).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> sut.addAndGet(time))
                .isInstanceOf(InvalidCreateArgumentException.class);

        verify(reservationTimeRepository).existByTime(time);
        verify(reservationTimeRepository).existBetween(any(LocalTime.class), any(LocalTime.class));
        verify(reservationTimeRepository, never()).save(any(ReservationTime.class));
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

        when(reservationTimeRepository.findAll()).thenReturn(dataTimes);

        // when
        List<ReservationTimeDto> result = sut.getAll();

        // then
        assertThat(result).isEqualTo(expectedTimes);
        verify(reservationTimeRepository).findAll();
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

        when(reservationTimeRepository.findAvailableByDateAndThemeId(date, themeId))
                .thenReturn(availableTimes);
        when(reservationTimeRepository.findNotAvailableByDateAndThemeId(date, themeId))
                .thenReturn(notAvailableTimes);

        // when
        List<ReservableReservationTimeDto> result = sut.getAllByDateAndThemeId(date, themeId.value());

        // then
        assertThat(result).containsExactlyElementsOf(expectedAvailableTimes);
        verify(reservationTimeRepository).findAvailableByDateAndThemeId(date, themeId);
        verify(reservationTimeRepository).findNotAvailableByDateAndThemeId(date, themeId);
    }

    @Test
    void 예약_시간을_삭제할_수_있다() {
        // given
        Id timeId = Id.create("time-id");

        when(reservationRepository.existByTimeId(timeId)).thenReturn(false);
        when(reservationTimeRepository.existById(timeId)).thenReturn(true);

        // when
        sut.delete(timeId.value());

        // then
        verify(reservationRepository).existByTimeId(timeId);
        verify(reservationTimeRepository).existById(timeId);
        verify(reservationTimeRepository).deleteById(timeId);
    }

    @Test
    void 존재하지_않는_예약_시간_삭제_시_예외가_발생한다() {
        // given
        Id timeId = Id.create("non-existing-id");

        when(reservationRepository.existByTimeId(timeId)).thenReturn(false);
        when(reservationTimeRepository.existById(timeId)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> sut.delete(timeId.value()))
                .isInstanceOf(NotFoundException.class);

        verify(reservationRepository).existByTimeId(timeId);
        verify(reservationTimeRepository).existById(timeId);
        verify(reservationTimeRepository, never()).deleteById(timeId);
    }

    @Test
    void 예약이_연결된_예약_시간_삭제_시_예외가_발생한다() {
        // given
        Id timeId = Id.create("time-with-reservations");

        when(reservationRepository.existByTimeId(timeId)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> sut.delete(timeId.value()))
                .isInstanceOf(RelatedEntityExistException.class);

        verify(reservationRepository).existByTimeId(timeId);
        verify(reservationTimeRepository, never()).existById(timeId);
        verify(reservationTimeRepository, never()).deleteById(timeId);
    }
}
