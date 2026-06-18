package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
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
import roomescape.repository.reservationslot.ReservationSlotRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;

@SpringBootTest
class JdbcReservationSlotRepositoryTest {

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clearTables();
    }

    @Test
    @DisplayName("날짜와 테마로 슬롯을 조회한다")
    void findByDateAndTheme() {
        Theme theme = themeRepository.save(
                Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png")
        );
        Theme otherTheme = themeRepository.save(
                Theme.createNew("심해 연구소", "잠수 테마", "https://example.com/other.png")
        );
        ReservationTime ten = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));
        ReservationTime eleven = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("11:00")));
        LocalDate date = LocalDate.parse("2026-08-06");

        ReservationSlot first = reservationSlotRepository.save(new ReservationSlot(date, theme, ten));
        ReservationSlot second = reservationSlotRepository.save(new ReservationSlot(date, theme, eleven));
        reservationSlotRepository.save(new ReservationSlot(date.plusDays(1), theme, ten));
        reservationSlotRepository.save(new ReservationSlot(date, otherTheme, ten));

        List<ReservationSlot> slots = reservationSlotRepository.findByDateAndTheme(date, theme);

        assertThat(slots)
                .extracting(ReservationSlot::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }
}
