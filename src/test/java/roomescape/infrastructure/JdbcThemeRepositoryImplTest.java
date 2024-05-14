package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

@JdbcTest
class JdbcThemeRepositoryImplTest {

    private ThemeRepository themeRepository;

    @Autowired
    public JdbcThemeRepositoryImplTest(JdbcTemplate jdbcTemplate) {
        this.themeRepository = new JdbcThemeRepositoryImpl(jdbcTemplate);
    }


    @DisplayName("id값이 db에 존재하지 않으면 optional.empty를 반환한다.")
    @Test
    void findById_NoSuchId() {
        Optional<Theme> actual = themeRepository.findById(1L);

        assertThat(actual).isEqualTo(Optional.empty());
    }

    @DisplayName("테마 이름을 받아서 존재 여부를 판단한다.")
    @Test
    void isNameExists() {
        String name = "방탈출1";
        Theme theme = new Theme(name, "방탈출1을 한다.", "https://url1");
        themeRepository.save(theme);
        assertThat(themeRepository.isNameExists(name)).isTrue();
    }

    @DisplayName("최근 일주일 내의 인기 테마를 찾는다.")
    @Test
    @Sql("/popularTestData.sql")
    void findPopular() {
        List<Theme> actual = themeRepository.findMostReservedThemesInPeriod(7, 10);
        List<Theme> expected = List.of(
            new Theme(1L, "theme1", "desc1", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(3L, "theme3", "desc3", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(2L, "theme2", "desc2", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
        );

        assertAll(
            () -> assertEquals(expected.get(0).getId(), actual.get(0).getId()),
            () -> assertEquals(expected.get(1).getId(), actual.get(1).getId()),
            () -> assertEquals(expected.get(2).getId(), actual.get(2).getId())
        );
    }
}
