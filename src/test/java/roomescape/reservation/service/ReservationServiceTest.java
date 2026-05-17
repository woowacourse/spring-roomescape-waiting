package roomescape.reservation.service;

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
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.domain.Theme;
import roomescape.test_config.MutableClock;
import roomescape.test_config.TestClockConfig;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;
import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;

@JdbcTest
@Import({
        TestClockConfig.class,
        ReservationService.class,
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class
})
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    MutableClock clock;


    @Test
    @DisplayName("이미 같은 날짜, 시간, 테마의 예약이 존재하면 예외가 발생한다.")
    public void create_fail1() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2023, 8, 5);
        insertReservation("브라운", date, time, theme);

        // when, then
        assertThatThrownBy(() -> reservationService.create("포비", date, time.getId(), theme.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_ALREADY_EXISTS.message());
    }

    @Test
    @DisplayName("이미 지난 날짜 및 시간으로 예약하려는 경우 예외가 발생한다.")
    public void create_fail2() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate pastDate = LocalDate.of(2023, 8, 5);
        LocalDate currentDate = LocalDate.of(2025, 5, 11);

        clock.setFixed(currentDate);

        // when, then
        assertThatThrownBy(() -> reservationService.create("포비", pastDate, time.getId(), theme.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessage(PAST_RESERVATION_NOT_ALLOWED.message());
    }

    @Test
    @DisplayName("해당 예약이 존재하지 않으면 취소할 수 없기 때문에 예외가 발생한다.")
    public void cancel_fail() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> reservationService.cancel(id))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("본인의 예약을 삭제한다.")
    public void cancelMine_success() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Reservation reservation = insertReservation(LocalDate.of(2023, 8, 10), time, "브라운");

        // when
        reservationService.deleteMine(reservation.getId(), reservation.getGuestName());

        // then
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE id = ? AND deleted_at IS NULL
                """, Integer.class, reservation.getId());
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("해당 예약이 존재하지 않으면 본인의 예약을 삭제할 수 없기 때문에 예외가 발생한다.")
    public void cancelMine_fail1() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> reservationService.deleteMine(id, "브라운"))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("이미 시작된 예약은 삭제할 수 없다.")
    public void cancelMine_fail2() {
        // given
        clock.setFixed(LocalDate.of(2023, 8, 11));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Reservation reservation = insertReservation(LocalDate.of(2023, 8, 10), time, "브라운");

        // when, then
        assertThatThrownBy(() -> reservationService.deleteMine(reservation.getId(), reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_ALREADY_STARTED_RESERVATION.message());
    }

    @Test
    @DisplayName("본인의 예약이 아니면 삭제할 수 없기 때문에 예외가 발생한다.")
    public void cancelMine_fail3() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Reservation reservation = insertReservation(LocalDate.of(2023, 8, 10), time, "브라운");

        // when, then
        assertThatThrownBy(() -> reservationService.deleteMine(reservation.getId(), "포비"))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_OTHER_GUEST_RESERVATION.message());
    }

    @Test
    @DisplayName("예약의 날짜 및 시간을 수정한다.")
    public void editDateTime_success() {
        // given
        ReservationTime existTime = insertReservationTime(LocalTime.of(10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 5);
        Reservation reservation = insertReservation(existDate, existTime, "브라운");

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        clock.setFixed(LocalDate.of(2023, 7, 20));

        // when
        Reservation editedReservation =
                reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(), reservation.getGuestName());

        // then
        assertThat(editedReservation)
                .extracting(Reservation::getDate, r -> r.getTime().getId())
                .containsExactly(editedDate, editedTime.getId());
    }

    @Test
    @DisplayName("수정하려는 예약이 존재하지 않으면 예외가 발생한다.")
    public void editDateTime_fail1() {
        // given
        Long reservationId = 1L;
        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservationId, editedDate, editedTime.getId(), "브라운"))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("수정하려는 예약 시간이 존재하지 않으면 예외가 발생한다.")
    public void editDateTime_fail2() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 20));

        ReservationTime existTime = insertReservationTime(LocalTime.of(10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 5);
        Reservation reservation = insertReservation(existDate, existTime, "브라운");

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        Long editedTimeId = 999L;

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), editedDate, editedTimeId, reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_TIME_NOT_FOUND.message());
    }

    @Test
    @DisplayName("이미 시작된 예약은 수정할 수 없다.")
    public void editDateTime_fail3() {
        // given
        clock.setFixed(LocalDate.of(2023, 8, 6));

        ReservationTime existTime = insertReservationTime(LocalTime.of(10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 5);
        Reservation reservation = insertReservation(existDate, existTime, "브라운");

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(), reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_ALREADY_STARTED_RESERVATION.message());
    }

    @Test
    @DisplayName("수정하려는 날짜 및 시간에 예약이 존재하면 예외가 발생한다.")
    public void editDateTime_fail4() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(10, 0));

        insertReservation("브라운", editedDate, editedTime, theme);

        LocalDate existDate = LocalDate.of(2023, 8, 6);
        ReservationTime existTime = insertReservationTime(LocalTime.of(12, 0));
        Reservation reservation = insertReservation("포비", existDate, existTime, theme);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(), reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_ALREADY_EXISTS.message());
    }

    @Test
    @DisplayName("수정하려는 날짜 및 시간에 예약이 존재는 하는데 그게 본인의 예약인 경우 예외가 발생하지 않는다.")
    public void editDateTime_fail4_2() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2023, 8, 10);
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));

        Reservation reservation = insertReservation("브라운", date, time, theme);

        // when then
        assertThatCode(() -> reservationService.editDateTime(reservation.getId(), date, time.getId(), reservation.getGuestName()))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "2023-07-05, 10:00", // 날짜가 지난 경우
            "2023-07-06, 09:59", // 시간이 지난 경우
    })
    @DisplayName("이미 지난 날짜 및 시간으로 예약을 수정하려는 경우 예외가 발생한다.")
    public void editDateTime_fail5(LocalDate ed, LocalTime et) {
        // given
        clock.setFixed(LocalDateTime.of(2023, 7, 6, 10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 6);
        ReservationTime existTime = insertReservationTime(LocalTime.of(12, 0));
        Reservation reservation = insertReservation(existDate, existTime, "브라운");

        ReservationTime editedTime = insertReservationTime(et);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), ed, editedTime.getId(), reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(PAST_RESERVATION_NOT_ALLOWED.message());
    }

    @Test
    @DisplayName("본인의 예약이 아니면 예외가 발생한다.")
    public void editDateTime_fail6() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));

        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 10), time, theme);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(
                reservation.getId(), reservation.getDate(), time.getId(), "other_guest"))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_OTHER_GUEST_RESERVATION.message());
    }

    private Reservation insertReservation(LocalDate existDate, ReservationTime existTime, String guestName) {
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        return insertReservation(guestName, existDate, existTime, theme);
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

        return new ReservationTime(getGeneratedId(keyHolder), startAt);
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

        return new Theme(getGeneratedId(keyHolder), name, description, thumbnail);
    }

    private Reservation insertReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO reservation (guest_name, date, time_id, theme_id)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            preparedStatement.setString(1, name);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, time.getId());
            preparedStatement.setLong(4, theme.getId());
            return preparedStatement;
        }, keyHolder);

        return new Reservation(getGeneratedId(keyHolder), name, date, time, theme);
    }

    private Long getGeneratedId(KeyHolder keyHolder) {
        return keyHolder.getKey().longValue();
    }
}
