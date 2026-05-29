package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static roomescape.config.FixedClockConfig.TODAY;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import roomescape.common.exception.ConflictException;
import roomescape.config.FixedClockConfig;
import roomescape.dao.ThemeDao;
import roomescape.domain.reservation.theme.Description;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.theme.ThemeName;
import roomescape.domain.reservation.theme.ThumbnailUrl;
import roomescape.service.dto.command.ThemeCommand;
import roomescape.service.dto.result.ThemeResult;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {
    private final Long themeId = 1L;
    private final String themeNameValue = "저주받은 저택";
    private final String descriptionValue = "100년 전 사라진 가문의 비밀을 파헤쳐라";

    private final Clock fixedClock = new FixedClockConfig().testClock();

    private ThemeService themeService;

    @Mock
    private ThemeDao themeDao;
    @Mock
    private MultipartFile file;

    @BeforeEach
    public void setUp() {
        themeService = new ThemeService(themeDao, fixedClock);
    }

    @Test
    public void 테마_생성_정상_테스트() {
        given(themeDao.existsByName(themeNameValue)).willReturn(false);
        given(file.getOriginalFilename()).willReturn("cursed.jpg");
        Theme saved = new Theme(
                themeId,
                ThemeName.parse(themeNameValue),
                Description.parse(descriptionValue),
                ThumbnailUrl.parse("/images/uuid_cursed.jpg")
        );
        given(themeDao.save(any())).willReturn(saved);

        ThemeCommand command = new ThemeCommand(themeNameValue, descriptionValue, file);
        ThemeResult result = themeService.createTheme(command);

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.name()).isEqualTo(saved.getName().value());
        assertThat(result.description()).isEqualTo(saved.getDescription().value());
        assertThat(result.url()).isEqualTo(saved.getUrl().value());

        verify(themeDao).save(argThat(theme ->
                theme.getName().value().equals(themeNameValue)
                        && theme.getDescription().value().equals(descriptionValue)
                        && theme.getUrl().value().endsWith("_cursed.jpg")
        ));
    }

    @Test
    public void 중복된_이름의_테마_생성_시_예외_테스트() {
        given(themeDao.existsByName(themeNameValue)).willReturn(true);

        ThemeCommand command = new ThemeCommand(themeNameValue, descriptionValue, file);

        assertThatThrownBy(() -> themeService.createTheme(command))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 존재하는 테마입니다.");

        verify(themeDao, never()).save(any());
    }

    @Test
    public void 인기_테마_조회_정상_테스트() {
        Long count = 10L;
        given(themeDao.findTopThemes(count, LocalDate.parse(TODAY))).willReturn(List.of());

        themeService.findTopTheme(count);

        verify(themeDao).findTopThemes(count, LocalDate.parse(TODAY));
    }

    @Test
    public void 테마_삭제_정상_테스트() {
        Theme existing = new Theme(
                themeId,
                ThemeName.parse(themeNameValue),
                Description.parse(descriptionValue),
                ThumbnailUrl.parse("/images/cursed.jpg")
        );
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(existing));

        themeService.deleteTheme(themeId);

        verify(themeDao).delete(themeId);
    }
}