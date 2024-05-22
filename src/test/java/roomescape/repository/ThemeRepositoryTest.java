package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("인기테마 조회 테스트")
    @Sql(value = "/init_data/reservationData.sql", executionPhase = BEFORE_TEST_METHOD)
    @Test
    void findTopReservedThemesByDateRangeAndLimit() {
        LocalDate startDate = LocalDate.parse("2024-04-25");
        LocalDate endDate = LocalDate.parse("2024-05-30");

        List<Theme> themes = themeRepository.findTopReservedThemesByDateRangeAndLimit(startDate, endDate, 2);

        assertThat(themes).hasSize(2)
                .extracting("id", "name")
                .containsExactly(
                        tuple(12L, "hi12"),
                        tuple(11L, "hi11")
                );
    }
}