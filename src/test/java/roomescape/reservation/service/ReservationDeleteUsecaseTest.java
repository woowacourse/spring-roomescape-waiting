package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.BadArgumentRequestException;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.theme.dto.ThemeResponse;
import roomescape.time.dto.TimeResponse;

@ExtendWith(MockitoExtension.class)
class ReservationDeleteUsecaseTest {
    private final ReservationResponse BEFORE_RESERVATION = new ReservationResponse(1L,
            new MemberResponse(1L, "브라운"),
            LocalDate.now().minusDays(7),
            new TimeResponse(1L, LocalTime.of(11, 0)),
            new ThemeResponse(1L, "테마 1", "묘사", "https://img.jpg"));

    @Mock
    private ReservationFindService reservationFindService;
    @InjectMocks
    private ReservationDeleteUsecase reservationDeleteUsecase;

    @DisplayName("현재보다 이전 예약을 취소할 경우, 예외를 던진다.")
    @Test
    void executeTest_whenReservationIsBeforeFromNow() {
        given(reservationFindService.findReservation(1L)).willReturn(BEFORE_RESERVATION);

        assertThatThrownBy(() -> reservationDeleteUsecase.execute(1L))
                .isInstanceOf(BadArgumentRequestException.class)
                .hasMessage("예약은 현재 날짜 이후여야 합니다.");
    }
}
