package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.theme.exception.ThemeErrorInformation.THEME_NOT_FOUND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest(showSql = false)
class ThemeServiceTest {

    private final String name = "테마1";
    private final String description = "테마1 설명";
    private final String thumbnail = "테마1 썸네일";

    @Autowired
    private ThemeRepository themeRepository;
    private ThemeService themeService;

    @BeforeEach
    void setup() {
        this.themeService = new ThemeService(themeRepository);
    }

    private List<Theme> saveAll(List<Theme> themes) {
        List<Theme> savedThemes = new ArrayList<>();
        for (Theme theme : themes) {
            savedThemes.add(themeRepository.save(theme));
        }
        return savedThemes;
    }


    @Nested
    @DisplayName("readThemes 메서드는")
    class ReadThemesTest {


        @Test
        @DisplayName("모든 테마를 가져온다")
        void 성공() {
            // given
            List<Theme> themes = List.of(
                ThemeFixture.theme("테마1"),
                ThemeFixture.theme("테마2"),
                ThemeFixture.theme("테마3")
            );
            themeRepository.saveAll(themes);

            // when
            List<Theme> actual = themeService.readThemes();

            // then
            assertThat(actual).hasSize(themes.size());
        }
    }

    @Nested
    @DisplayName("readTheme 메서드는")
    class ReadThemeTest {


        @Test
        @DisplayName("한 테마를 가져온다")
        void 성공() {
            // given
            Theme savedTheme = themeRepository.save(ThemeFixture.theme());

            // when
            Theme actual = themeService.readTheme(savedTheme.getId());

            // then
            assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(savedTheme);
        }


        @Test
        @DisplayName("등록되지 않은 테마를 가져오려고 하면 예외가 발생한다")
        void 실패() {
            // given
            Long unregisteredId = Long.MIN_VALUE;

            // when & then
            assertThatThrownBy(() -> themeService.readTheme(unregisteredId))
                .isInstanceOf(ThemeException.class)
                .hasMessage(THEME_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("readActiveThemes 메서드는")
    class ReadActiveThemesTest {


        @Test
        @DisplayName("활성화된 테마를 가져온다")
        void 성공() {
            // given
            List<Theme> themes = saveAll(List.of(
                ThemeFixture.activeTheme("다테마"),
                ThemeFixture.activeTheme("나테마"),
                ThemeFixture.activeTheme("가테마"))
            );
            Collections.sort(themes, Comparator.comparing(Theme::getName));

            // when
            List<Theme> actual = themeService.readActiveThemes();

            // then
            assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(themes);
        }
    }

    @Nested
    @DisplayName("register 메서드는")
    class RegisterTest {


        @Test
        @DisplayName("테마를 등록한다")
        void 성공1() {
            // when
            themeService.register(name, description, thumbnail);

            // then
            assertThat(themeService.readThemes())
                .hasSize(1);
        }


        @Test
        @DisplayName("등록된 테마와 조회한 테마는 동등하다")
        void 성공2() {
            // when
            Theme registeredTheme = themeService.register(name, description, thumbnail);

            // then
            assertThat(registeredTheme)
                .usingRecursiveComparison()
                .isEqualTo(themeService.readTheme(registeredTheme.getId()));
        }
    }

    @Nested
    @DisplayName("updateStatus 메서드는")
    class UpdateStatusTest {


        @Test
        @DisplayName("활성 상태로 변경한다")
        void 성공1() {
            // given
            Theme savedTheme = themeRepository.save(ThemeFixture.theme());

            // when
            themeService.updateStatus(savedTheme.getId(), true);

            // then
            assertThat(themeRepository.findById(savedTheme.getId()).get().isActive())
                .isTrue();
        }


        @Test
        @DisplayName("비활성 상태로 변경한다")
        void 성공2() {
            // given
            Theme savedTheme = themeRepository.save(ThemeFixture.activeTheme());

            // when
            themeService.updateStatus(savedTheme.getId(), false);

            // then
            assertThat(themeRepository.findById(savedTheme.getId()).get().isActive())
                .isFalse();
        }
    }
}
