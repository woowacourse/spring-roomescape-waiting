package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.AvailableTime;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always"
})
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findAll_전체_테마_조회() {
        assertThat(themeRepository.findAll()).isNotEmpty();
    }

    @Test
    void findById_존재하는_id이면_반환() {
        Optional<Theme> result = themeRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("공포의 저택");
    }

    @Test
    void findById_존재하지_않는_id이면_empty() {
        Optional<Theme> result = themeRepository.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findPopularThemes_인기_테마_순위_조회() {
        List<Theme> themes = themeRepository.findPopularThemes(4, LocalDate.of(2026, 4, 29), LocalDate.of(2026, 5, 5));

        assertThat(themes).hasSize(4);
        assertThat(themes.get(0).getName()).isEqualTo("공포의 저택");
        assertThat(themes.get(1).getName()).isEqualTo("탐정 사무소");
        assertThat(themes.get(2).getName()).isEqualTo("마법사의 연구실");
        assertThat(themes.get(3).getName()).isEqualTo("우주 정거장");
    }

    @Test
    void findAvailableTimesForTheme_예약된_시간은_false_나머지는_true() {
        List<AvailableTime> times = themeRepository.findAvailableTimesForTheme(1L, LocalDate.of(2026, 5, 10))
                .stream()
                .map(row -> new AvailableTime(new ReservationTime(row.getId(), row.getStartAt()), row.isAvailable()))
                .toList();

        AvailableTime bookedSlot = times.stream()
                .filter(t -> t.time().getStartAt().getHour() == 12)
                .findFirst()
                .orElseThrow();
        assertThat(bookedSlot.available()).isFalse();
        assertThat(times.stream().filter(AvailableTime::available).count())
                .isEqualTo(times.size() - 1);
    }

    @Test
    void findAvailableTimesForTheme_대기_예약이_있어도_시간_슬롯_중복_없음() {
        LocalDate date = LocalDate.of(2026, 12, 31);
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, created_at, time_id, theme_id, status) VALUES (?, ?, ?, ?, ?, ?)",
                "브라운", date, "2026-11-01 00:00:00", 1, 1, "CONFIRMED");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, created_at, time_id, theme_id, status) VALUES (?, ?, ?, ?, ?, ?)",
                "이영희", date, "2026-11-01 01:00:00", 1, 1, "WAITING");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, created_at, time_id, theme_id, status) VALUES (?, ?, ?, ?, ?, ?)",
                "김철수", date, "2026-11-01 02:00:00", 1, 1, "WAITING");

        List<AvailableTime> times = themeRepository.findAvailableTimesForTheme(1L, date)
                .stream()
                .map(row -> new AvailableTime(new ReservationTime(row.getId(), row.getStartAt()), row.isAvailable()))
                .toList();

        assertThat(times).hasSize(13);
        AvailableTime slot = times.stream()
                .filter(t -> t.time().getStartAt().getHour() == 10)
                .findFirst()
                .orElseThrow();
        assertThat(slot.available()).isFalse();
    }

    @Test
    void save_테마_저장() {
        Theme saved = themeRepository.save(new Theme(null, "새로운 테마", "설명", "https://example.com/img.jpg"));

        assertThat(themeRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void delete_테마_삭제() {
        Theme saved = themeRepository.save(new Theme(null, "임시 테마", "설명", "https://example.com/img.jpg"));

        themeRepository.deleteById(saved.getId());

        assertThat(themeRepository.findById(saved.getId())).isEmpty();
    }
}
