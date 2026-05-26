package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import roomescape.domain.AvailableTime;
import roomescape.domain.Theme;

class ThemeDaoTest {

    private EmbeddedDatabase dataSource;
    private ThemeDao themeDao;

    @BeforeEach
    void setUp() {
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:data.sql")
                .build();
        themeDao = new ThemeDao(new JdbcTemplate(dataSource));
    }

    @AfterEach
    void tearDown() {
        dataSource.shutdown();
    }

    @Test
    void findAll_전체_테마_조회() {
        assertThat(themeDao.findAll()).isNotEmpty();
    }

    @Test
    void findById_존재하는_id이면_반환() {
        Optional<Theme> result = themeDao.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("공포의 저택");
    }

    @Test
    void findById_존재하지_않는_id이면_empty() {
        Optional<Theme> result = themeDao.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findPopularThemes_인기_테마_순위_조회() {
        List<Theme> themes = themeDao.findPopularThemes(4, LocalDate.of(2026, 4, 29), LocalDate.of(2026, 5, 5));

        assertThat(themes).hasSize(4);
        assertThat(themes.get(0).getName()).isEqualTo("공포의 저택");
        assertThat(themes.get(1).getName()).isEqualTo("탐정 사무소");
        assertThat(themes.get(2).getName()).isEqualTo("마법사의 연구실");
        assertThat(themes.get(3).getName()).isEqualTo("우주 정거장");
    }

    @Test
    void findAvailableTimeById_예약된_시간은_false_나머지는_true() {
        List<AvailableTime> times = themeDao.findAvailableTimeById(1L, LocalDate.of(2026, 5, 10));

        AvailableTime bookedSlot = times.stream()
                .filter(t -> t.time().getStartAt().getHour() == 12)
                .findFirst()
                .orElseThrow();
        assertThat(bookedSlot.available()).isFalse();
        assertThat(times.stream().filter(AvailableTime::available).count())
                .isEqualTo(times.size() - 1);
    }

    @Test
    void save_테마_저장() {
        long id = themeDao.save("새로운 테마", "설명", "https://example.com/img.jpg");

        assertThat(themeDao.findById(id)).isPresent();
    }

    @Test
    void delete_테마_삭제() {
        long id = themeDao.save("임시 테마", "설명", "https://example.com/img.jpg");

        themeDao.delete(id);

        assertThat(themeDao.findById(id)).isEmpty();
    }
}
