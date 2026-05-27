package roomescape.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.exception.IdNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ThemeDao themeDao;

    @Mock
    private ReservationTimeDao reservationTimeDao;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void 존재하지_않는_시간_ID로_예약_시_예외() {
        Long invalidTimeId = 999L;
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 5, 4), invalidTimeId, 1L);

        when(reservationTimeDao.findTimeById(invalidTimeId)).thenReturn(null);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IdNotFoundException.class)
                .hasMessageContaining("요청하신 시간 정보를 찾을 수 없습니다. 선택하신 시간이 정확한지 다시 한번 확인해 주세요.");
    }

    @Test
    void 존재하지_않는_테마_ID로_예약_시_예외() {
        Long invalidThemeId = 999L;
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 5, 4), 1L, invalidThemeId);

        when(reservationTimeDao.findTimeById(1L)).thenReturn(new ReservationTime(1L, LocalTime.of(10, 0)));
        when(themeDao.findThemeById(invalidThemeId)).thenReturn(null);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IdNotFoundException.class)
                .hasMessageContaining("요청하신 테마를 찾을 수 없습니다. 선택하신 테마가 정확한지 다시 한번 확인해 주세요.");
    }

    @Test
    void 이미_지난_시간_날짜_예약_시_예외() {
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 5, 4), 1L, 1L);
        ReservationTime mockTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme mockTheme = new Theme(1L, "테마1", "설명", "url");

        when(reservationTimeDao.findTimeById(1L)).thenReturn(mockTime);
        when(themeDao.findThemeById(1L)).thenReturn(mockTheme);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 지난 시간/날짜는 예약할 수 없습니다.");
    }
}
