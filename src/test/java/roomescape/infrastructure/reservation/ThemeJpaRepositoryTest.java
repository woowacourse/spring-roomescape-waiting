package roomescape.infrastructure.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@DataJpaTest
class ThemeJpaRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @Sql("/insert-reservations.sql")
    @DisplayName("주어진 날짜 사이에 예약된 갯수를 기준으로 테마를 반환한다.")
    void shouldReturnPopularThemes() {
        LocalDate from = LocalDate.of(1999, 12, 24);
        LocalDate to = LocalDate.of(1999, 12, 29);
        int limit = 3;

        List<Long> themeIds = themeRepository.findPopularThemesDateBetween(from, to, limit)
                .stream()
                .map(Theme::getId)
                .toList();
        assertThat(themeIds).containsExactly(4L, 3L, 2L);
    }
}
