package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeDao;
import roomescape.service.dto.AvailableTimeResult;

@JdbcTest
@Import(ReservationTimeDao.class)
class ReservationTimeDaoTest {

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("예약 시간을 저장하고 전체 조회하면 시작 시각으로 정렬된 도메인 객체를 반환한다.")
    @Test
    void saveAndFindAll() {
        reservationTimeDao.save(LocalTime.of(11, 0));
        reservationTimeDao.save(LocalTime.of(10, 0));

        List<ReservationTime> times = reservationTimeDao.findAll();

        assertThat(times)
                .extracting(ReservationTime::getStartAt)
                .containsExactly(LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    @DisplayName("예약 시간은 유니크 제약조건을 가진다.")
    @Test
    void uniqueStartAt() {
        reservationTimeDao.save(LocalTime.of(10, 0));

        assertThatThrownBy(() -> reservationTimeDao.save(LocalTime.of(10, 0)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("시작 시각으로 예약 시간 존재 여부를 조회한다.")
    @Test
    void existsByStartAt() {
        reservationTimeDao.save(LocalTime.of(10, 0));

        assertThat(reservationTimeDao.existsByStartAt(LocalTime.of(10, 0))).isTrue();
        assertThat(reservationTimeDao.existsByStartAt(LocalTime.of(11, 0))).isFalse();
    }

    @DisplayName("이용 가능 시간 조회는 취소되지 않은 예약 수를 함께 반환한다.")
    @Test
    void findAvailableTimes() {
        Long ten = reservationTimeDao.save(LocalTime.of(10, 0));
        Long eleven = reservationTimeDao.save(LocalTime.of(11, 0));
        Long themeId = insertTheme("잠긴 방");
        Long scheduleId = insertSchedule(LocalDate.of(2026, 7, 1), ten, themeId);
        insertReservation("확정자", scheduleId, ReservationStatus.RESERVED);
        insertReservation("대기자", scheduleId, ReservationStatus.WAITING);
        insertReservation("취소자", scheduleId, ReservationStatus.CANCELED);

        List<AvailableTimeResult> results = reservationTimeDao.findAvailableTimes(
                themeId,
                LocalDate.of(2026, 7, 1),
                ReservationStatus.CANCELED
        );

        assertThat(results).hasSize(2);
        assertThat(findById(results, ten).reservationCount()).isEqualTo(2);
        assertThat(findById(results, eleven).reservationCount()).isZero();
    }

    @DisplayName("예약 시간을 삭제하면 조회 결과에서 제외된다.")
    @Test
    void delete() {
        Long id = reservationTimeDao.save(LocalTime.of(10, 0));

        reservationTimeDao.delete(id);

        assertThat(reservationTimeDao.findAll()).isEmpty();
    }

    private AvailableTimeResult findById(List<AvailableTimeResult> results, Long id) {
        return results.stream()
                .filter(result -> result.id() == id)
                .findFirst()
                .orElseThrow();
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)",
                name,
                name + " 설명",
                "https://example.com/" + name + ".jpg",
                20000
        );
        return jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, name);
    }

    private Long insertSchedule(LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update("INSERT INTO schedule (date, time_id, theme_id) VALUES (?, ?, ?)", date, timeId, themeId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM schedule WHERE date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                date,
                timeId,
                themeId
        );
    }

    private void insertReservation(String name, Long scheduleId, ReservationStatus status) {
        Long userId = insertUser(name);
        jdbcTemplate.update(
                "INSERT INTO reservation (user_id, schedule_id, status, updated_at) VALUES (?, ?, ?, ?)",
                userId,
                scheduleId,
                status.name(),
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );
    }

    private Long insertUser(String name) {
        String loginId = name + "-" + System.nanoTime();
        jdbcTemplate.update(
                "INSERT INTO users (login_id, name, password, role) VALUES (?, ?, ?, ?)",
                loginId,
                name,
                "password",
                "USER"
        );
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE login_id = ?", Long.class, loginId);
    }
}
