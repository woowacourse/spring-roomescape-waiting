package roomescape.theme.repository.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Sql("/clear.sql")
@Import(JdbcThemeRepository.class)
class JdbcThemeRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JdbcThemeRepository themeRepository;

    @Test
    @DisplayName("테마를 저장하고 조회한다")
    void saveAndFindTheme() {
        Theme theme = Theme.create("링", "공포 테마", "http:~", 10000);

        Theme savedTheme = themeRepository.save(theme);

        Optional<Theme> foundTheme = themeRepository.findById(savedTheme.getId());
        assertThat(foundTheme).isPresent();
        assertThat(foundTheme.get().getName()).isEqualTo("링");
        assertThat(foundTheme.get().getDescription()).isEqualTo("공포 테마");
        assertThat(foundTheme.get().getThumbnailUrl()).isEqualTo("http:~");
    }

    @Test
    @DisplayName("존재하지 않는 테마를 조회하면 빈 Optional을 반환한다")
    void returnEmptyOptionalWhenThemeDoesNotExist() {
        Optional<Theme> foundTheme = themeRepository.findById(1L);

        assertThat(foundTheme).isEmpty();
    }

    @Test
    @DisplayName("테마를 삭제한다")
    void deleteTheme() {
        Theme savedTheme = themeRepository.save(Theme.create("링", "공포 테마", "http:~", 10000));

        boolean deleted = themeRepository.deleteById(savedTheme.getId());

        assertThat(deleted).isTrue();
        assertThat(themeRepository.findById(savedTheme.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하면 false를 반환한다")
    void returnFalseWhenDeletingNonExistingTheme() {
        boolean deleted = themeRepository.deleteById(1L);

        assertThat(deleted).isFalse();
    }

    @Test
    @DisplayName("해당 테마에 예약이 있으면 테마를 삭제할 수 없다")
    void cannotDeleteThemeInUse() {
        Theme savedTheme = themeRepository.save(Theme.create("링", "공포 테마", "http:~", 10000));
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update(
                "INSERT INTO reservation_slot (reservation_date, time_id, theme_id) VALUES (?, ?, ?)",
                "2026-08-05",
                1L,
                savedTheme.getId()
        );
        jdbcTemplate.update(
                "INSERT INTO reservation (customer_name, customer_email, slot_id, status) VALUES (?, ?, ?, ?)",
                "브라운",
                "brown@example.com",
                1L,
                "CONFIRMED"
        );

        assertThatThrownBy(() -> themeRepository.deleteById(savedTheme.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Sql(scripts = {
            "/clear.sql",
            "/popular-themes-test-data.sql"
    })
    @DisplayName("최근 예약이 많은 상위 10개 테마를 조회한다")
    void findTopTenPopularThemesByRecentReservations() {
        final LocalDate today = LocalDate.now();

        List<Theme> popularThemes = themeRepository.findPopularThemes(
                today.minusDays(7),
                today
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
