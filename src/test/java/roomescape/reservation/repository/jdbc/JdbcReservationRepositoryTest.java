package roomescape.reservation.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Sql("/clear.sql")
class JdbcReservationRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.now(Clock.fixed(
            LocalDate.of(2026, 5, 8)
                    .atTime(10, 30)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant(),
            ZoneId.of("Asia/Seoul")
    ));

    @Autowired
    JdbcTemplate jdbcTemplate;

    JdbcReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new JdbcReservationRepository(jdbcTemplate);
    }

    @Test
    void 예약을_저장하고_조회한다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        Long slotId = insertReservationSlot("2026-08-05", 1L, 1L);
        Reservation reservation = Reservation.create(
                "브라운",
                ReservationSlot.of(
                        slotId,
                        LocalDate.of(2026, 8, 5),
                        ReservationTime.of(1L, LocalTime.of(10, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~")
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
    void 존재하지_않는_예약_슬롯으로_예약을_저장하면_예외가_발생한다() {
        insertTheme("링", "공포 테마", "http:~");
        Reservation reservation = Reservation.create(
                "브라운",
                ReservationSlot.of(
                        999L,
                        LocalDate.of(2026, 8, 5),
                        ReservationTime.of(999L, LocalTime.of(10, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~")
                ),
                LocalDateTime.of(2026, 8, 5, 9, 0)
        );

        assertThatThrownBy(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 예약을_id로_조회한다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        Optional<Reservation> reservation = reservationRepository.findById(1L);

        assertThat(reservation).isPresent();
        assertThat(reservation.get().getCustomerName()).isEqualTo("브라운");
    }

    @Test
    void 예약을_수정한다() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);
        Long changedSlotId = insertReservationSlot("2026-08-06", 2L, 1L);

        boolean updated = reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                ReservationSlot.of(
                        changedSlotId,
                        LocalDate.of(2026, 8, 6),
                        ReservationTime.of(2L, LocalTime.of(11, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~")
                )
        ));

        Optional<Reservation> reservation = reservationRepository.findById(1L);
        assertThat(updated).isTrue();
        assertThat(reservation).isPresent();
        assertThat(reservation.get().getDate()).isEqualTo(LocalDate.of(2026, 8, 6));
        assertThat(reservation.get().getTime().getId()).isEqualTo(2L);
    }

    @Test
    void 존재하지_않는_예약을_수정하면_false를_반환한다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");

        boolean updated = reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                ReservationSlot.of(
                        999L,
                        LocalDate.of(2026, 8, 5),
                        ReservationTime.of(1L, LocalTime.of(10, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~")
                )
        ));

        assertThat(updated).isFalse();
    }

    @Test
    void 이미_존재하는_예약으로_수정하면_예외가_발생한다() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);
        insertReservation("제임스", "2026-08-05", 2L, 1L);
        Long duplicatedSlotId = insertReservationSlot("2026-08-05", 2L, 1L);

        assertThatThrownBy(() -> reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                ReservationSlot.of(
                        duplicatedSlotId,
                        LocalDate.of(2026, 8, 5),
                        ReservationTime.of(2L, LocalTime.of(11, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~")
                )
        ))).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 존재하지_않는_예약_슬롯으로_수정하면_예외가_발생한다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        assertThatThrownBy(() -> reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                ReservationSlot.of(
                        999L,
                        LocalDate.of(2026, 8, 5),
                        ReservationTime.of(999L, LocalTime.of(11, 0)),
                        Theme.of(1L, "링", "공포 테마", "http:~")
                )
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 예약자_이름으로_예약_목록을_조회한다() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~");
        insertReservation("초코칩", "2026-08-05", 1L, 1L);
        insertReservation("재키", "2026-08-05", 2L, 1L);

        List<Reservation> reservations = reservationRepository.findAllByCustomerNameAndReservationDateTimeAfter(new CustomerName("초코칩"), NOW);

        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getCustomerName()).isEqualTo("초코칩");
    }

    @Test
    void 날짜와_테마에_따른_예약_시간_상태를_조회한다() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~");
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

    private void insertTheme(final String name, final String description, final String thumbnailUrl) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name,
                description,
                thumbnailUrl
        );
    }

    private void insertReservation(final String name, final String date, final Long timeId, final Long themeId) {
        Long slotId = insertReservationSlot(date, timeId, themeId);
        jdbcTemplate.update(
                "INSERT INTO reservation (customer_name, slot_id) VALUES (?, ?)",
                name,
                slotId
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
