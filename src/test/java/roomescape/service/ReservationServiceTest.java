package roomescape.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("존재하지 않는 timeId로 예약하면 예외가 발생한다")
    void save_fail_invalid_time_id() {
        // given
        Long invalidTimeId = 999L;
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 5, 4), invalidTimeId, 1L);

        // when & then
        when(reservationTimeDao.findTimeById(invalidTimeId)).thenReturn(null);
        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IdNotFoundException.class)
                .hasMessageContaining("요청하신 시간 ID가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 themeId로 예약하면 예외가 발생한다")
    void save_fail_invalid_theme_id() {
        // given
        Long invalidThemeId = 999L;
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 5, 4), 1L, invalidThemeId);

        // when & then
        when(reservationTimeDao.findTimeById(1L)).thenReturn(new ReservationTime(1L, LocalTime.of(10, 0)));
        when(themeDao.findThemeById(invalidThemeId)).thenReturn(null);
        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IdNotFoundException.class)
                .hasMessageContaining("요청하신 테마 ID가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("이미 지난 시간/날짜로 예약하면 예외가 발생한다")
    void avoid_invalid_time_date() {
        // given
        ReservationRequest request = new ReservationRequest("아나키", LocalDate.of(2026, 5, 4), 1L, 1L);
        ReservationTime mockTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme mockTheme = new Theme(1L, "테마1", "설명", "url");

        // when & then
        when(reservationTimeDao.findTimeById(1L)).thenReturn(mockTime);
        when(themeDao.findThemeById(1L)).thenReturn(mockTheme);

        assertThatThrownBy(() -> reservationService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 지난 시간/날짜는 예약할 수 없습니다.");
    }
}
