package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

@JdbcTest
class ReservationTimeRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    public ReservationTimeRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.reservationTimeRepository = new ReservationTimeRepository(new ReservationTimeDao(jdbcTemplate));
    }

    @Nested
    class Save {
        @Test
        @DisplayName("save persists a new reservation time and returns its id.")
        void save_validTime_returnsWithId() {
            // given
            LocalTime startTime = LocalTime.of(10, 0);
            ReservationTime time = new ReservationTime(startTime);

            // when
            ReservationTime savedTime = reservationTimeRepository.save(time);

            // then
            assertThat(savedTime.getId()).isNotNull();
            assertThat(savedTime.getStartAt()).isEqualTo(startTime);
        }

        @Test
        @DisplayName("save throws ConflictException when the time already exists.")
        void save_duplicateTime_throwsConflictException() {
            // given
            LocalTime startTime = LocalTime.of(10, 0);
            ReservationTime time = new ReservationTime(startTime);

            reservationTimeRepository.save(time);

            // when & then
            assertThatThrownBy(() -> reservationTimeRepository.save(time))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage(TimeErrorCode.DUPLICATE_TIME.getMessage());
        }
    }

    @Test
    @DisplayName("findById returns the persisted reservation time.")
    void findById_existingTime_returnsTime() {
        // given
        ReservationTime savedTime = createTime(LocalTime.of(11, 0));

        // when
        Optional<ReservationTime> result = reservationTimeRepository.findById(savedTime.getId());

        // then
        assertThat(result).contains(savedTime);
    }

    @Test
    @DisplayName("findAll returns all persisted times.")
    void findAll_multipleTimes_returnsAllTimes() {
        // given
        ReservationTime saved1 = createTime(LocalTime.of(10, 0));
        ReservationTime saved2 = createTime(LocalTime.of(11, 0));

        // when
        List<ReservationTime> times = reservationTimeRepository.findAll();

        // then
        assertThat(times).containsExactly(saved1, saved2);
    }

    @Test
    @DisplayName("queryAvailableTimes marks booked times as alreadyBooked.")
    void queryAvailableTimes() {
        // given
        ReservationTime time1 = createTime(LocalTime.of(10, 0));
        ReservationTime time2 = createTime(LocalTime.of(11, 0));
        ReservationTime time3 = createTime(LocalTime.of(12, 0));

        LocalDate date = LocalDate.of(2025, 1, 1);

        Long themeId = createTheme();
        createReservation(time1, date, themeId);

        // when
        List<AvailableTimeQueryResult> result = reservationTimeRepository.queryAvailableTimes(themeId, date);

        // then
        assertThat(result).hasSize(3);

        AvailableTimeQueryResult result1 = result.stream().filter(t -> t.id().equals(time1.getId())).findFirst().get();
        AvailableTimeQueryResult result2 = result.stream().filter(t -> t.id().equals(time2.getId())).findFirst().get();
        AvailableTimeQueryResult result3 = result.stream().filter(t -> t.id().equals(time3.getId())).findFirst().get();

        assertThat(result1.alreadyBooked()).isTrue();
        assertThat(result2.alreadyBooked()).isFalse();
        assertThat(result3.alreadyBooked()).isFalse();
    }

    @Test
    @DisplayName("delete removes the persisted time.")
    void delete_existingId_removesTime() {
        // given
        ReservationTime saved = createTime(LocalTime.of(10, 0));

        // when
        reservationTimeRepository.delete(saved);

        // then
        List<ReservationTime> all = reservationTimeRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("delete throws NotFoundException when the time does not exist.")
    void delete_nonExistentId_throwsNotFound() {
        // given
        ReservationTime missing = new ReservationTime(9999L, LocalTime.of(10, 0));

        // when & then
        assertThatThrownBy(() -> reservationTimeRepository.delete(missing))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("delete throws ConflictException when the time is in use.")
    void deleteById_timeInUse_throwsConflictException() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));

        Long themeId = createTheme();
        createReservation(time, LocalDate.of(2026, 5, 6), themeId);

        // when & then
        assertThatThrownBy(() -> reservationTimeRepository.delete(time))
                .isInstanceOf(ConflictException.class)
                .hasMessage(TimeErrorCode.TIME_IN_USE.getMessage());
    }

    private ReservationTime createTime(LocalTime time) {
        return reservationTimeRepository.save(new ReservationTime(time));
    }

    private Long createTheme() {
        jdbcTemplate.update(
                "insert into theme(name, description, thumbnail_url) values ('theme', 'description', 'url')"
        );

        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class,
                "theme"
        );
    }

    private void createReservation(ReservationTime time, LocalDate date, Long themeId) {
        jdbcTemplate.update(
                "insert into reservation(name, reservation_date, time_id, theme_id) values (?, ?, ?, ?)",
                "brown", java.sql.Date.valueOf(date), time.getId(), themeId
        );
    }
}
