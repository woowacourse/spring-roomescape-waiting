package roomescape.theme.repository;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;

@DataJpaTest
@Sql("/data.sql")
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository repository;

    @Test
    void 인기있는_테마들을_조회한다() {
        // given
        // when
        final List<Theme> themes = repository.findAllPopular(LocalDate.of(2999, 5, 1), LocalDate.of(2999, 5, 5));

        // then
        assertThat(themes).hasSize(2);
    }
}
