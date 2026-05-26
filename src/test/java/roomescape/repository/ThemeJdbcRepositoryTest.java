package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.PopularTheme;
import roomescape.domain.Theme;
import roomescape.fixture.DbFixtures;

@JdbcTest
@Import(ThemeJdbcRepository.class)
class ThemeJdbcRepositoryTest {

    @Autowired
    private ThemeJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void save_저장하면_생성된_id를_반환한다() {
        Theme theme = new Theme(null, "공포", "무서움", "https://thumbnail.url");

        Long id = repository.save(theme);

        assertThat(id).isPositive();
    }

    @Test
    void findById_저장한_테마를_조회한다() {
        Long id = repository.save(new Theme(null, "공포", "무서움", "https://thumbnail.url"));

        Theme found = repository.findById(id).orElseThrow();

        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getName()).isEqualTo("공포");
        assertThat(found.getDescription()).isEqualTo("무서움");
        assertThat(found.getThumbnailImageUrl()).isEqualTo("https://thumbnail.url");
    }

    @Test
    void findById_없는_id이면_Optional_empty를_반환한다() {
        assertThat(repository.findById(9999L)).isEmpty();
    }

    @Test
    void findAll_페이징은_id_오름차순으로_limit과_offset을_적용한다() {
        Long id1 = repository.save(new Theme(null, "A", "a", "u"));
        Long id2 = repository.save(new Theme(null, "B", "b", "u"));
        Long id3 = repository.save(new Theme(null, "C", "c", "u"));

        List<Theme> firstPage = repository.findAll(2, 0);
        List<Theme> secondPage = repository.findAll(2, 2);

        assertThat(firstPage).extracting(Theme::getId).containsExactly(id1, id2);
        assertThat(secondPage).extracting(Theme::getId).containsExactly(id3);
    }

    @Test
    void deleteById_삭제된_테마는_조회되지_않는다() {
        Long id = repository.save(new Theme(null, "공포", "무서움", "https://thumbnail.url"));

        repository.deleteById(id);

        List<Theme> all = repository.findAll(10, 0);
        assertThat(all).extracting(Theme::getId).doesNotContain(id);
    }

    @Test
    void findPopularThemes_예약수가_많은_순서대로_정렬된다() {
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long themeA = repository.save(new Theme(null, "A", "a", "u"));
        Long themeB = repository.save(new Theme(null, "B", "b", "u"));
        Long themeC = repository.save(new Theme(null, "C", "c", "u"));

        DbFixtures.insertReservation(jdbcTemplate, "u1", themeA, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeB, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u2", themeB, "2026-05-02", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeC, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u2", themeC, "2026-05-02", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u3", themeC, "2026-05-03", timeId);

        List<PopularTheme> popular = repository.findPopularThemes(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 10);

        assertThat(popular).extracting(p -> p.getTheme().getId())
                .containsExactly(themeC, themeB, themeA);
        assertThat(popular).extracting(PopularTheme::getReservationCount)
                .containsExactly(3L, 2L, 1L);
    }

    @Test
    void findPopularThemes_예약수가_같으면_id_오름차순으로_정렬된다() {
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long themeA = repository.save(new Theme(null, "A", "a", "u"));
        Long themeB = repository.save(new Theme(null, "B", "b", "u"));

        DbFixtures.insertReservation(jdbcTemplate, "u1", themeA, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeB, "2026-05-01", timeId);

        List<PopularTheme> popular = repository.findPopularThemes(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 10);

        assertThat(popular).extracting(p -> p.getTheme().getId())
                .containsExactly(themeA, themeB);
    }

    @Test
    void findPopularThemes_기간_밖_예약은_집계되지_않는다() {
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long themeId = repository.save(new Theme(null, "A", "a", "u"));

        DbFixtures.insertReservation(jdbcTemplate, "u1", themeId, "2026-04-30", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u2", themeId, "2026-05-08", timeId);

        List<PopularTheme> popular = repository.findPopularThemes(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 10);

        assertThat(popular).isEmpty();
    }

    @Test
    void findPopularThemes_limit_만큼만_반환한다() {
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long themeA = repository.save(new Theme(null, "A", "a", "u"));
        Long themeB = repository.save(new Theme(null, "B", "b", "u"));
        Long themeC = repository.save(new Theme(null, "C", "c", "u"));

        DbFixtures.insertReservation(jdbcTemplate, "u1", themeA, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeB, "2026-05-01", timeId);
        DbFixtures.insertReservation(jdbcTemplate, "u1", themeC, "2026-05-01", timeId);

        List<PopularTheme> popular = repository.findPopularThemes(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 2);

        assertThat(popular).hasSize(2);
    }
}
