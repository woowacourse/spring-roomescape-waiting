package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import roomescape.common.config.FixedClockConfig;
import roomescape.common.exception.ConflictException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.dto.TimeQueryResult;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.service.dto.command.ThemeCommand;
import roomescape.service.dto.result.ReservationTimeDetailResult;
import roomescape.service.dto.result.ThemeResult;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {
    private final Theme theme1 = new Theme(1L, ThemeName.parse("테마1"), Description.parse("설명1"),
            ThumbnailUrl.parse("/images/url1"));
    private final Theme theme2 = new Theme(2L, ThemeName.parse("테마2"), Description.parse("설명2"),
            ThumbnailUrl.parse("/images/url2"));

    private ThemeService themeService;

    @Mock
    private ReservationDao reservationDao;
    @Mock
    private ThemeDao themeDao;
    @Mock
    private ReservationTimeDao reservationTimeDao;
    @Mock
    private FileUploader fileUploader;

    private final Clock clock = new FixedClockConfig().testClock();

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(reservationDao, themeDao, reservationTimeDao, fileUploader, clock);
    }

    @Test
    @DisplayName("테마 목록을 조회한다.")
    void findThemes_Success() {
        given(themeDao.findAllThemes()).willReturn(List.of(theme1, theme2));

        List<ThemeResult> themes = themeService.findThemes();

        assertThat(themes).hasSize(2);
        assertThat(themes.get(0).name()).isEqualTo("테마1");
        assertThat(themes.get(1).name()).isEqualTo("테마2");
    }

    @Test
    @DisplayName("인기 테마 목록을 조회한다.")
    void findPopularThemes_Success() {
        given(themeDao.findPopularThemes(any(Integer.class), any(LocalDate.class))).willReturn(List.of(theme1, theme2));

        List<ThemeResult> themes = themeService.findPopularThemes(10);

        assertThat(themes).hasSize(2);
        assertThat(themes.get(0).name()).isEqualTo(theme1.getName().value());
    }

    @Test
    @DisplayName("테마를 등록한다.")
    void registerTheme_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        ThemeCommand command = new ThemeCommand("새로운 테마", "설명", file);
        String imageUrl = "/images/test.jpg";
        Theme savedTheme = new Theme(1L, ThemeName.parse(command.name()), Description.parse(command.description()),
                ThumbnailUrl.parse(imageUrl));

        given(fileUploader.upload(file)).willReturn(imageUrl);
        given(themeDao.save(any(Theme.class))).willReturn(savedTheme);

        ThemeResult result = themeService.registerTheme(command);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("새로운 테마");
        assertThat(result.url()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("예약이 없는 테마를 삭제한다.")
    void deleteTheme_Success() {
        Long themeId = 1L;
        given(reservationDao.existsByThemeId(themeId)).willReturn(false);

        assertDoesNotThrow(() -> themeService.deleteTheme(themeId));
    }

    @Test
    @DisplayName("예약이 있는 테마를 삭제하면 예외가 발생한다.")
    void deleteTheme_WhenExistingReservation_ThrowsConflictException() {
        Long themeId = 1L;
        given(reservationDao.existsByThemeId(themeId)).willReturn(true);

        assertThatThrownBy(() -> themeService.deleteTheme(themeId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("예약이 존재하는 테마는 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("특정 날짜의 테마 스케줄을 조회한다.")
    void findThemeSchedulesByDate_Success() {
        Long themeId = 1L;
        LocalDate date = LocalDate.now();
        List<TimeQueryResult> timeQueryResults = List.of(
                new TimeQueryResult(new ReservationTime(1L, LocalTime.parse("10:00")), true),
                new TimeQueryResult(new ReservationTime(2L, LocalTime.parse("11:00")), false)
        );
        given(reservationTimeDao.findStatuesByThemeIdAndDate(themeId, date)).willReturn(timeQueryResults);

        List<ReservationTimeDetailResult> schedules = themeService.findThemeSchedulesByDate(themeId, date);

        assertThat(schedules).hasSize(2);
        assertThat(schedules.get(0).timeResult().startAt()).isEqualTo("10:00");
        assertThat(schedules.get(0).isReserved()).isTrue();
        assertThat(schedules.get(1).timeResult().startAt()).isEqualTo("11:00");
        assertThat(schedules.get(1).isReserved()).isFalse();
    }
}
