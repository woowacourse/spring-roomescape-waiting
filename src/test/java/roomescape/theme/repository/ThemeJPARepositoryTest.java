package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql({"/test-theme-data.sql"})
public class ThemeJPARepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("모든 테마를 조회할 수 있다.")
    @Test
    void testFindAll() {
        // given
        // when
        List<Theme> themes = themeRepository.findAll();
        // then
        assertThat(themes).hasSize(3);
    }

    @DisplayName("해당 테마가 존재하는 지 확인할 수 있다.")
    @Test
    void testExistByName() {
        // given
        // when
        // then
        assertThat(themeRepository.existsByName(new ThemeName("테마1"))).isTrue();
        assertThat(themeRepository.existsByName(new ThemeName("테스트"))).isFalse();
    }
}

