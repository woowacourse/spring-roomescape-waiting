package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.test_config.MutableClock;
import roomescape.test_config.TestClockConfig;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@JdbcTest
@Import({JdbcReservationRepository.class, TestClockConfig.class})
class JdbcReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MutableClock clock;


    @Test
    @DisplayName("id로 특정 예약을 조회한다.")
    public void findById() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        // when
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservation.getId());

        // then
        assertThat(optionalReservation).isPresent();
        Reservation found = optionalReservation.get();
        assertThat(found)
                .extracting(
                        Reservation::getId, Reservation::getGuestName, Reservation::getDate,
                        Reservation::getTime, Reservation::getTheme
                ).containsExactly(
                        reservation.getId(), reservation.getGuestName(), reservation.getDate(),
                        reservation.getTime(), reservation.getTheme()
                );
    }

    @Test
    @DisplayName("id로 특정 예약을 조회는 deleted_at이 null인 정보만 조회할 수 있다.")
    public void findById_softDelete() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        // 삭제처리된 데이터
        Reservation reservation = insertDeletedReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        // when
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservation.getId());

        // then
        assertThat(optionalReservation).isEmpty();
    }

    @Test
    @DisplayName("예약의 목록을 페이지 단위로 조회한다")
    void findAllWithPaging() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);
        Reservation reservation2 = insertReservation("포비", LocalDate.of(2023, 8, 6), time, theme);
        insertReservation("조이", LocalDate.of(2023, 8, 7), time, theme);

        // when
        List<Reservation> reservations = reservationRepository.findAll(2, 1);

        // then
        assertThat(reservations)
                .extracting(Reservation::getId, Reservation::getGuestName, Reservation::getDate)
                .containsExactly(
                        tuple(reservation2.getId(), "포비", LocalDate.of(2023, 8, 6))
                );
    }

    @Test
    @DisplayName("예약 목록은 삭제되지 않은 예약만 조회할 수 있다.")
    void findAllWithPaging_softDelete() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation deletedReservation = insertDeletedReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);
        Reservation notDeletedReservation1 = insertReservation("포비", LocalDate.of(2023, 8, 6), time, theme);
        Reservation notDeletedReservation2 = insertReservation("조이", LocalDate.of(2023, 8, 7), time, theme);

        // when
        List<Reservation> reservations = reservationRepository.findAll(1, 3);

        // then
        assertThat(reservations).hasSize(2)
                .contains(notDeletedReservation1, notDeletedReservation2)
                .doesNotContain(deletedReservation);
    }

    @Test
    @DisplayName("예약자 이름으로 예약 정보를 조회한다.")
    public void findByGuest() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        // when
        List<Reservation> reservations = reservationRepository.findByGuestName(reservation.getGuestName());

        // then
        assertThat(reservations)
                .extracting(
                        Reservation::getId, Reservation::getGuestName, Reservation::getDate,
                        Reservation::getTime, Reservation::getTheme
                ).containsExactly(
                        tuple(reservation.getId(), reservation.getGuestName(), reservation.getDate(),
                                reservation.getTime(), reservation.getTheme())
                );
    }

    @Test
    @DisplayName("예약자 이름으로 예약 정보를 조회할 때, 삭제되지 않은 예약만 조회할 수 있다.")
    public void findByGuest_softdelete() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertDeletedReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        // when
        List<Reservation> reservations = reservationRepository.findByGuestName(reservation.getGuestName());

        // then
        assertThat(reservations)
                .extracting(Reservation::getId).doesNotContain(reservation.getId());
    }

    @Test
    @DisplayName("예약을 저장한다.")
    public void save() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = Reservation.create("브라운", LocalDate.of(2023, 8, 5), time, theme);

        // when
        Reservation saved = reservationRepository.save(reservation);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved)
                .extracting(
                        Reservation::getGuestName, Reservation::getDate,
                        Reservation::getTime, Reservation::getTheme
                ).containsExactly(
                        reservation.getGuestName(), reservation.getDate(),
                        reservation.getTime(), reservation.getTheme()
                );
    }

    @Test
    @DisplayName("예약의 날짜 및 시간을 수정한다.")
    public void updateDateAndTime() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        LocalDate updatedDate = LocalDate.of(2023, 9, 7);
        ReservationTime updatedTime = insertReservationTime(LocalTime.of(12, 0));

        // when
        boolean result = reservationRepository.updateDateAndTime(reservation.getId(), updatedDate, updatedTime.getId());

        // then
        assertThat(result).isTrue();

        Map<String, Object> map = findDateAndTimeIdById(reservation.getId());
        LocalDate date = ((Date) map.get("date")).toLocalDate();
        Long timeId = ((Number) map.get("time_id")).longValue();
        assertThat(date).isEqualTo(updatedDate);
        assertThat(timeId).isEqualTo(updatedTime.getId());
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마를 가진 예약이 존재하는지 확인한다.")
    public void existsByDateAndTimeIdAndThemeId() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = insertReservationTime(LocalTime.of(12, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Theme theme2 = insertTheme("레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        insertReservation("브라운", targetDate, time, theme);

        // when
        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeId(targetDate, time.getId(), theme.getId());
        boolean notExists = reservationRepository.existsByDateAndTimeIdAndThemeId(targetDate, time2.getId(), theme2.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마를 가진 예약이 삭제된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existsByDateAndTimeIdAndThemeId_softDelete() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        insertDeletedReservation("브라운", targetDate, time, theme);

        // when
        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeId(
                targetDate, time.getId(), theme.getId());

        // then
        assertThat(exists).isFalse();
    }


    @ParameterizedTest
    @CsvSource({
            "true, false",
            "false, true"
    })
    @DisplayName("특정 예약이 아닌 예약 중에서 특정 날짜, 시간, 테마가 겹치는 예약이 존재하는지 확인한다.")
    public void existsByDateAndTimeIdAndThemeIdAndIdNot(
            boolean isTargetReservationId, boolean expected
    ) {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        ReservationTime otherTime = insertReservationTime(LocalTime.of(12, 0));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Theme otherTheme = insertTheme("레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png");

        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        Reservation targetReservation = insertReservation("브라운", targetDate, time, theme);
        Reservation otherReservation = insertReservation("제이", targetDate, otherTime, otherTheme);

        Long excludedReservationId = isTargetReservationId
                ? targetReservation.getId()
                : otherReservation.getId();

        // when
        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                targetDate, time.getId(), theme.getId(), excludedReservationId);

        // then
        assertThat(exists).isEqualTo(expected);
    }

    @Test
    @DisplayName("특정 예약이 아닌 예약 중에서 겹치는 예약이 삭제된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existsByDateAndTimeIdAndThemeIdAndIdNot_softDelete() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        Reservation deletedReservation = insertDeletedReservation("브라운", targetDate, time, theme);

        // when
        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                targetDate, time.getId(), theme.getId(), deletedReservation.getId() + 1);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("예약을 취소하면 deleted_at이 현재시간, delete_token이 해당 데이터의 pk값으로 설정된다.")
    public void cancelById_success() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 10, 0);
        clock.setFixed(now);

        // when
        boolean result = reservationRepository.cancelById(reservation.getId());

        // then
        assertThat(result).isTrue();
        Map<String, Object> reservationMap = findDeleteAtAndDeleteToken(reservation.getId());
        assertThat(((Timestamp) reservationMap.get("deleted_at")).toLocalDateTime()).isEqualTo(now);
        assertThat(((Long) reservationMap.get("delete_token"))).isEqualTo(reservation.getId());
    }

    @Test
    @DisplayName("존재하지 않는 예약은 취소되지 않는다.")
    public void cancelById_fail() {
        // given
        Long id = 1L;

        // when
        boolean result = reservationRepository.cancelById(id);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("특정 예약 시간 id를 가진 예약이 존재하는지 확인한다.")
    public void existByTimeId() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = insertReservationTime(LocalTime.of(12, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        // when
        boolean exists = reservationRepository.existByTimeId(time.getId());
        boolean notExists = reservationRepository.existByTimeId(time2.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 예약 시간 id를 가진 예약이 삭제된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existByTimeId_softDelete() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertDeletedReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        // when
        boolean exists = reservationRepository.existByTimeId(time.getId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 테마 id를 가진 예약이 존재하는지 확인한다.")
    public void existByThemeId() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Theme theme2 = insertTheme("레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        // when
        boolean exists = reservationRepository.existByThemeId(theme.getId());
        boolean notExists = reservationRepository.existByThemeId(theme2.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 테마 id를 가진 예약이 삭제된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existByThemeId_softDelete() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertDeletedReservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        // when
        boolean exists = reservationRepository.existByThemeId(theme.getId());

        // then
        assertThat(exists).isFalse();
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

    private Reservation insertReservation(String guestName, LocalDate date, ReservationTime time, Theme theme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (guest_name, date, time_id, theme_id)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, guestName);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, time.getId());
            preparedStatement.setLong(4, theme.getId());
            return preparedStatement;
        }, keyHolder);

        return Reservation.of(getGeneratedId(keyHolder), guestName, date, time, theme);
    }

    private Reservation insertDeletedReservation(String guestName, LocalDate date, ReservationTime time, Theme theme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (guest_name, date, time_id, theme_id, deleted_at)
                    VALUES (?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, guestName);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, time.getId());
            preparedStatement.setLong(4, theme.getId());

            preparedStatement.setTimestamp(5, Timestamp.valueOf(now));
            return preparedStatement;
        }, keyHolder);

        return Reservation.of(getGeneratedId(keyHolder), guestName, date, time, theme);
    }

    private Map<String, Object> findDateAndTimeIdById(Long id) {
        return jdbcTemplate.queryForMap("""
                SELECT
                    r.date,
                    time_id
                FROM reservation r
                WHERE r.id = ?
                """, id);
    }

    private Map<String, Object> findDeleteAtAndDeleteToken(Long id) {
        return jdbcTemplate.queryForMap("""
                SELECT
                    deleted_at, delete_token
                FROM reservation r
                WHERE r.id = ?
                """, id);
    }


    private Long getGeneratedId(KeyHolder keyHolder) {
        return keyHolder.getKey().longValue();
    }
}
