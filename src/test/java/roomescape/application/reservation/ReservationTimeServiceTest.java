package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.presentation.reservation.request.TimeCreateRequest;
import roomescape.presentation.reservation.response.ReservationTimesResponse;
import roomescape.presentation.reservation.response.TimeCreateResponse;

@DisplayName("예약 시간 서비스")
@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository timeRepository;

    @Mock
    private ReservationSlotRepository slotRepository;

    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        reservationTimeService = new ReservationTimeService(timeRepository, slotRepository);
    }

    @DisplayName("예약 시간 목록을 조회할 수 있다")
    @Test
    void getAllReservationTime() {
        // given
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
        given(timeRepository.findAll()).willReturn(List.of(time));

        // when
        ReservationTimesResponse response = reservationTimeService.getAllReservationTime();

        // then
        assertThat(response.times()).hasSize(1);
        assertThat(response.times()).singleElement()
                .satisfies((Object payload) -> assertThat(payload)
                        .extracting("startAt")
                        .isEqualTo(LocalTime.of(10, 0)));
        verify(timeRepository, times(1)).findAll();
        verifyNoInteractions(slotRepository);
    }

    @DisplayName("예약 시간을 저장할 수 있다")
    @Test
    void createReservationTime() {
        // given
        TimeCreateRequest request = new TimeCreateRequest(LocalTime.of(18, 30));
        ReservationTime savedTime = ReservationTime.of(10L, LocalTime.of(18, 30));
        given(timeRepository.save(any(ReservationTime.class))).willReturn(savedTime);

        // when
        TimeCreateResponse response = reservationTimeService.createReservationTime(request);

        // then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.startAt()).isEqualTo(LocalTime.of(18, 30));
        verify(timeRepository, times(1)).save(any(ReservationTime.class));
        verifyNoInteractions(slotRepository);
    }

    @DisplayName("사용 중이 아닌 예약 시간은 삭제할 수 있다")
    @Test
    void deleteReservationTime() {
        // given
        given(slotRepository.existsByTimeId(1L)).willReturn(false);
        given(timeRepository.deleteById(1L)).willReturn(1);

        // when
        reservationTimeService.deleteReservationTime(1L);

        // then
        verify(slotRepository, times(1)).existsByTimeId(1L);
        verify(timeRepository, times(1)).deleteById(1L);
    }

    @DisplayName("사용 중인 예약 시간은 삭제할 수 없다")
    @Test
    void deleteReservationTimeWhenInUse() {
        // given
        given(slotRepository.existsByTimeId(1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_TIME_IN_USE);
        verify(slotRepository, times(1)).existsByTimeId(1L);
        verify(timeRepository, never()).deleteById(1L);
    }

    @DisplayName("존재하지 않는 예약 시간은 삭제할 수 없다")
    @Test
    void deleteReservationTimeWhenNotFound() {
        // given
        given(slotRepository.existsByTimeId(1L)).willReturn(false);
        given(timeRepository.deleteById(1L)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_TIME_NOT_FOUND);
        verify(slotRepository, times(1)).existsByTimeId(1L);
        verify(timeRepository, times(1)).deleteById(1L);
    }
}
