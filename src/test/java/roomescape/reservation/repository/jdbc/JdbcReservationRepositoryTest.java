package roomescape.reservation.repository.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Sql("/clear.sql")
@Import(JdbcReservationRepository.class)
class JdbcReservationRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 8, 10, 30);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JdbcReservationRepository reservationRepository;

    @Test
    @DisplayName("예약을 저장하고 조회한다")
    void saveAndFindReservation() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        Long slotId = insertReservationSlot("2026-08-05", 1L, 1L);
        Reservation reservation = Reservation.create(
                "브라운",
                "customer@example.com",
                ReservationSlot.of(
                        slotId,
                        LocalDate.of(2026, 8, 5),
                        ReservationTime.of(1L, LocalTime.of(10, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~", 10000)
                ),
                LocalDateTime.of(2026, 8, 5, 9, 0)
        );

        Reservation savedReservation = reservationRepository.save(reservation);

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getId()).isEqualTo(savedReservation.getId());
        assertThat(savedReservation.getSlotId()).isEqualTo(slotId);
        assertThat(reservations.getFirst().getCustomerName()).isEqualTo("브라운");
        assertThat(reservations.getFirst().getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reservations.getFirst().getTheme().getName()).isEqualTo("링");
    }

    @Test
    @DisplayName("존재하지 않는 예약 슬롯으로 예약을 저장하면 예외가 발생한다")
    void throwExceptionWhenSavingReservationWithNonExistingReservationSlot() {
        insertTheme("링", "공포 테마", "http:~", 10000);
        Reservation reservation = Reservation.create(
                "브라운",
                "customer@example.com",
                ReservationSlot.of(
                        999L,
                        LocalDate.of(2026, 8, 5),
                        ReservationTime.of(999L, LocalTime.of(10, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~", 10000)
                ),
                LocalDateTime.of(2026, 8, 5, 9, 0)
        );

        assertThatThrownBy(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("예약을 id로 조회한다")
    void findReservationById() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        Optional<Reservation> reservation = reservationRepository.findById(1L);

        assertThat(reservation).isPresent();
        assertThat(reservation.get().getCustomerName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("예약을 수정한다")
    void updateReservation() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", "2026-08-05", 1L, 1L);
        Long changedSlotId = insertReservationSlot("2026-08-06", 2L, 1L);

        boolean updated = reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                ReservationSlot.of(
                        changedSlotId,
                        LocalDate.of(2026, 8, 6),
                        ReservationTime.of(2L, LocalTime.of(11, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~", 10000)
                )
        ));

        Optional<Reservation> reservation = reservationRepository.findById(1L);
        assertThat(updated).isTrue();
        assertThat(reservation).isPresent();
        assertThat(reservation.get().getDate()).isEqualTo(LocalDate.of(2026, 8, 6));
        assertThat(reservation.get().getTime().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 수정하면 false를 반환한다")
    void returnFalseWhenUpdatingNonExistingReservation() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~", 10000);

        boolean updated = reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                ReservationSlot.of(
                        999L,
                        LocalDate.of(2026, 8, 5),
                        ReservationTime.of(1L, LocalTime.of(10, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~", 10000)
                )
        ));

        assertThat(updated).isFalse();
    }

    @Test
    @DisplayName("이미 존재하는 예약으로 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingToExistingReservation() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", "2026-08-05", 1L, 1L);
        insertReservation("제임스", "2026-08-05", 2L, 1L);
        Long duplicatedSlotId = insertReservationSlot("2026-08-05", 2L, 1L);

        assertThatThrownBy(() -> reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                ReservationSlot.of(
                        duplicatedSlotId,
                        LocalDate.of(2026, 8, 5),
                        ReservationTime.of(2L, LocalTime.of(11, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~", 10000)
                )
        ))).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 슬롯으로 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingToNonExistingReservationSlot() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        assertThatThrownBy(() -> reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                ReservationSlot.of(
                        999L,
                        LocalDate.of(2026, 8, 5),
                        ReservationTime.of(999L, LocalTime.of(11, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~", 10000)
                )
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("예약을 id와 슬롯 id로 삭제한다")
    void deleteReservationByIdAndSlotId() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        Long slotId = insertReservation("브라운", "2026-08-05", 1L, 1L);

        boolean deleted = reservationRepository.deleteByIdAndSlotId(1L, slotId);

        Integer reservationCount = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);
        assertThat(deleted).isTrue();
        assertThat(reservationCount).isZero();
    }

    @Test
    @DisplayName("예약 id와 슬롯 id가 함께 일치하지 않으면 삭제하지 않는다")
    void returnFalseWhenDeletingReservationWithDifferentSlotId() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", "2026-08-05", 1L, 1L);
        Long otherSlotId = insertReservationSlot("2026-08-05", 2L, 1L);

        boolean deleted = reservationRepository.deleteByIdAndSlotId(1L, otherSlotId);

        Integer reservationCount = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);
        assertThat(deleted).isFalse();
        assertThat(reservationCount).isEqualTo(1);
    }

    @Test
    @DisplayName("예약자 이름으로 예약 목록을 조회한다")
    void findReservationsByCustomerName() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        insertReservation("초코칩", "2026-08-05", 1L, 1L);
        insertReservation("재키", "2026-08-05", 2L, 1L);

        List<Reservation> reservations = reservationRepository.findAllByCustomerNameAndCustomerEmailAndReservationDateTimeAfter(
                "초코칩",
                "customer@example.com",
                NOW
        );

        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getCustomerName()).isEqualTo("초코칩");
    }

    @Test
    @DisplayName("날짜와 테마에 따른 예약 시간 상태를 조회한다")
    void findReservationTimeStatusesByDateAndTheme() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~", 10000);
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        List<ReservationTimesWithStatus> timeStatuses = reservationRepository.findReservationTimeStatusesByDateAndThemeId(
                LocalDate.of(2026, 8, 5),
                1L
        );

        assertThat(timeStatuses).hasSize(2);
        assertThat(timeStatuses.get(0).id()).isEqualTo(1L);
        assertThat(timeStatuses.get(0).reserved()).isTrue();
        assertThat(timeStatuses.get(1).id()).isEqualTo(2L);
        assertThat(timeStatuses.get(1).reserved()).isFalse();
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

    private Long insertReservation(final String name, final String date, final Long timeId, final Long themeId) {
        Long slotId = insertReservationSlot(date, timeId, themeId);
        jdbcTemplate.update(
                "INSERT INTO reservation (customer_name, customer_email, slot_id, status) VALUES (?, ?, ?, ?)",
                name,
                "customer@example.com",
                slotId,
                "CONFIRMED"
        );
        return slotId;
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
