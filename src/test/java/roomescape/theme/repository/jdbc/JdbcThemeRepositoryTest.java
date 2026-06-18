package roomescape.theme.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@ActiveProfiles("jdbc")
@Sql("/clear.sql")
class JdbcThemeRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    JdbcThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        themeRepository = new JdbcThemeRepository(jdbcTemplate);
    }

    @Test
    void 테마를_저장하고_조회한다() {
        Theme theme = Theme.create("링", "공포 테마", "http:~");

        Theme savedTheme = themeRepository.save(theme);

        Optional<Theme> foundTheme = themeRepository.findById(savedTheme.getId());
        assertThat(foundTheme).isPresent();
        assertThat(foundTheme.get().getName()).isEqualTo("링");
        assertThat(foundTheme.get().getDescription()).isEqualTo("공포 테마");
        assertThat(foundTheme.get().getThumbnailUrl()).isEqualTo("http:~");
    }

    @Test
    void 존재하지_않는_테마를_조회하면_빈_Optional을_반환한다() {
        Optional<Theme> foundTheme = themeRepository.findById(1L);

        assertThat(foundTheme).isEmpty();
    }

    @Test
    void 테마를_삭제한다() {
        Theme savedTheme = themeRepository.save(Theme.create("링", "공포 테마", "http:~"));

        boolean deleted = themeRepository.deleteById(savedTheme.getId());

        assertThat(deleted).isTrue();
        assertThat(themeRepository.findById(savedTheme.getId())).isEmpty();
    }

    @Test
    void 존재하지_않는_테마를_삭제하면_false를_반환한다() {
        boolean deleted = themeRepository.deleteById(1L);

        assertThat(deleted).isFalse();
    }

    @Test
    void 해당_테마에_예약이_있으면_테마를_삭제할_수_없다() {
        Theme savedTheme = themeRepository.save(Theme.create("링", "공포 테마", "http:~"));
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운",
                "2026-08-05",
                1L,
                savedTheme.getId()
        );

        assertThatThrownBy(() -> themeRepository.deleteById(savedTheme.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Sql(scripts = {
            "/clear.sql",
            "/popular-themes-test-data.sql"
    })
    void 최근_예약이_많은_상위_10개_테마를_조회한다() {
        List<Theme> popularThemes = themeRepository.findPopularThemes(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 8)
        );

        assertThat(popularThemes).hasSize(10);
        assertThat(popularThemes)
                .extracting(Theme::getName)
                .containsExactly(
                        "잃어버린 시간의 방",
                        "심야 병동",
                        "마법사의 서재",
                        "해적선의 보물",
                        "비밀 연구소",
                        "탐정 사무소의 마지막 사건",
                        "고대 유적의 저주",
                        "달빛 아래의 저택",
                        "우주 정거장 알파",
                        "지하철 0호선"
                );
    }
}
