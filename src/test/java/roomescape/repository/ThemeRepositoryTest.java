package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.theme.Theme;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("성공: 최근 일주일 내 인기 테마를 조회한다.")
    @Test
    void findPopular() {
        LocalDate start = LocalDate.now().minusDays(8);
        LocalDate end = LocalDate.now().minusDays(1);
        List<Theme> themes = themeRepository.findPopular(start, end, 10);

        Assertions.assertAll(
            () -> assertThat(themes.get(0).getId()).isEqualTo(1L),
            () -> assertThat(themes.get(1).getId()).isEqualTo(3L),
            () -> assertThat(themes.get(2).getId()).isEqualTo(2L)
        );
    }
}
