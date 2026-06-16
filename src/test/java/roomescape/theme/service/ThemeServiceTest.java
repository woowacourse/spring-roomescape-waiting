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
import org.junit.jupiter.api.Test;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.fixture.FakeThemeRepository;
import roomescape.theme.fixture.ThemeFixture;

class ThemeServiceTest {

    private final String name = "테마1";
    private final String description = "테마1 설명";
    private final String thumbnail = "테마1 썸네일";
    private final Long amount = 1000L;

    private ThemeService themeService;
    private FakeThemeRepository themeRepository;

    @BeforeEach
    void setup() {
        this.themeRepository = new FakeThemeRepository();
        this.themeService = new ThemeService(themeRepository);
    }

    @Test
    @DisplayName("등록된 테마가 여러개이면 조회 시 등록된 갯수만큼 반환한다.")
    void readThemes() {
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

    @Test
    @DisplayName("등록된 테마와 조회되는 테마의 모든 필드가 일치한다.")
    void readTheme() {
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
    @DisplayName("등록되지 않은 테마 조회 시 예외가 발생한다.")
    void readTheme_unregistered() {
        // given
        Long unregisteredId = Long.MIN_VALUE;

        // when & then
        assertThatThrownBy(() -> themeService.readTheme(unregisteredId))
                .isInstanceOf(ThemeException.class)
                .hasMessage(THEME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("활성화된 테마 목록을 가나다순으로 조회한다.")
    void readActiveThemes() {
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

    @Test
    @DisplayName("테마를 1개 등록하면 테마 데이터 수가 1 증가한다.")
    void register() {
        // when
        themeService.register(name, description, thumbnail, amount);

        // then
        assertThat(themeService.readThemes())
                .hasSize(1);
    }

    @Test
    @DisplayName("등록한 테마와 다시 조회한 테마의 모든 필드가 일치한다.")
    void register_theme_fields_match() {
        // when
        Theme registeredTheme = themeService.register(name, description, thumbnail, amount);

        // then
        assertThat(registeredTheme)
                .usingRecursiveComparison()
                .isEqualTo(themeService.readTheme(registeredTheme.getId()));
    }

    @Test
    @DisplayName("테마를 등록하면 가격이 그대로 저장된다.")
    void register_with_amount() {
        // when
        Theme registeredTheme = themeService.register(name, description, thumbnail, amount);

        // then
        assertThat(registeredTheme.getAmount())
                .isEqualTo(amount);
    }

    @Test
    @DisplayName("테마를 활성화한다.")
    void updateStatus_active() {
        // given
        Theme savedTheme = themeRepository.save(ThemeFixture.theme());

        // when
        themeService.updateStatus(savedTheme.getId(), true);

        // then
        assertThat(themeRepository.findById(savedTheme.getId()).get().isActive())
                .isTrue();
    }


    @Test
    @DisplayName("테마를 비활성화한다.")
    void updateStatus_deactivate() {
        // given
        Theme savedTheme = themeRepository.save(ThemeFixture.activeTheme());

        // when
        themeService.updateStatus(savedTheme.getId(), false);

        // then
        assertThat(themeRepository.findById(savedTheme.getId()).get().isActive())
                .isFalse();
    }

    private List<Theme> saveAll(List<Theme> themes) {
        List<Theme> savedThemes = new ArrayList<>();
        for (Theme theme : themes) {
            savedThemes.add(themeRepository.save(theme));
        }
        return savedThemes;
    }

}
