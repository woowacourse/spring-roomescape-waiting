package roomescape.domain.theme.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.RepositoryTest;
import roomescape.domain.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ThemeRepositoryTest extends RepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @MethodSource
    static Stream<Arguments> themeProvider() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 5, 9), LocalDate.of(2024, 5, 10), List.of(4L)),
                Arguments.of(LocalDate.of(2024, 5, 10), LocalDate.of(2024, 5, 12), List.of(3L, 4L)),
                Arguments.of(LocalDate.of(2024, 5, 11), LocalDate.of(2024, 5, 13), List.of(3L, 2L)),
                Arguments.of(LocalDate.of(2024, 5, 13), LocalDate.of(2024, 5, 13), List.of(2L)),
                Arguments.of(LocalDate.of(2024, 5, 14), LocalDate.of(2024, 5, 17), List.of(1L))
        );
    }

    @ParameterizedTest
    @MethodSource("themeProvider")
    @DisplayName("인기 테마를 알 수 있습니다.")
    void findThemeOrderByReservationCountTest(LocalDate startDate, LocalDate endDate, List<Long> expected) {
        List<Theme> themes = themeRepository.findThemeOrderByReservationCount(startDate, endDate);

        List<Long> actual = themes.stream()
                .map(Theme::getId)
                .toList();

        assertThat(actual).isEqualTo(expected);
    }
}
