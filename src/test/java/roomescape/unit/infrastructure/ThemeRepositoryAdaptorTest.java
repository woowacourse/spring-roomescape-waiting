package roomescape.unit.infrastructure;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;
import roomescape.infrastructure.JpaThemeRepository;
import roomescape.infrastructure.ThemeRepositoryAdaptor;

@DataJpaTest
@Sql(value = "/sql/testTheme.sql")
class ThemeRepositoryAdaptorTest {

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    private ThemeRepositoryAdaptor themeRepositoryAdaptor;

    private List<Theme> themes;

    @BeforeEach
    void setUp() {
        themeRepositoryAdaptor = new ThemeRepositoryAdaptor(jpaThemeRepository);
        themes = themeRepositoryAdaptor.findAll();
    }

    @Test
    void findAll() {
        assertThat(themes).hasSize(5);
    }

    @Test
    void findByName() {
        //given
        String themeName = "테마1";

        //when & then
        Theme theme = themeRepositoryAdaptor.findByName(themeName).get();
        assertThat(theme.getName()).isEqualTo(themeName);
    }

    @Test
    void findById() {
        //given
        Long themeId = getFirstId();

        //when & then
        Optional<Theme> theme = themeRepositoryAdaptor.findById(themeId);
        assertThat(theme).isPresent();
    }

    @Test
    void deleteById() {
        //given
        Long themeId = getFirstId();
        themeRepositoryAdaptor.deleteById(themeId);
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

    private Long getFirstId() {
        return themes.getFirst().getId();
    }
}
