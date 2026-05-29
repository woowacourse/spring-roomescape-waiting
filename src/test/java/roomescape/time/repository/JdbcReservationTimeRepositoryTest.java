package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.time.domain.ReservationTime;
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

    @Test
    @DisplayName("새로운 시간 정보를 저장하고 반환된 객체의 ID를 확인한다.")
    void saveTest() {
        // given
        LocalTime startTime = LocalTime.of(10, 0);
        ReservationTime time = ReservationTime.of(startTime);

        // when
        ReservationTime savedTime = reservationTimeRepository.save(time);

        //then
        assertAll(
                () -> assertThat(savedTime.getId()).isNotNull(),
                () -> assertThat(savedTime.getStartAt()).isEqualTo(startTime)
        );
    }

    @Test
    @DisplayName("기존에 이미 해당 시간이 있으면 예외가 발생한다.")
    void saveTest_duplicate() {
        // given
        LocalTime startTime = LocalTime.of(10, 0);
        ReservationTime time = ReservationTime.of(startTime);

        reservationTimeRepository.save(time);

        // when & then
        assertThatThrownBy(() ->  reservationTimeRepository.save(time))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("ID를 통해 시간 정보를 삭제한다.")
    void deleteByIdTest() {
        // given
        ReservationTime saved = createTime(LocalTime.of(10, 0));

        // when
        reservationTimeRepository.deleteById(saved.getId());

        // then
        List<ReservationTime> all = reservationTimeRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("ID가 사용되고 있으면 예외가 발생한다.")
    void deleteByIdTest_used() {
        //given
        ReservationTime time = createTime(LocalTime.of(10, 0));

        Long themeId = createTheme();
        createReservation(time, LocalDate.of(2026, 5, 6), themeId);

        //when & then
        assertThatThrownBy(
                () -> reservationTimeRepository.deleteById(time.getId())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("ID를 통해 저장된 시간 정보를 정확히 조회한다.")
    void findByIdTest() {
        // given
        ReservationTime savedTime = createTime(LocalTime.of(11, 0));

        // when
        ReservationTime foundTime = reservationTimeRepository.findById(savedTime.getId())
                .orElseThrow(() -> new AssertionError("조회된 결과가 없습니다. id: " + savedTime.getId()));

        // then
        assertThat(foundTime).isEqualTo(savedTime);
    }

    @DisplayName("해당 시간이 저장돼 있는지 조회한다.")
    @Test
    void existByStartAt() {
        //given
        createTime(LocalTime.of(11, 0));

        //when & then
        assertAll(
                () -> assertThat(reservationTimeRepository.existByStartAt(LocalTime.of(11, 0)))
                        .isTrue(),
                () -> assertThat(reservationTimeRepository.existByStartAt(LocalTime.of(12, 0)))
                        .isFalse()
        );
    }

    @Test
    @DisplayName("존재하는 모든 시간 목록을 리스트로 조회한다.")
    void findAllTest() {
        // given
        ReservationTime saved1 = createTime(LocalTime.of(10, 0));
        ReservationTime saved2 = createTime(LocalTime.of(11, 0));

        // when
        List<ReservationTime> times = reservationTimeRepository.findAll();

        // then
        assertThat(times).containsExactly(saved1, saved2);
    }

    @Test
    @DisplayName("예약되지 않은 시간만 조회된다.")
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
        List<LocalTime> times = result.stream()
                .map(AvailableTimeQueryResult::startAt).toList();

        assertThat(times).containsExactly(
                time2.getStartAt(),
                time3.getStartAt()
        );
    }

    @Test
    @DisplayName("예약 대기가 있는 시간은 예약 가능한 시간 목록에서 제외된다.")
    void findAvailableTimes_exclude_reservation_waiting() {
        // given
        ReservationTime time1 = createTime(LocalTime.of(10, 0));
        ReservationTime time2 = createTime(LocalTime.of(11, 0));
        ReservationTime time3 = createTime(LocalTime.of(12, 0));

        LocalDate date = LocalDate.of(2025, 1, 1);

        Long themeId = createTheme();
        createReservationWaiting(time1, date, themeId);
        createReservation(time2, date, themeId);

        // when
        List<AvailableTimeQueryResult> result = reservationTimeRepository.findAvailableTimes(themeId, date);

        // then
        List<LocalTime> times = result.stream()
                .map(AvailableTimeQueryResult::startAt)
                .toList();

        assertThat(times).containsExactly(time3.getStartAt());
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
        jdbcTemplate.update("""
            insert into reservation(name, reservation_date, time_id, theme_id)
            values (?, ?, ?, ?)
        """, "브라운", date, time.getId(), themeId
        );
    }

    private void createReservationWaiting(ReservationTime time, LocalDate date, Long themeId) {
        jdbcTemplate.update("""
            insert into reservation_waiting(name, reservation_date, time_id, theme_id)
            values (?, ?, ?, ?)
        """, "브라운", date, time.getId(), themeId
        );
    }
}
