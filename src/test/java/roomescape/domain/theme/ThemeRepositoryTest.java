package roomescape.domain.theme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ThemeRepositoryTest {
    @Autowired
    ThemeRepository themeRepository;

    @DisplayName("예약 기간 내에 예약된 테마를 예약이 많은 순으로 지정된 개수만큼 조회한다.")
    @Test
    @SqlGroup({
            @Sql(value = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD),
            @Sql("/insert-popular-theme.sql")
    })
    void findByReservationTermAndLimit() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(2);
        LocalDate startDate = today.minusDays(8);
        int limit = 2;

        // when
        List<Theme> themes = themeRepository.findByReservationTermAndLimit(startDate, endDate, limit);
        List<Long> popularThemeIds = themes.stream()
                .map(Theme::getId)
                .toList();

        // then
        assertThat(popularThemeIds).containsExactly(5L, 2L);
    }
}
