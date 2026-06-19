package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;

@JdbcTest
class ThemeRepositoryTest {

    private JdbcThemeRepository jdbcThemeRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("등록된 테마가 여러개이면 조회 시 등록된 갯수만큼 반환한다.")
    void findAll() {
        // given
        List<Theme> themes = List.of(
                ThemeFixture.theme("테마1"),
                ThemeFixture.theme("테마2"),
                ThemeFixture.theme("테마3")
        );
        saveAll(themes);

        // when
        List<Theme> actual = jdbcThemeRepository.findAll();

        // then
        assertThat(actual)
                .hasSize(themes.size());
    }

    @Test
    @DisplayName("등록된 테마와 조회되는 테마의 모든 필드가 일치한다.")
    void findById() {
        // given
        Theme savedTheme = jdbcThemeRepository.save(ThemeFixture.theme());

        // when
        Theme actual = jdbcThemeRepository.findById(savedTheme.getId()).get();

        // then
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(savedTheme);
    }

    @Test
    @DisplayName("등록되지 않은 테마를 조회하면 빈 값을 반환한다.")
    void findById_wrongId() {
        // given
        Long wrongId = Long.MIN_VALUE;

        // when
        Optional<Theme> actual = jdbcThemeRepository.findById(wrongId);

        // then
        Assertions.assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("활성화된 테마 목록을 가나다순으로 조회한다.")
    void findByActive() {
        // given
        List<Theme> themes = saveAll(List.of(
                ThemeFixture.activeTheme("다테마"),
                ThemeFixture.activeTheme("나테마"),
                ThemeFixture.activeTheme("가테마"))
        );
        Collections.sort(themes, Comparator.comparing(Theme::getName));

        // when
        List<Theme> actual = jdbcThemeRepository.findByIsActive(true);

        // then
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(themes);
    }

    @Test
    @DisplayName("테마를 등록하면 가격이 그대로 저장되고 조회된다.")
    void save_and_find_amount() {
        // given
        Theme savedTheme = jdbcThemeRepository.save(ThemeFixture.theme());

        // when
        Theme actual = jdbcThemeRepository.findById(savedTheme.getId()).get();

        // then
        assertThat(actual.getAmount())
                .isEqualTo(savedTheme.getAmount());
    }

    @Test
    @DisplayName("테마를 1개 등록하면 테마 데이터 수가 1 증가한다.")
    void save() {
        // given
        List<Theme> themes = List.of();

        // when
        jdbcThemeRepository.save(ThemeFixture.theme());

        // then
        assertThat(jdbcThemeRepository.findAll())
                .hasSize(themes.size() + 1);
    }

    @Test
    @DisplayName("테마를 활성화한다.")
    void updateStatus_active() {
        // given
        Theme savedTheme = jdbcThemeRepository.save(ThemeFixture.theme());
        savedTheme.updateStatus(true);

        // when
        jdbcThemeRepository.updateStatus(savedTheme);

        // then
        assertThat(jdbcThemeRepository.findById(savedTheme.getId()).get().isActive())
                .isTrue();
    }


    @Test
    @DisplayName("테마를 비활성화한다.")
    void updateStatus_deactivate() {
        // given
        Theme savedTheme = jdbcThemeRepository.save(ThemeFixture.activeTheme());
        savedTheme.updateStatus(false);

        // when
        jdbcThemeRepository.updateStatus(savedTheme);

        // then
        assertThat(jdbcThemeRepository.findById(savedTheme.getId()).get().isActive())
                .isFalse();
    }

    private List<Theme> saveAll(List<Theme> themes) {
        List<Theme> savedThemes = new ArrayList<>();
        for (Theme theme : themes) {
            Theme savedTheme = jdbcThemeRepository.save(theme);
            savedThemes.add(savedTheme);
        }
        return savedThemes;
    }

}
