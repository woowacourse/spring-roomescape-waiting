package roomescape.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;

@DataJpaTest
@Sql(value = "/sql/testTheme.sql")
class ThemeRepositoryAdaptorTest {

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    private ThemeRepositoryAdaptor themeRepositoryAdaptor;

    @BeforeEach
    void setUp() {
        themeRepositoryAdaptor = new ThemeRepositoryAdaptor(jpaThemeRepository);
    }

    @Test
    void findAll() {
        //given
        List<Theme> themes = themeRepositoryAdaptor.findAll();

        //when & then
        assertThat(themes.size()).isEqualTo(5);
    }

    @Test
    void findByName() {
        //given
        String themeName = "테마1";
        Theme theme = themeRepositoryAdaptor.findByName(themeName).get();

        //when & then
        assertThat(theme.getName()).isEqualTo(themeName);
    }

    @Test
    void findById() {
        //given
        Long id = 1L;
        Theme theme = themeRepositoryAdaptor.findById(id).get();

        //when & then
        assertThat(theme.getId()).isEqualTo(id);
    }

    @Test
    void deleteById() {
        //given
        Long id = 1L;
        themeRepositoryAdaptor.deleteById(id);
        List<Theme> themes = themeRepositoryAdaptor.findAll();

        //when & then
        assertThat(themes.size()).isEqualTo(4);
    }

    @Test
    void save() {
        //given
        Theme theme = Theme.createWithoutId("테마6", "설명6", "섬네일6");
        themeRepositoryAdaptor.save(theme);
        List<Theme> themes = themeRepositoryAdaptor.findAll();

        //when & then
        assertThat(themes.size()).isEqualTo(6);
    }
}
