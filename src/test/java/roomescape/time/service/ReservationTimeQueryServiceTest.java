package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.exception.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

@ExtendWith(MockitoExtension.class)
class ReservationTimeQueryServiceTest {

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeService themeService;

    @InjectMocks
    private ReservationTimeQueryService reservationTimeQueryService;

    @Test
    @DisplayName("모든 예약 시간 목록을 성공적으로 조회한다.")
    void findAll_success() {
        // given
        ReservationTime time1 = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(2L, LocalTime.of(13, 0));
        given(reservationTimeRepository.findAll()).willReturn(List.of(time1, time2));

        // when
        reservationTimeQueryService.findAll();

        // then
        then(reservationTimeRepository).should().findAll();
    }

    @Test
    @DisplayName("ID에 해당하는 예약 시간 정보를 성공적으로 조회한다.")
    void findById_success() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        given(reservationTimeRepository.findById(1L)).willReturn(java.util.Optional.of(time));

        // when
        reservationTimeQueryService.findById(1L);

        // then
        then(reservationTimeRepository).should().findById(1L);
    }

    @Test
    @DisplayName("예약 가능한 시간 목록을 성공적으로 조회한다.")
    void findAvailableTimes_success() {
        // given
        Theme theme = new Theme(1L, "테마", "설명", "url");
        given(themeService.findById(1L)).willReturn(theme);

        AvailableTimeQueryResult queryResult = new AvailableTimeQueryResult(1L, LocalTime.of(10, 0), false);
        given(reservationTimeRepository.findAvailableTimes(1L, LocalDate.of(2026, 5, 5)))
                .willReturn(List.of(queryResult));

        // when
        reservationTimeQueryService.findAvailableTimes(1L, LocalDate.of(2026, 5, 5));

        // then
        then(themeService).should().findById(1L);
        then(reservationTimeRepository).should().findAvailableTimes(1L, LocalDate.of(2026, 5, 5));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 시, 테마가 존재하지 않으면 예외가 발생한다.")
    void findAvailableTimes_themeNotFound() {
        // given
        given(themeService.findById(1L))
                .willThrow(new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage()));

        // when & then
        assertThatThrownBy(() -> reservationTimeQueryService.findAvailableTimes(1L, LocalDate.of(2026, 5, 5)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
    }
}
