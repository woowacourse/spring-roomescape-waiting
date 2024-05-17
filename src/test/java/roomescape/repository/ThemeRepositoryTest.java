package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.Theme;

@DataJpaTest
@Sql(value = "classpath:test-db-clean.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("상위 10일 간 가장 많이 예약 된 테마 10개를 가져온다.")
    @Test
    void findThemesWithReservationsBetweenDates() {
        //given
        LocalDate now = LocalDate.now();
        LocalDate dateFrom = now.minusWeeks(1);
        LocalDate dateTo = now.minusDays(1);

        //when
        List<Theme> results =
                themeRepository.findThemesWithReservationsBetweenDates(dateFrom, dateTo);
        Theme firstTheme = results.get(0);
        Theme secondTheme = results.get(1);
        Theme thirdTheme = results.get(2);
        Theme fourthTheme = results.get(3);
        /*
         *   테마 통계
         *   5번 방탈출 - 5개
         *   4번 방탈출 - 4개
         *   3번 방탈출 - 3개
         *   2번 방탈출 - 2개
         *   1번 방탈출 - 1개
         * */
        //then
        assertAll(
                () -> assertThat(results).hasSize(10),
                () -> assertThat(firstTheme.getId()).isEqualTo(5L),
                () -> assertThat(secondTheme.getId()).isEqualTo(4L),
                () -> assertThat(thirdTheme.getId()).isEqualTo(3L),
                () -> assertThat(fourthTheme.getId()).isEqualTo(2L)
        );
    }
}
