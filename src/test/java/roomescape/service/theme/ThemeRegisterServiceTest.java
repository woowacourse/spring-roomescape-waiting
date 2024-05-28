package roomescape.service.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.theme.Theme;
import roomescape.dto.theme.ThemeRequest;
import roomescape.repository.ThemeRepository;
import roomescape.service.theme.module.ThemeRegisterService;

@Sql("/all-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ThemeRegisterServiceTest {

    @Autowired
    ThemeRegisterService themeRegisterService;

    @Autowired
    ThemeRepository themeRepository;

    @Test
    void 테마_등록() {
        //given
        ThemeRequest themeRequest = new ThemeRequest("테마명", "테마설명테마설명테마설명", "썸네일명");

        //when
        Long themeId = themeRegisterService.resisterTheme(themeRequest);

        //then
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        assertAll(
                () -> assertThat(theme.getThemeName()).isEqualTo(themeRequest.name()),
                () -> assertThat(theme.getDescription()).isEqualTo(themeRequest.description()),
                () -> assertThat(theme.getThumbnail()).isEqualTo(themeRequest.thumbnail())
        );
    }

    @Test
    void 동일한_이름의_테마를_등록할_경우_예외_발생() {
        //given
        ThemeRequest themeRequest = new ThemeRequest("테마1", "테마설명테마설명테마설명", "썸네일명");

        //when, then
        assertThatThrownBy(() -> themeRegisterService.resisterTheme(themeRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
