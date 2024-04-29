package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.theme.Theme;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class JdbcThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    private final String name1 = "theme1";
    private final String description1 = "desc1";
    private final String thumbnail1 = "https://thumbnail1";
    private final Theme theme1 = new Theme(name1, description1, thumbnail1);

    private final String name2 = "theme2";
    private final Theme theme2 = new Theme(name2, "desc2", "https://thumbnail2");

    @DisplayName("테마 정보를 DB에 저장한다.")
    @Test
    void save() {
        Theme saved = themeRepository.save(theme1);
        assertThat(saved).isEqualTo(new Theme(saved.getId(), name1, description1, thumbnail1));
    }

    @DisplayName("전체 테마 정보를 불러온다.")
    @Test
    void findAll() {
        Theme saved1 = themeRepository.save(theme1);
        Theme saved2 = themeRepository.save(theme2);

        assertThat(themeRepository.findAll()).containsExactly(saved1, saved2);
    }

    @DisplayName("ID를 받아와 테마를 DB에서 삭제한다.")
    @Test
    void deleteById() {
        Theme saved1 = themeRepository.save(theme1);
        Theme saved2 = themeRepository.save(theme2);

        themeRepository.deleteById(saved1.getId());

        assertThat(themeRepository.findAll()).containsExactly(saved2);
    }

    @DisplayName("테마 id로부터 테마를 찾는다.")
    @Test
    void findById() {
        Theme saved = themeRepository.save(theme1);

        assertThat(themeRepository.findById(saved.getId())).isEqualTo(saved);
    }

    @DisplayName("테마 이름을 받아서 존재 여부를 판단한다.")
    @Test
    void isNameExists() {
        themeRepository.save(theme1);
        themeRepository.save(theme2);

        assertAll(
            () -> assertThat(themeRepository.isExists(name1)).isTrue(),
            () -> assertThat(themeRepository.isExists(name2)).isTrue(),
            () -> assertThat(themeRepository.isExists("bad_name")).isFalse()
        );
    }

    @DisplayName("최근 일주일 내의 인기 테마를 찾는다.")
    @Test
    @Sql(value = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    void findPopular() {
        List<Theme> expected = List.of(
            new Theme(1L, "theme1", "desc1", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(3L, "theme3", "desc3", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(2L, "theme2", "desc2", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
        );

        assertThat(
            themeRepository.findPopular(8, 1, 10)
        ).containsExactlyElementsOf(expected);
    }
}
