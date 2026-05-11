package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@JdbcTest
@Import(JdbcThemeRepository.class)
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcThemeRepository jdbcThemeRepository;

    @Test
    @DisplayName("Theme를 저장하고 조회한다.")
    public void saveAndFindById() {
        Theme theme = jdbcThemeRepository.save(new Theme("kim", "desc1", "thumb1"));

        Optional<Theme> found = jdbcThemeRepository.findById(theme.getId());

        assertThat(found).isPresent();
        Theme savedTheme = found.get();
        assertThat(savedTheme.getId()).isEqualTo(theme.getId());
        assertThat(savedTheme.getName()).isEqualTo("kim");
        assertThat(savedTheme.getDescription()).isEqualTo("desc1");
        assertThat(savedTheme.getThumbnail()).isEqualTo("thumb1");
    }

    @Test
    @DisplayName("모든 Theme를 불러온다.")
    public void findAll() {
        jdbcThemeRepository.save(new Theme("kim", "desc1", "thumb1"));
        jdbcThemeRepository.save(new Theme("lee", "desc2", "thumb2"));
        jdbcThemeRepository.save(new Theme("park", "desc3", "thumb3"));

        List<Theme> themes = jdbcThemeRepository.findAll();

        assertThat(themes).hasSize(3)
                .extracting(
                        Theme::getName,
                        Theme::getDescription,
                        Theme::getThumbnail
                ).containsExactlyInAnyOrder(
                        tuple("kim", "desc1", "thumb1"),
                        tuple("lee", "desc2", "thumb2"),
                        tuple("park", "desc3", "thumb3")
                );
    }

    @Test
    @DisplayName("Theme 존재 여부를 조회한다.")
    public void existsById() {
        Theme theme = jdbcThemeRepository.save(new Theme("kim", "desc1", "thumb1"));

        boolean exists = jdbcThemeRepository.existsById(theme.getId());
        boolean notExists = jdbcThemeRepository.existsById(theme.getId() + 1);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("지정된 날짜 기간의 지정된 갯수 만큼의 테마정보를 예약 수를 순서대로 불러온다.")
    @Sql(scripts = "/popular-theme-data.sql")
    public void findTopThemesByReservationCount() {
        // given - @/popular-theme-data.sql

        // when
        List<Theme> topThemes = jdbcThemeRepository.findTopThemesByReservationCount(
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 5, 5),
                10
        );

        // then
        assertThat(topThemes).hasSizeLessThanOrEqualTo(10)
                .extracting(Theme::getId)
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    }

    @Test
    @DisplayName("Theme를 삭제한다.")
    public void deleteById() {
        Theme theme = jdbcThemeRepository.save(new Theme("kim", "desc1", "thumb1"));

        boolean deleted = jdbcThemeRepository.deleteById(theme.getId());

        assertThat(deleted).isTrue();
        assertThat(jdbcThemeRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 Theme는 삭제되지 않는다.")
    public void deleteById_fail() {
        // given
        Long id = 1L;

        // when
        boolean deleted = jdbcThemeRepository.deleteById(id);

        // then
        assertThat(deleted).isFalse();
    }

}
