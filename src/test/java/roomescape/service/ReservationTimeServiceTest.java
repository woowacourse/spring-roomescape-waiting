package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.RoomEscapeException;
import roomescape.controller.dto.request.AvailableTimeFindRequest;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.SlotRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ReservationTimeServiceTest {

    @Mock
    private Clock clock;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private SlotRepository slotRepository;

    private void givenNow(LocalDateTime dateTime) {
        given(clock.instant()).willReturn(dateTime.toInstant(ZoneOffset.UTC));
        given(clock.getZone()).willReturn(ZoneOffset.UTC);
    }

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    void 정상적인_시간_삭제는_성공해야_한다() {
        given(reservationTimeRepository.existsById(1L)).willReturn(true);
        given(slotRepository.existsByTimeId(1L)).willReturn(false);

        Assertions.assertThatNoException().isThrownBy(() -> reservationTimeService.delete(1L));
    }

    @Test
    void 존재하지_않는_시간_삭제시_예외가_발생한다() {
        given(reservationTimeRepository.existsById(999L)).willReturn(false);

        Assertions.assertThatThrownBy(() -> reservationTimeService.delete(999L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약이_있는_시간_삭제시_예외가_발생한다() {
        given(reservationTimeRepository.existsById(1L)).willReturn(true);
        given(slotRepository.existsByTimeId(1L)).willReturn(true);

        Assertions.assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 과거_날짜로_예약_가능_시간_조회시_예외가_발생한다() {
        givenNow(LocalDateTime.of(2026, 6, 8, 0, 0));
        AvailableTimeFindRequest request = new AvailableTimeFindRequest(LocalDate.of(2026, 6, 7), 1L);

        Assertions.assertThatThrownBy(() -> reservationTimeService.findAvailable(request))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 오늘_이후_날짜로_예약_가능_시간_조회시_정상_조회된다() {
        givenNow(LocalDateTime.of(2026, 6, 8, 0, 0));
        AvailableTimeFindRequest request = new AvailableTimeFindRequest(LocalDate.of(2026, 6, 9), 1L);

        Assertions.assertThatNoException().isThrownBy(() -> reservationTimeService.findAvailable(request));
    }
}
