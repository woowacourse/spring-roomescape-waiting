package roomescape.reservationTime.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.exception.ReservationTimeErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservationTime.application.dto.ReservationTimeCreateCommand;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ReservationTimeReference reservationReference;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 시간을 저장한다")
    void saveTime_success() {
        // given
        LocalTime testStartAt = LocalTime.now();
        ReservationTime saved = ReservationTime.createRow(1L, testStartAt);
        given(reservationTimeRepository.save(any(ReservationTime.class)))
                .willReturn(saved);

        // when
        ReservationTime reservationTime = reservationTimeService.saveTime(new ReservationTimeCreateCommand(testStartAt));

        // then
        assertThat(reservationTime).isEqualTo(saved);

        ArgumentCaptor<ReservationTime> captor = ArgumentCaptor.forClass(ReservationTime.class);
        verify(reservationTimeRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getStartAt()).isEqualTo(testStartAt);
        verifyNoMoreInteractions(reservationTimeRepository);
        verifyNoInteractions(reservationReference);
    }

    @Test
    @DisplayName("예약 시간 목록을 조회한다")
    void getTimes_success() {
        // given
        LocalTime testStartAt = LocalTime.now();
        ReservationTime saved = ReservationTime.createRow(1L, testStartAt);
        given(reservationTimeRepository.findAll())
                .willReturn(List.of(saved));

        // when
        List<ReservationTime> reservationTimes = reservationTimeService.getTimes();

        // then
        assertThat(reservationTimes)
                .extracting(ReservationTime::getStartAt)
                .containsExactly(testStartAt);

        verify(reservationTimeRepository, times(1)).findAll();
        verifyNoMoreInteractions(reservationTimeRepository);
        verifyNoInteractions(reservationReference);
    }

    @Test
    @DisplayName("예약 시간이 없으면 빈 목록을 반환한다")
    void getTimes_success_when_empty() {
        // given
        given(reservationTimeRepository.findAll())
                .willReturn(List.of());

        // when
        List<ReservationTime> reservationTimes = reservationTimeService.getTimes();

        // then
        assertThat(reservationTimes).isEmpty();

        verify(reservationTimeRepository, times(1)).findAll();
        verifyNoMoreInteractions(reservationTimeRepository);
        verifyNoInteractions(reservationReference);
    }

    @Test
    @DisplayName("날짜와 테마 기준으로 예약된 시간을 조회한다")
    void getBookedTimes_success() {
        // given
        ReservationTime bookedTime = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        LocalDate date = LocalDate.now().plusDays(1);
        Long themeId = 1L;
        given(reservationReference.getBookedTimes(date, themeId))
                .willReturn(List.of(bookedTime));

        // when
        Set<Long> result = reservationTimeService.getBookedTimes(date, themeId);

        // then
        assertThat(result)
                .containsExactlyInAnyOrder(bookedTime.getId());

        verify(reservationReference, times(1)).getBookedTimes(date, themeId);
        verifyNoInteractions(reservationTimeRepository);
        verifyNoMoreInteractions(reservationReference);
    }

    @Test
    @DisplayName("예약 시간을 삭제한다")
    void deleteTime_success() {
        // given
        Long timeId = 1L;
        ReservationTime saved = ReservationTime.createRow(timeId, LocalTime.now());
        given(reservationTimeRepository.findById(timeId))
                .willReturn(Optional.of(saved));

        // when
        reservationTimeService.deleteTime(timeId);

        // then
        verify(reservationTimeRepository, times(1)).findById(timeId);
        verify(reservationReference, times(1)).validateReservationTimeNotReferenced(timeId);
        verify(reservationTimeRepository, times(1)).deleteById(timeId);
        verifyNoMoreInteractions(reservationTimeRepository);
        verifyNoMoreInteractions(reservationReference);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하면 예외가 발생한다")
    void deleteReservationTime_fail_with_not_found_time() {
        // given
        Long timeId = 999L;
        given(reservationTimeRepository.findById(timeId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(timeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("예약 시간을 찾을 수 없습니다.");

        verify(reservationTimeRepository, times(1)).findById(timeId);
        verify(reservationTimeRepository, times(0)).deleteById(timeId);
        verifyNoMoreInteractions(reservationTimeRepository);
        verifyNoInteractions(reservationReference);
    }

    @Test
    @DisplayName("예약에서 참조 중인 예약 시간을 삭제하면 예외가 발생한다")
    void deleteTime_fail_with_referenced_time() {
        // given
        Long timeId = 1L;
        ReservationTime savedReservationTime = ReservationTime.createRow(timeId, LocalTime.now().plusHours(1));
        given(reservationTimeRepository.findById(timeId))
                .willReturn(Optional.of(savedReservationTime));
        willThrow(new BusinessException(ReservationTimeErrorCode.RESERVATION_TIME_IN_USE))
                .given(reservationReference)
                .validateReservationTimeNotReferenced(timeId);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(timeId))
                .isInstanceOf(BusinessException.class);

        verify(reservationTimeRepository, times(1)).findById(timeId);
        verify(reservationReference, times(1)).validateReservationTimeNotReferenced(timeId);
        verify(reservationTimeRepository, times(0)).deleteById(timeId);
        verifyNoMoreInteractions(reservationTimeRepository);
        verifyNoMoreInteractions(reservationReference);
    }
}
