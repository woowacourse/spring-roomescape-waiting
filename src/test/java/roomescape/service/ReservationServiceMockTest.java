package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.UserReservationUpdateRequest;

@ExtendWith(MockitoExtension.class)
class ReservationServiceMockTest {

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private ReservationTimeDao reservationTimeDao;

    @Mock
    private ThemeDao themeDao;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void 예약_변경_시_이미_예약이_존재하면_대기_불가() {
        ReservationTime time = new ReservationTime(2L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "테마1", "설명", "url");

        given(reservationTimeDao.findTimeById(2L)).willReturn(time);
        given(themeDao.findThemeById(1L)).willReturn(theme);
        given(reservationDao.existsBy(any(), any(), any())).willReturn(true);

        UserReservationUpdateRequest updateRequest = new UserReservationUpdateRequest(
                LocalDate.of(2027, 6, 1), 2L, 1L
        );

        assertThatThrownBy(() -> reservationService.update(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
