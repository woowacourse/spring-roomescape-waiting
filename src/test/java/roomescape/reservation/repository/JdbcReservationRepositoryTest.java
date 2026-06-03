package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWaitingResult;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@JdbcTest
@Import(JdbcReservationRepository.class)
class JdbcReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    @DisplayName("id로 특정 예약을 조회한다.")
    public void findById() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);

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
    @DisplayName("예약의 목록을 페이지 단위로 조회한다")
    void findAllWithPaging() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);
        Reservation reservation2 = insertReservation("포비", LocalDate.of(2023, 8, 6), time, theme, Status.WAITING);
        insertReservation("조이", LocalDate.of(2023, 8, 7), time, theme, Status.WAITING);

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
    @DisplayName("예약자 이름으로 예약 정보를 조회한다.")
    public void findByGuest() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);

        // when
        List<ReservationWaitingResult> reservationWaitingResults = reservationRepository.findAllByGuestName(
                reservation.getGuestName());

        // then
        assertThat(reservationWaitingResults)
                .extracting(
                        ReservationWaitingResult::id,
                        ReservationWaitingResult::guestName,
                        ReservationWaitingResult::date,
                        ReservationWaitingResult::time,
                        ReservationWaitingResult::theme
                ).containsExactly(
                        tuple(
                                reservation.getId(),
                                reservation.getGuestName(),
                                reservation.getDate(),
                                reservation.getTime(),
                                reservation.getTheme()
                        )
                );
    }

    @Test
    @DisplayName("예약을 저장한다.")
    public void save() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = Reservation.create("브라운", ReservationSlot.of(LocalDate.of(2023, 8, 5), time, theme), Status.WAITING);

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
    public void updateSlot() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);

        LocalDate updatedDate = LocalDate.of(2023, 9, 7);
        ReservationTime updatedTime = insertReservationTime(LocalTime.of(12, 0));

        // when
        boolean result = reservationRepository.updateSlot(
                reservation.getId(),
                ReservationSlot.of(updatedDate, updatedTime, theme),
                Status.WAITING
        );

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
    public void existsByDateAndTimeIdAndThemeIdAndGuestName() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = insertReservationTime(LocalTime.of(12, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        Theme theme2 = insertTheme("레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        insertReservation("브라운", targetDate, time, theme, Status.WAITING);

        // when
        boolean exists = reservationRepository.existsBySlotAndGuestNameExceptCanceled(
                ReservationSlot.of(targetDate, time, theme),
                "브라운"
        );
        boolean notExists = reservationRepository.existsBySlotAndGuestNameExceptCanceled(
                ReservationSlot.of(targetDate, time2, theme2),
                "브라운"
        );

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마를 가진 예약이 취소된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existsByDateAndTimeIdAndThemeId_AndGuestName_canceledReservation() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        insertReservation("브라운", targetDate, time, theme, Status.CANCELED);

        // when
        boolean exists = reservationRepository.existsBySlotAndGuestNameExceptCanceled(
                ReservationSlot.of(targetDate, time, theme), "브라운");

        // then
        assertThat(exists).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "target, other, false",
            "target, target, true"
    })
    @DisplayName("해당 예약을 제외하고 동일 슬롯에 활성 예약이 존재하는지 확인한다.")
    public void existsActiveReservationBySlotExceptGuestName(
            String target1,
            String target2,
            boolean expected
    ) {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        ReservationTime otherTime = insertReservationTime(LocalTime.of(12, 0));

        Theme theme = insertTheme(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://example.com/theme.png"
        );

        LocalDate targetDate = LocalDate.of(2023, 8, 5);

        insertReservation(target1, targetDate, time, theme, Status.CONFIRMED);
        insertReservation(target2, targetDate, otherTime, theme, Status.CONFIRMED);

        // when
        boolean exists = reservationRepository
                .existsBySlotAndGuestNameExceptCanceled(
                        ReservationSlot.of(targetDate, otherTime, theme),
                        target1
                );

        // then
        assertThat(exists).isEqualTo(expected);
    }

    @Test
    @DisplayName("특정 예약이 아닌 예약 중에서 겹치는 예약이 취소된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existsByDateAndTimeIdAndThemeIdAndIdNot_AndStatusCanceled_canceledReservationAndGuestName() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate targetDate = LocalDate.of(2023, 8, 5);
        insertReservation("브라운", targetDate, time, theme, Status.CANCELED);

        // when
        boolean exists = reservationRepository.existsBySlotAndGuestNameExceptCanceled(
                ReservationSlot.of(targetDate, time, theme), "브라운");

        // then
        assertThat(exists).isFalse();
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
    @DisplayName("같은 게스트의 같은 슬롯 취소 이력은 여러 개 저장할 수 있다.")
    public void allowDuplicateCanceledReservationsForSameGuestAndSlot() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2023, 8, 5);

        insertReservation("브라운", date, time, theme, Status.CANCELED);
        Reservation reservation = insertReservation("브라운", date, time, theme, Status.WAITING);

        // when
        boolean result = reservationRepository.cancelById(reservation.getId());

        // then
        assertThat(result).isTrue();
        assertThat(countReservationsByGuestName("브라운")).isEqualTo(2);
    }

    @Test
    @DisplayName("특정 예약 시간 id를 가진 예약이 존재하는지 확인한다.")
    public void existByTimeId() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = insertReservationTime(LocalTime.of(12, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);

        // when
        boolean exists = reservationRepository.existByTimeId(time.getId());
        boolean notExists = reservationRepository.existByTimeId(time2.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 예약 시간 id를 가진 예약이 취소된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existByTimeId_canceledReservation() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.CANCELED);

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
        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);

        // when
        boolean exists = reservationRepository.existByThemeId(theme.getId());
        boolean notExists = reservationRepository.existByThemeId(theme2.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 테마 id를 가진 예약이 취소된 예약이면 존재하지 않는 것으로 확인한다.")
    public void existByThemeId_canceledReservation() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.CANCELED);

        // when
        boolean exists = reservationRepository.existByThemeId(theme.getId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 테마 id를 가진 조회 시 순번을 반환해야한다.")
    public void findWaitingById() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.CONFIRMED);
        insertReservation("주디", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);
        Reservation reservation = insertReservation("초코칩", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);

        // when
        ReservationWaitingResult reservationWaitingResult = reservationRepository.findWaitingById(reservation.getId())
                .get();

        // then
        assertThat(reservationWaitingResult.waitNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("예약 조회에서 날짜, 시간, 테마가 같고 Confiremd인 것만 조회한다.")
    public void existsConfirmedReservationBySlot() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.CONFIRMED);
        insertReservation("주디", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);
        Reservation reservation = insertReservation("초코칩", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);

        // when
        boolean result = reservationRepository.existsConfirmedReservationBySlot(reservation.getSlot());

        // then
        assertThat(result).isTrue();
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

    private Reservation insertReservation(
            String guestName,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            Status status
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                INSERT INTO reservation (
                    guest_name,
                    date,
                    time_id,
                    theme_id,
                    status,
                    confirmed_token,
                    waiting_token
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, new String[]{"id"});

            preparedStatement.setString(1, guestName);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, time.getId());
            preparedStatement.setLong(4, theme.getId());
            preparedStatement.setString(5, status.name());
            preparedStatement.setObject(6, toConfirmedToken(status));
            preparedStatement.setObject(7, toWaitingToken(status));

            return preparedStatement;
        }, keyHolder);

        return Reservation.of(
                getGeneratedId(keyHolder),
                guestName,
                ReservationSlot.of(date, time, theme),
                status
        );
    }

    private Integer toConfirmedToken(Status status) {
        if (status.isConfirmed()) {
            return 1;
        }
        return null;
    }

    private Integer toWaitingToken(Status status) {
        if (status.isWaiting()) {
            return 1;
        }
        return null;
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

    private Integer countReservationsByGuestName(String guestName) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE guest_name = ?
                """, Integer.class, guestName);
    }

    private Long getGeneratedId(KeyHolder keyHolder) {
        return keyHolder.getKey().longValue();
    }
}
