package roomescape.waiting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.waiting.dao.ReservationWaitingDao;
import roomescape.waiting.service.ReservationWaitingService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationWaitingServiceTest {

    @Mock
    private ReservationWaitingDao reservationWaitingDao;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    @Test
    void 중복_예약_대기_신청하는_경우_예외_발생() {
        String name = "ever";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;

        when(reservationWaitingDao.existsByNameAndDateAndThemeIdAndTimeId(anyString(), anyLong(), any(LocalDate.class), anyLong())).thenReturn(true);

        assertThatThrownBy(() ->reservationWaitingService.add(name, themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.DUPLICATED_RESERVATION_WAITING.getMessage());
    }
}
