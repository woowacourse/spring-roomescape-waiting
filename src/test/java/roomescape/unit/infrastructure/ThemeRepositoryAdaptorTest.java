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
    void 전체_테마_조회_테스트() {
        assertThat(themes).hasSize(5);
    }

    @Test
    void 이름으로_테마_조회_테스트() {
        //given
        String themeName = "테마1";

        //when & then
        Theme theme = themeRepositoryAdaptor.findByName(themeName).get();

        assertThat(theme.getName()).isEqualTo(themeName);
    }

    @Test
    void id로_테마_조회_테스트() {
        //given
        Long themeId = getFirstId();

        //when & then
        Optional<Theme> theme = themeRepositoryAdaptor.findById(themeId);

        assertThat(theme).isPresent();
    }

    @Test
    void id로_테마_삭제_테스트() {
        //given
        Long themeId = getFirstId();

        //when & then
        themeRepositoryAdaptor.deleteById(themeId);
        List<Theme> themes = themeRepositoryAdaptor.findAll();

        assertThat(themes.size()).isEqualTo(4);
    }

    @Test
    void 테마_저장_테스트() {
        //given
        Theme theme = Theme.createWithoutId("테마6", "설명6", "섬네일6");

        //when & then
        themeRepositoryAdaptor.save(theme);
        List<Theme> themes = themeRepositoryAdaptor.findAll();

        assertThat(themes.size()).isEqualTo(6);
    }

    private Long getFirstId() {
        return themes.getFirst().getId();
    }
}
