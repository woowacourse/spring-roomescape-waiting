package roomescape.theme.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DuplicateException;
import roomescape.theme.application.dto.CreateThemeServiceRequest;
import roomescape.theme.application.service.ThemeCommandServiceImpl;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.domain.ThemeThumbnail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class ThemeCommandServiceImplTest {

    @Autowired
    private ThemeCommandServiceImpl themeCommandService;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("테마를 저장할 수 있다")
    void create() {
        // given
        final String name = "시소";
        final String description = "공포 방탈출 대표 테마";
        final String url = "https://www.naver.com";
        final CreateThemeServiceRequest request = new CreateThemeServiceRequest(
                ThemeName.from(name),
                ThemeDescription.from(description),
                ThemeThumbnail.from(url));

        // when
        final Theme theme = themeCommandService.create(request);

        // then
        final Theme foundTheme = themeRepository.findById(theme.getId())
                .orElseThrow();
        assertAll(() -> {
            assertThat(foundTheme.getName().getValue())
                    .isEqualTo(name);
            assertThat(foundTheme.getDescription().getValue())
                    .isEqualTo(description);
            assertThat(foundTheme.getThumbnail().getValue().toString())
                    .isEqualTo(url);
        });
    }

    @Test
    @DisplayName("존재하는 테마 이름으로 테마를 저장할 수 없다")
    void cannotCreateWithSameThemeName() {
        // given
        final String name = "이름이같다";
        final CreateThemeServiceRequest request1 = new CreateThemeServiceRequest(
                ThemeName.from(name),
                ThemeDescription.from("des"),
                ThemeThumbnail.from("uri"));

        final Theme theme = themeCommandService.create(request1);

        final CreateThemeServiceRequest request2 = new CreateThemeServiceRequest(
                ThemeName.from(name),
                ThemeDescription.from("des는 같아도 되고 달라도 되는 것"),
                ThemeThumbnail.from("uricansametoo"));

        // when
        // then
        assertThatThrownBy(() -> themeCommandService.create(request2))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("THEME already exists. params={ThemeName=ThemeName(value=이름이같다)}");
    }

    @Test
    @DisplayName("테마를 삭제할 수 있다")
    void delete() {
        // given
        final String name = "시소";
        final String description = "공포 방탈출 대표 테마";
        final String url = "https://www.naver.com";

        final Theme saved = themeRepository.save(Theme.withoutId(
                ThemeName.from(name),
                ThemeDescription.from(description),
                ThemeThumbnail.from(url)));

        // when
        themeCommandService.delete(saved.getId());

        // then
        assertThat(themeRepository.findById(saved.getId()).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("저장되지 않은 테마를 삭제할 수 없다")
    void cannotDelete() {
        // given
        final ThemeId unassigned = ThemeId.unassigned();

        // when
        // then
        assertThatThrownBy(() -> themeCommandService.delete(unassigned))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("식별자가 할당되지 않았습니다.");
    }
}
