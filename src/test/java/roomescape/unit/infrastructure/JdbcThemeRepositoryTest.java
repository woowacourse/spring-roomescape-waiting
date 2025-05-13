package roomescape.unit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.infrastructure.JdbcThemeRepository;

@JdbcTest
public class JdbcThemeRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private final ThemeRepository themeRepository;

    @Autowired
    public JdbcThemeRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeRepository = new JdbcThemeRepository(jdbcTemplate);
    }

    @Test
    void 테마를_추가할_수_있다() {
        // given
        Theme theme = Theme.createWithoutId("레벨2", "탈출하자",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        // when
        Theme createdTheme = themeRepository.save(theme);
        // then
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM theme", Integer.class);
        assertThat(count).isEqualTo(Integer.valueOf(1));
    }

    @Test
    void 전체_테마를_조회한다() {
        // given
        themeRepository.save(
                Theme.createWithoutId(
                        "레벨1",
                        "탈출하자",
                        "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
        );
        themeRepository.save(
                Theme.createWithoutId(
                        "레벨2",
                        "탈출하자",
                        "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
        );
        // when
        List<Theme> allTheme = themeRepository.findAll();
        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(allTheme).hasSize(2);
        soft.assertThat(allTheme.getFirst().getName()).isEqualTo("레벨1");
        soft.assertThat(allTheme.get(1).getName()).isEqualTo("레벨2");
        soft.assertAll();
    }

    @Test
    void 테마를_삭제한다() {
        // given
        Theme theme1 = themeRepository.save(
                Theme.createWithoutId(
                        "레벨2",
                        "탈출하자",
                        "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
        );
        Theme theme2 = themeRepository.save(
                Theme.createWithoutId(
                        "레벨1",
                        "탈출하자",
                        "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
        );
        // when
        themeRepository.deleteById(theme1.getId());
        // then
        List<Theme> allThemes = themeRepository.findAll();
        assertThat(allThemes).hasSize(1);
    }
}
