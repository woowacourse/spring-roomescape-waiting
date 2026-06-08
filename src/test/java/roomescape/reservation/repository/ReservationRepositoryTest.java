package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.global.exception.ConflictException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@JdbcTest
class ReservationRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationRepository = new ReservationRepository(new ReservationDao(jdbcTemplate));
    }

    @Test
    @DisplayName("예약을 정상 저장하면 ID가 부여된다.")
    void save_validReservation_returnsWithId() {
        Reservation saved = reservationRepository.save(buildReservation("브라운"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("동일 예약이 있으면 저장 시 ConflictException이 발생한다.")
    void save_duplicateReservation_throwsConflictException() {
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마", "설명", "url");
        Reservation first = new Reservation("브라운", LocalDate.of(2026, 5, 1), time, theme,
                LocalDate.of(2026, 5, 1).atStartOfDay());
        reservationRepository.save(first);

        Reservation duplicate = new Reservation("브라운", LocalDate.of(2026, 5, 1), time, theme,
                LocalDate.of(2026, 5, 1).atStartOfDay());

        assertThatThrownBy(() -> reservationRepository.save(duplicate))
                .isInstanceOf(ConflictException.class)
                .hasMessage(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("ID로 예약을 조회할 수 있다.")
    void findById_existingReservation_returnsReservation() {
        Reservation saved = reservationRepository.save(buildReservation("브라운"));

        Optional<Reservation> found = reservationRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("이름으로 예약을 조회할 수 있다.")
    void findAllByName_returnsReservations() {
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마", "설명", "url");

        Reservation saved = reservationRepository.save(
                new Reservation("브라운", LocalDate.of(2026, 5, 1), time, theme, LocalDate.of(2026, 5, 1).atStartOfDay())
        );
        reservationRepository.save(
                new Reservation("포비", LocalDate.of(2026, 5, 2), time, theme, LocalDate.of(2026, 5, 2).atStartOfDay())
        );

        assertThat(reservationRepository.findAllByName("브라운"))
                .extracting(Reservation::getId)
                .containsExactly(saved.getId());
    }

    @Test
    @DisplayName("예약을 삭제하면 조회되지 않는다.")
    void delete_existingReservation_removesReservation() {
        Reservation saved = reservationRepository.save(buildReservation("브라운"));

        reservationRepository.delete(saved);

        assertThat(reservationRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("동일 시간 예약 여부를 확인할 수 있다.")
    void hasBookingAtSameTime_returnsTrueIfSameUserHasBooking() {
        Reservation saved = reservationRepository.save(buildReservation("브라운"));
        assertThat(reservationRepository.hasBookingAtSameTime("브라운", saved.getSlot())).isTrue();
        assertThat(reservationRepository.hasBookingAtSameTime("포비", saved.getSlot())).isFalse();
    }

    private Reservation buildReservation(String name) {
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마", "설명", "url");
        return new Reservation(name, LocalDate.of(2026, 5, 1), time, theme, LocalDate.of(2026, 5, 1).atStartOfDay());
    }

    private ReservationTime createTime(LocalTime time) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", Time.valueOf(time));
        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = ?", Long.class,
                Time.valueOf(time));
        return new ReservationTime(timeId, time);
    }

    private Theme createTheme(String name, String description, String thumbnailUrl) {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", name, description,
                thumbnailUrl);
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, name);
        return new Theme(themeId, name, description, thumbnailUrl);
    }
}
