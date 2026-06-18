package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.reservationslot.ReservationSlotRepository;
import roomescape.repository.theme.ThemeRepository;

@SpringBootTest
class JdbcThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        clearTables();
    }

    @Test
    @DisplayName("테마 저장")
    void theme_save_test() {
        // given
        Theme theme = Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png");

        // when
        Theme result = themeRepository.save(theme);
        Theme saved = themeRepository.findById(result.getId())
                .orElseThrow();

        // then
        assertThat(saved).isEqualTo(result);
    }

    @Test
    @DisplayName("테마 전체 조회")
    void theme_findAll_test() {
        // given
        themeRepository.save(Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png"));

        // when
        List<Theme> themes = themeRepository.findAll();

        // then
        assertThat(themes).hasSize(1);
    }

    @Test
    @DisplayName("테마 이름 중복 저장 예외")
    void theme_save_duplicate_test() {
        // given
        themeRepository.save(Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png"));

        // when & then
        assertThrows(PersistenceConflictException.class, () ->
                themeRepository.save(Theme.createNew("미술관의 밤", "새 설명", "https://example.com/new-theme.png"))
        );
    }

    @Test
    @DisplayName("테마 삭제")
    void theme_delete_test() {
        // given
        Theme saved = themeRepository.save(
                Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png")
        );
        int beforeSize = themeRepository.findAll().size();

        // when
        int affectedRowCount = themeRepository.deleteById(saved.getId());

        // then
        int afterSize = themeRepository.findAll().size();
        assertThat(affectedRowCount).isOne();
        assertThat(afterSize).isEqualTo(beforeSize - 1);
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID는 삭제 건수가 0이다")
    void theme_delete_not_found_test() {
        // when
        int affectedRowCount = themeRepository.deleteById(999L);

        // then
        assertThat(affectedRowCount).isZero();
    }

    @Test
    @DisplayName("테마 이름 존재 여부 확인")
    void theme_existsByName_test() {
        // given
        themeRepository.save(Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png"));

        // when
        boolean exists = themeRepository.existsByName("미술관의 밤");
        boolean notExists = themeRepository.existsByName("놀이공원 탈출");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("최근 기간 기준 인기 테마를 예약 수 순서대로 조회한다")
    void findPopularThemes_test() {
        LocalDate today = LocalDate.now();
        Theme firstTheme = createTheme("미술관의 밤");
        Theme secondTheme = createTheme("심해 연구소");
        Theme thirdTheme = createTheme("폐병원 탈출");

        ReservationTime firstThemeTime = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));
        ReservationTime secondThemeTime = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("11:00")));
        ReservationTime thirdThemeTime = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("12:00")));

        insertHistoricalReservation("쿠다", today.minusDays(1), firstTheme, firstThemeTime);
        insertHistoricalReservation("아루", today.minusDays(2), firstTheme, firstThemeTime);
        insertHistoricalReservation("도기", today.minusDays(3), firstTheme, firstThemeTime);

        insertHistoricalReservation("포비", today.minusDays(1), secondTheme, secondThemeTime);
        insertHistoricalReservation("솔라", today.minusDays(2), secondTheme, secondThemeTime);

        insertHistoricalReservation("레오", today.minusDays(1), thirdTheme, thirdThemeTime);
        insertHistoricalReservation("오래된예약", today.minusDays(10), thirdTheme, thirdThemeTime);

        List<Theme> popularThemes = themeRepository.findPopularThemes(7, 2);

        assertThat(popularThemes).hasSize(2);
        assertThat(popularThemes.get(0).getId()).isEqualTo(firstTheme.getId());
        assertThat(popularThemes.get(1).getId()).isEqualTo(secondTheme.getId());
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    private Theme createTheme(final String name) {
        return themeRepository.save(
                Theme.createNew(name, "추리 테마", "https://example.com/theme.png")
        );
    }

    private void insertHistoricalReservation(
            final String name,
            final LocalDate date,
            final Theme theme,
            final ReservationTime reservationTime
    ) {
        ReservationSlot slot = reservationSlotRepository.save(new ReservationSlot(date, theme, reservationTime));
        jdbcTemplate.update(
                "INSERT INTO reservation (name, slot_id, created_at) VALUES (?, ?, ?)",
                name,
                slot.getId(),
                date.minusDays(1).atStartOfDay()
        );
    }
}
