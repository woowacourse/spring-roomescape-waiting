package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

@JdbcTest
class JdbcReservationTimeRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    public JdbcReservationTimeRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @Nested
    class save {
        @Test
        @DisplayName("새로운 시간 정보를 저장하고 반환된 객체의 ID를 확인한다.")
        void save_validTime_returnsWithId() {
            // given
            LocalTime startTime = LocalTime.of(10, 0);
            ReservationTime time = ReservationTime.of(startTime);

            // when
            ReservationTime savedTime = reservationTimeRepository.save(time);

            //then
            assertThat(savedTime.getId()).isNotNull();
            assertThat(savedTime.getStartAt()).isEqualTo(startTime);
        }

        @Test
        @DisplayName("기존에 이미 해당 시간이 있으면 예외가 발생한다.")
        void save_duplicateTime_throwsDataIntegrityViolation() {
            // given
            LocalTime startTime = LocalTime.of(10, 0);
            ReservationTime time = ReservationTime.of(startTime);

            reservationTimeRepository.save(time);

            // when & then
            assertThatThrownBy(
                    () -> reservationTimeRepository.save(time)
            ).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Test
    @DisplayName("ID를 통해 저장된 시간 정보를 정확히 조회한다.")
    void findById_existingTime_returnsTime() {
        // given
        ReservationTime savedTime = createTime(LocalTime.of(11, 0));

        // when
        Optional<ReservationTime> result = reservationTimeRepository.findById(savedTime.getId());

        // then
        assertTrue(result.isPresent());
        assertThat(result.get()).isEqualTo(savedTime);
    }

    @Test
    @DisplayName("해당 시간이 저장돼 있는지 조회한다.")
    void existsByStartAt() {
        //given
        createTime(LocalTime.of(11, 0));

        //when & then
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(11, 0)))
                .isTrue();

        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(12, 0)))
                .isFalse();
    }

    @Test
    @DisplayName("존재하는 모든 시간 목록을 리스트로 조회한다.")
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
    @DisplayName("모든 시간을 조회하고, 예약된 시간은 alreadyBooked=true 로 반환한다")
    void findAvailableTimes() {
        // given
        ReservationTime time1 = createTime(LocalTime.of(10, 0));
        ReservationTime time2 = createTime(LocalTime.of(11, 0));
        ReservationTime time3 = createTime(LocalTime.of(12, 0));

        LocalDate date = LocalDate.of(2025, 1, 1);

        Long themeId = createTheme();
        createReservation(time1, date, themeId);

        // when
        List<AvailableTimeQueryResult> result = reservationTimeRepository.findAvailableTimes(themeId, date);

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
    @DisplayName("시간 정보를 삭제한다.")
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
    @DisplayName("다른 객체가 참조하고 있으면 예외가 발생한다.")
    void deleteById_timeInUse_throwsDataIntegrityViolation() {
        //given
        ReservationTime time = createTime(LocalTime.of(10, 0));

        Long themeId = createTheme();
        createReservation(time, LocalDate.of(2026, 5, 6), themeId);

        //when & then
        assertThatThrownBy(
                () -> reservationTimeRepository.delete(time)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    private ReservationTime createTime(LocalTime time) {
        return reservationTimeRepository.save(
                ReservationTime.of(time)
        );
    }

    private Long createTheme() {
        jdbcTemplate.update(
                "insert into theme(name, description, thumbnail_url) values ('테마', '설명', 'url')"
        );

        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class,
                "테마"
        );
    }

    private void createReservation(ReservationTime time, LocalDate date, Long themeId) {
        jdbcTemplate.update(
                "insert into reservation(name, reservation_date, time_id, theme_id) values (?, ?, ?, ?)",
                "브라운", java.sql.Date.valueOf(date), time.getId(), themeId
        );
    }
}
