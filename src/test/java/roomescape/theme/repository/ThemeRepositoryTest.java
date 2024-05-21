package roomescape.theme.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ThemeRepositoryTest {

    @Autowired
    ThemeRepository themeRepository;

    @DisplayName("name nullable false 테스트")
    @Test
    void nameNullableFalseTest() {
        Theme theme = new Theme(
                null,
                null,
                "description",
                "http://www.thumbnail.com"
                );

        assertThatThrownBy(() -> themeRepository.save(theme))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
