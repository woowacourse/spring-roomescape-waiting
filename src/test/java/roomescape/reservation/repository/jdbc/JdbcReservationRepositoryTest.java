package roomescape.reservation.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@JdbcTest
@ActiveProfiles("jdbc")
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
        Reservation reservation = Reservation.create(
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~"),
                LocalDateTime.of(2026, 8, 5, 9, 0)
        );

        Reservation savedReservation = reservationRepository.save(reservation);

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getId()).isEqualTo(savedReservation.getId());
        assertThat(reservations.getFirst().getCustomerName()).isEqualTo("브라운");
        assertThat(reservations.getFirst().getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reservations.getFirst().getTheme().getName()).isEqualTo("링");
    }

    @Test
    void 존재하지_않는_예약_시간으로_예약을_저장하면_예외가_발생한다() {
        insertTheme("링", "공포 테마", "http:~");
        Reservation reservation = Reservation.create(
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(999L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~"),
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

    @Nested
    @DisplayName("슬롯(날짜, 시간, 테마) 정보로 예약이 존재하는지 확인한다")
    class ExistsBySlot {

        @Test
        void 동일한_슬롯_정보의_예약이_존재하면_TRUE_를_반환한다() {
            // given
            insertReservationTime("10:00");
            insertTheme("링", "공포 테마", "http:~");

            final LocalDate date = LocalDate.parse("2026-08-05");
            insertReservation("브라운", date.toString(), 1L, 1L);

            // when
            final boolean existsBySlot = reservationRepository.existsBySlot(date, 1L, 1L);

            // then
            assertThat(existsBySlot).isTrue();
        }

        @Test
        void 날짜가_달라_동일한_슬롯_정보의_예약이_존재하지_않으면_FALSE_를_반환한다() {
            // given
            insertReservationTime("10:00");
            insertTheme("링", "공포 테마", "http:~");

            final LocalDate date = LocalDate.parse("2026-08-05");
            insertReservation("브라운", date.toString(), 1L, 1L);

            final LocalDate otherDate = date.plusDays(1);

            // when
            final boolean existsBySlot = reservationRepository.existsBySlot(otherDate, 1L, 1L);

            // then
            assertThat(existsBySlot).isFalse();
        }

        @Test
        void 시간이_달라_동일한_슬롯_정보의_예약이_존재하지_않으면_FALSE_를_반환한다() {
            // given
            insertReservationTime("10:00");
            insertReservationTime("12:00");
            insertTheme("링", "공포 테마", "http:~");

            final LocalDate date = LocalDate.parse("2026-08-05");
            insertReservation("브라운", date.toString(), 1L, 1L);

            // when
            final boolean existsBySlot = reservationRepository.existsBySlot(date, 2L, 1L);

            // then
            assertThat(existsBySlot).isFalse();
        }

        @Test
        void 테마가_달라_동일한_슬롯_정보의_예약이_존재하지_않으면_FALSE_를_반환한다() {
            // given
            insertReservationTime("10:00");
            insertTheme("링", "공포 테마", "http:~");
            insertTheme("링", "공포 테마", "http:~");

            final LocalDate date = LocalDate.parse("2026-08-05");
            insertReservation("브라운", date.toString(), 1L, 1L);

            // when
            final boolean existsBySlot = reservationRepository.existsBySlot(date, 1L, 2L);

            // then
            assertThat(existsBySlot).isFalse();
        }
    }

    @Test
    void 예약을_수정한다() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        boolean updated = reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 6),
                ReservationTime.of(2L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
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
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
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

        assertThatThrownBy(() -> reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(2L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ))).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 존재하지_않는_예약_시간으로_수정하면_예외가_발생한다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        assertThatThrownBy(() -> reservationRepository.update(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(999L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
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
    void 예약을_삭제한다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        insertReservation("브라운", "2026-08-05", 1L, 1L);

        boolean deleted = reservationRepository.deleteById(1L);

        assertThat(deleted).isTrue();
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_false를_반환한다() {
        boolean deleted = reservationRepository.deleteById(1L);

        assertThat(deleted).isFalse();
        assertThat(reservationRepository.findAll()).isEmpty();
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
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name,
                date,
                timeId,
                themeId
        );
    }
}
