package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.testFixture.Fixture.resetH2TableIds;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.Theme;
import roomescape.infrastructure.jdbc.JdbcThemeRepository;

@JdbcTest
@Import(JdbcThemeRepository.class)
@ActiveProfiles("test")
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcThemeRepository jdbcThemeRepository;

    @BeforeEach
    void cleanDatabase() {
        resetH2TableIds(jdbcTemplate);
    }

    @DisplayName("모든 테마를 조회할 수 있다.")
    @Test
    void findAll() {
        // given
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES ('테마1', '테마 1입니다.', '썸네일입니다.')");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES ('테마2', '테마 2입니다.', '썸네일입니다.')");

        // when
        List<Theme> themes = jdbcThemeRepository.findAll();

        // then
        assertThat(themes).hasSize(2);
    }

    @DisplayName("테마 생성 후 id를 반환한다")
    @Test
    void save() {
        //given
        Theme themeWithoutId = Theme.withoutId("테마1", "테마 1입니다.", "썸네일");

        //when
        Long id = jdbcThemeRepository.save(themeWithoutId);

        //then
        assertThat(id).isEqualTo(1);
    }

    @DisplayName("id로 해당하는 테마를 삭제한다")
    @Test
    void delete() {
        //given
        Long themeId = 1L;
        jdbcTemplate.update("INSERT INTO theme (id, name, description, thumbnail) VALUES (?, ?, ?, ?)",
                themeId, "테마1", "테마 1입니다.", "썸네일입니다.");
        assertThat(getThemesCount()).isEqualTo(1);

        //when
        jdbcThemeRepository.deleteById(themeId);

        //then
        assertThat(getThemesCount()).isEqualTo(0);
    }

    private int getThemesCount() {
        int themesCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM theme", Integer.class);
        return themesCount;
    }
}
