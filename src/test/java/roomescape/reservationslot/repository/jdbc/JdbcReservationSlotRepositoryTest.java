package roomescape.reservationslot.repository.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Sql("/clear.sql")
@Import(JdbcReservationSlotRepository.class)
class JdbcReservationSlotRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcReservationSlotRepository reservationSlotRepository;

    @Test
    @DisplayName("예약 슬롯을 조회한다")
    void findReservationSlot() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        insertReservationSlot("2026-08-05", 1L, 1L);

        Optional<ReservationSlot> slot = reservationSlotRepository.findByDateAndTimeIdAndThemeId(
                LocalDate.of(2026, 8, 5),
                1L,
                1L
        );

        assertThat(slot).isPresent();
        assertThat(slot.get().getDate()).isEqualTo(LocalDate.of(2026, 8, 5));
    }

    @Test
    @DisplayName("같은 날짜, 시간, 테마의 슬롯이 이미 있으면 기존 슬롯을 반환한다")
    void findExistingSlotWhenCreatingDuplicatedSlot() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        Long savedSlotId = insertReservationSlot("2026-08-05", 1L, 1L);

        ReservationSlot slot = reservationSlotRepository.findOrCreate(
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~", 10000)
        );

        assertThat(slot.getId()).isEqualTo(savedSlotId);
    }

    @Test
    @DisplayName("예약 슬롯을 id로 조회하면서 잠근다")
    void findReservationSlotByIdForUpdate() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        Long savedSlotId = insertReservationSlot("2026-08-05", 1L, 1L);

        Optional<ReservationSlot> slot = reservationSlotRepository.findByIdForUpdate(savedSlotId);

        assertThat(slot).isPresent();
        assertThat(slot.get().getId()).isEqualTo(savedSlotId);
    }

    @Test
    @DisplayName("존재하지 않는 예약 슬롯을 id로 조회하면 empty를 반환한다")
    void returnEmptyWhenFindingNonExistingReservationSlotByIdForUpdate() {
        Optional<ReservationSlot> slot = reservationSlotRepository.findByIdForUpdate(999L);

        assertThat(slot).isEmpty();
    }

    private void insertReservationTime(final String startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                startAt
        );
    }

    private void insertTheme(
            final String name,
            final String description,
            final String thumbnailUrl,
            final int price
    ) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)",
                name,
                description,
                thumbnailUrl,
                price
        );
    }

    private Long insertReservationSlot(final String date, final Long timeId, final Long themeId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO reservation_slot (reservation_date, time_id, theme_id) VALUES (?, ?, ?)",
                    date,
                    timeId,
                    themeId
            );
        } catch (DuplicateKeyException ignored) {
        }
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_slot WHERE reservation_date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                date,
                timeId,
                themeId
        );
    }
}
