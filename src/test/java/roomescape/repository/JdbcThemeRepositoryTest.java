package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;
import roomescape.domain.Theme;

@JdbcTest
@Import({
        JdbcThemeRepository.class,
        TestTimeConfig.class
})
class JdbcThemeRepositoryTest {

    @Autowired
    Clock clock;

    @Autowired
    ThemeRepository themeRepository;

    @Test
    void 테마를_저장하면_id를_부여한다() {
        // given
        Theme theme = Theme.create("귀신찾기", "귀신을 찾는다", "example.com");

        // when
        Theme saved = themeRepository.save(theme);

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 테마를_id로_조회한다() {
        // given
        Theme saved = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "example.com")
        );

        // when
        Optional<Theme> result = themeRepository.findById(saved.getId());

        // then
        assertThat(result)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    void 저장된_모든_테마를_조회한다() {
        // given
        Theme savedHorror = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "example.com")
        );
        Theme savedSuspect = themeRepository.save(
                Theme.create("추리", "추리한다", "example.com")
        );

        // when
        List<Theme> foundThemes = themeRepository.findAll();

        // then
        assertThat(foundThemes)
                .hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        savedHorror,
                        savedSuspect
                );
    }

    @Test
    @Sql("/data.sql")
    void 최근_1주_동안의_예약_상위_10개의_테마를_조회한다() {
        List<Long> popularThemes = themeRepository.findPopularThemes(
                LocalDate.now(clock).minusWeeks(1),
                LocalDate.now(clock),
                10L
        ).stream().map(Theme::getId).collect(Collectors.toList());

        assertThat(popularThemes)
                .containsExactly(
                        1L, 2L, 3L, // 1순위: 테마의 예약 수 내림차순 정렬
                        6L, 5L, 4L, 8L, 7L, // 2순위: 예약 수가 같으면 테마 이름 오름차순 정렬
                        10L, 9L // 예약 개수가 0개여도, 상위 10위 이내라면 조회되어야 함 (예약 개수 0개인 테마들은 2순위 정렬 기준으로 비교)
                );
    }

    @Test
    void 테마를_삭제한다() {
        // given
        Theme savedHorror = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "example.com")
        );

        // when
        themeRepository.delete(savedHorror.getId());
        Optional<Theme> result = themeRepository.findById(savedHorror.getId());

        // then
        assertThat(result).isNotPresent();
    }
}
