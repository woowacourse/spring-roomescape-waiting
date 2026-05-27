package roomescape.reservationtime.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.reservation.domain.Status;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.dto.ReservationTimeAvailability;
import roomescape.theme.domain.Theme;

@JdbcTest
@Import(JdbcReservationTimeRepository.class)
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    @DisplayName("예약 시간을 저장한다.")
    void save() {

        // given
        ReservationTime reservationTime = ReservationTime.create(LocalTime.of(10, 0));

        // given
        ReservationTime saved = reservationTimeRepository.save(reservationTime);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("삭제된 예약 시간은 id로 조회되지 않는다.")
    public void findById_softDelete() {
        // given
        ReservationTime reservationTime = insertDeletedReservationTime(LocalTime.of(10, 0));

        // when
        Optional<ReservationTime> found = reservationTimeRepository.findById(reservationTime.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 시간 목록을 조회한다")
    void findAll() {
        insertReservationTime(LocalTime.of(10, 0));

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        assertThat(reservationTimes).hasSize(1);
        assertThat(reservationTimes.getFirst().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("예약 시간 목록은 삭제되지 않은 예약 시간만 조회한다.")
    public void findAll_softDelete() {
        // given
        insertDeletedReservationTime(LocalTime.of(10, 0));
        ReservationTime activeTime = insertReservationTime(LocalTime.of(12, 0));

        // when
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        // then
        assertThat(reservationTimes)
                .extracting(ReservationTime::getId, ReservationTime::getStartAt)
                .containsExactly(Tuple.tuple(activeTime.getId(), activeTime.getStartAt()));
    }

    @Test
    void 예약_시간_존재_여부를_조회한다() {
        insertReservationTime(LocalTime.of(10, 0));

        boolean exists = reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0));

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("삭제된 예약 시간은 존재하지 않는 것으로 조회한다.")
    public void existsByStartAt_softDelete() {
        // given
        insertDeletedReservationTime(LocalTime.of(10, 0));

        // when
        boolean exists = reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0));

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("예약 시간을 삭제한다.")
    void delete_success() {
        // given
        ReservationTime reservationTime = insertReservationTime(LocalTime.of(10, 0));
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 10, 0);

        // when
        boolean deleted = reservationTimeRepository.cancelById(reservationTime.getId(), now);

        // then
        assertThat(deleted).isTrue();
        assertThat(reservationTimeRepository.findAll()).isEmpty();

        Map<String, Object> deleteInfo = findDeleteInfoById(reservationTime.getId());
        assertThat(((Timestamp) deleteInfo.get("deleted_at")).toLocalDateTime()).isEqualTo(now);
        assertThat(((Number) deleteInfo.get("delete_token")).longValue()).isEqualTo(reservationTime.getId());
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간은 삭제되지 않는다.")
    public void cancelById_fail() {
        // given
        Long id = 1L;

        // when
        boolean deleted = reservationTimeRepository.cancelById(id, LocalDateTime.now());

        // then
        assertThat(deleted).isFalse();
    }

    @Test
    @DisplayName("이용 가능한 시간을 조회한다.")
    public void findAllByDateAndThemeIdWithAvailability() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = insertReservationTime(LocalTime.of(12, 0));
        Theme targetTheme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Theme nonTargetTheme = insertTheme("레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png");

        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        insertReservation("브라운", targetDate, time, targetTheme);
        insertReservation("브라운", LocalDate.of(2024, 9, 10), time, targetTheme);
        insertReservation("브라운", targetDate, time, nonTargetTheme);

        // when
        List<ReservationTimeAvailability> availableTimes = reservationTimeRepository.findAllByDateAndThemeIdWithAvailability(
                targetDate, targetTheme.getId());

        // then
        assertThat(availableTimes).hasSize(2)
                .extracting(ReservationTimeAvailability::getReservationTime,
                        ReservationTimeAvailability::isAvailable)
                .containsExactly(Tuple.tuple(time, false), Tuple.tuple(time2, true));
    }

    @Test
    @DisplayName("삭제된 예약은 이용 가능한 시간 조회에서 제외한다.")
    public void findAllByDateAndThemeIdWithAvailability_softDelete() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2023, 8, 5);
        insertDeletedReservation("브라운", date, time, theme);

        // when
        List<ReservationTimeAvailability> availableTimes =
                reservationTimeRepository.findAllByDateAndThemeIdWithAvailability(date, theme.getId());

        // then
        assertThat(availableTimes).hasSize(1)
                .extracting(ReservationTimeAvailability::getReservationTime,
                        ReservationTimeAvailability::isAvailable)
                .containsExactly(Tuple.tuple(time, true));
    }

    @Test
    @DisplayName("삭제된 예약 시간은 이용 가능한 시간 조회에서 제외한다.")
    public void findAllByDateAndThemeIdWithAvailability_deletedReservationTime() {
        // given
        insertDeletedReservationTime(LocalTime.of(10, 0));
        ReservationTime activeTime = insertReservationTime(LocalTime.of(12, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2023, 8, 5);

        // when
        List<ReservationTimeAvailability> availableTimes =
                reservationTimeRepository.findAllByDateAndThemeIdWithAvailability(date, theme.getId());

        // then
        assertThat(availableTimes).hasSize(1)
                .extracting(ReservationTimeAvailability::getReservationTime,
                        ReservationTimeAvailability::isAvailable)
                .containsExactly(Tuple.tuple(activeTime, true));
    }

    private ReservationTime insertReservationTime(LocalTime startAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation_time (start_at)
                    VALUES (?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, startAt.toString());
            return preparedStatement;
        }, keyHolder);

        return ReservationTime.of(getGeneratedId(keyHolder), startAt);
    }

    private ReservationTime insertDeletedReservationTime(LocalTime startAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation_time (start_at, deleted_at)
                    VALUES (?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, startAt.toString());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            return preparedStatement;
        }, keyHolder);

        return ReservationTime.of(getGeneratedId(keyHolder), startAt);
    }

    private Theme insertTheme(String name, String description, String thumbnail) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO theme (name, description, thumbnail)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, thumbnail);
            return preparedStatement;
        }, keyHolder);

        return Theme.of(getGeneratedId(keyHolder), name, description, thumbnail);
    }

    private void insertReservation(String guestName, LocalDate date, ReservationTime time, Theme theme) {
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (guest_name, date, time_id, theme_id, status)
                    VALUES (?, ?, ?, ?, ?)
                    """);
            preparedStatement.setString(1, guestName);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, time.getId());
            preparedStatement.setLong(4, theme.getId());
            preparedStatement.setString(5, Status.WAITING.toString());
            return preparedStatement;
        });
    }

    private void insertDeletedReservation(String guestName, LocalDate date, ReservationTime time, Theme theme) {
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (guest_name, date, time_id, theme_id, status)
                    VALUES (?, ?, ?, ?, ?)
                    """);
            preparedStatement.setString(1, guestName);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, time.getId());
            preparedStatement.setLong(4, theme.getId());
            preparedStatement.setString(5, Status.CANCELED.toString());
            return preparedStatement;
        });
    }

    private Map<String, Object> findDeleteInfoById(Long id) {
        return jdbcTemplate.queryForMap("""
                SELECT deleted_at, delete_token
                FROM reservation_time
                WHERE id = ?
                """, id);
    }

    private Long getGeneratedId(KeyHolder keyHolder) {
        return keyHolder.getKey().longValue();
    }
}
