package roomescape.reservationslot.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.exception.ReservationNotFoundException;
import roomescape.reservation.repository.jdbc.JdbcReservationRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Sql("/clear.sql")
class JdbcReservationSlotRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private JdbcReservationSlotRepository reservationSlotRepository;
    private JdbcReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationSlotRepository = new JdbcReservationSlotRepository(jdbcTemplate);
        reservationRepository = new JdbcReservationRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("예약을 삭제하고 첫 번째 대기를 예약으로 승격한다")
    void deleteReservationAndPromoteFirstWaiting() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        Long slotId = insertReservationSlot("2026-08-05", 1L, 1L);
        insertReservation("브라운", slotId);
        insertWaiting("코로구", slotId, LocalDateTime.of(2026, 8, 1, 10, 0));
        insertWaiting("재키", slotId, LocalDateTime.of(2026, 8, 1, 10, 1));

        Reservation reservation = reservationRepository.findById(1L).get();

        reservationSlotRepository.deleteReservationAndPromoteWaiting(reservation);

        String promotedCustomerName = jdbcTemplate.queryForObject(
                "SELECT customer_name FROM reservation WHERE slot_id = ?",
                String.class,
                slotId
        );
        Integer waitingCount = jdbcTemplate.queryForObject("SELECT count(1) FROM waiting", Integer.class);
        Integer slotCount = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation_slot", Integer.class);

        assertThat(promotedCustomerName).isEqualTo("코로구");
        assertThat(waitingCount).isEqualTo(1);
        assertThat(slotCount).isEqualTo(1);
    }

    @Test
    @DisplayName("예약과 대기가 모두 없어지면 빈 슬롯을 삭제한다")
    void deleteEmptySlotWhenReservationAndWaitingDoNotExist() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        Long slotId = insertReservationSlot("2026-08-05", 1L, 1L);
        insertReservation("브라운", slotId);

        Reservation reservation = reservationRepository.findById(1L).get();

        reservationSlotRepository.deleteReservationAndPromoteWaiting(reservation);

        Integer reservationCount = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);
        Integer slotCount = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation_slot", Integer.class);

        assertThat(reservationCount).isZero();
        assertThat(slotCount).isZero();
    }

    @Test
    @DisplayName("이미 삭제된 예약으로 다시 삭제하면 승격된 예약은 삭제하지 않는다")
    void doNotDeletePromotedReservationWhenDeletingAlreadyDeletedReservationAgain() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        Long slotId = insertReservationSlot("2026-08-05", 1L, 1L);
        insertReservation("브라운", slotId);
        insertWaiting("코로구", slotId, LocalDateTime.of(2026, 8, 1, 10, 0));

        Reservation deletedReservation = reservationRepository.findById(1L).get();
        reservationSlotRepository.deleteReservationAndPromoteWaiting(deletedReservation);

        assertThatThrownBy(() -> reservationSlotRepository.deleteReservationAndPromoteWaiting(deletedReservation))
                .isInstanceOf(ReservationNotFoundException.class);

        String promotedCustomerName = jdbcTemplate.queryForObject(
                "SELECT customer_name FROM reservation WHERE slot_id = ?",
                String.class,
                slotId
        );

        assertThat(promotedCustomerName).isEqualTo("코로구");
    }

    private void insertReservationTime(final String startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                startAt
        );
    }

    private void insertTheme(final String name, final String description, final String thumbnailUrl) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name,
                description,
                thumbnailUrl
        );
    }

    private void insertReservation(final String name, final Long slotId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (customer_name, slot_id) VALUES (?, ?)",
                name,
                slotId
        );
    }

    private void insertWaiting(final String name, final Long slotId, final LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO waiting (customer_name, slot_id, created_at) VALUES (?, ?, ?)",
                name,
                slotId,
                Timestamp.valueOf(createdAt)
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
