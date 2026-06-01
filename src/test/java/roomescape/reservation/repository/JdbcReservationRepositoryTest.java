package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@JdbcTest
@Import(JdbcReservationRepository.class)
class JdbcReservationRepositoryTest {

    private static final LocalDate TARGET_DATE = LocalDate.of(2026, 6, 10);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("전체 예약 조회 시 id가 아닌 신청 순서 기준으로 예약 상태와 대기 순번을 계산한다.")
    void findAll_success_calculatesStatusAndWaitingRankByRequestOrder() {
        // given
        Long themeId = insertTheme("레벨2 탈출");
        Long timeId = insertReservationTime(LocalTime.of(10, 0));
        Long waitingReservationId = insertReservation("브라운", TARGET_DATE, timeId, themeId, 20L);
        Long reservedReservationId = insertReservation("레아", TARGET_DATE, timeId, themeId, 10L);

        // when
        List<Reservation> reservations = reservationRepository.findAll();

        // then
        assertThat(reservations)
                .extracting(Reservation::getId, Reservation::getStatus, Reservation::getWaitingRank)
                .containsExactly(
                        tuple(reservedReservationId, ReservationStatus.RESERVED, 0L),
                        tuple(waitingReservationId, ReservationStatus.WAITING, 1L)
                );
    }

    @Test
    @DisplayName("날짜와 테마로 예약 조회 시 같은 슬롯 안에서만 대기 순번을 계산한다.")
    void findByDateAndThemeId_success_calculatesWaitingRankPerSlot() {
        // given
        Long targetThemeId = insertTheme("레벨2 탈출");
        Long otherThemeId = insertTheme("레벨3 탈출");
        Long tenId = insertReservationTime(LocalTime.of(10, 0));
        Long twelveId = insertReservationTime(LocalTime.of(12, 0));

        insertReservation("브라운", TARGET_DATE, tenId, targetThemeId, 1L);
        insertReservation("레아", TARGET_DATE, tenId, targetThemeId, 2L);
        insertReservation("포비", TARGET_DATE, tenId, targetThemeId, 3L);
        insertReservation("제이슨", TARGET_DATE, twelveId, targetThemeId, 4L);
        insertReservation("그레이", TARGET_DATE.plusDays(1), tenId, targetThemeId, 5L);
        insertReservation("조이", TARGET_DATE, tenId, otherThemeId, 6L);

        // when
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(TARGET_DATE, targetThemeId);

        // then
        assertThat(reservations)
                .extracting(
                        Reservation::getName,
                        reservation -> reservation.getTime().getId(),
                        Reservation::getStatus,
                        Reservation::getWaitingRank
                )
                .containsExactly(
                        tuple("브라운", tenId, ReservationStatus.RESERVED, 0L),
                        tuple("레아", tenId, ReservationStatus.WAITING, 1L),
                        tuple("포비", tenId, ReservationStatus.WAITING, 2L),
                        tuple("제이슨", twelveId, ReservationStatus.RESERVED, 0L)
                );
    }

    @Test
    @DisplayName("id로 단건 조회해도 같은 슬롯의 다른 예약을 포함해 대기 순번을 계산한다.")
    void findById_success_calculatesWaitingRankWithSameSlotReservations() {
        // given
        Long themeId = insertTheme("레벨2 탈출");
        Long timeId = insertReservationTime(LocalTime.of(10, 0));
        insertReservation("브라운", TARGET_DATE, timeId, themeId, 1L);
        Long waitingReservationId = insertReservation("레아", TARGET_DATE, timeId, themeId, 2L);

        // when
        var foundReservation = reservationRepository.findById(waitingReservationId);

        // then
        assertThat(foundReservation)
                .isPresent()
                .get()
                .satisfies(reservation -> {
                    assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.WAITING);
                    assertThat(reservation.getWaitingRank()).isEqualTo(1L);
                });
    }

    @Test
    @DisplayName("존재하지 않는 id로 단건 조회하면 빈 값을 반환한다.")
    void findById_success_returnsEmptyWhenReservationNotFound() {
        // when
        var foundReservation = reservationRepository.findById(1L);

        // then
        assertThat(foundReservation).isEmpty();
    }

    @Test
    @DisplayName("이름으로 예약 조회 시 활성 예약과 취소 이력을 함께 반환한다.")
    void findByName_success_returnsActiveReservationsAndCanceledHistories() {
        // given
        Long themeId = insertTheme("레벨2 탈출");
        Long tenId = insertReservationTime(LocalTime.of(10, 0));
        Long twelveId = insertReservationTime(LocalTime.of(12, 0));

        insertReservation("브라운", TARGET_DATE, tenId, themeId, 1L);
        Long activeWaitingReservationId = insertReservation("레아", TARGET_DATE, tenId, themeId, 2L);
        Long canceledReservationId = insertReservation("레아", TARGET_DATE, twelveId, themeId, 3L);
        deleteReservation(canceledReservationId);
        insertReservationHistory(canceledReservationId, "레아", TARGET_DATE, twelveId, themeId, 3L);

        // when
        List<Reservation> reservations = reservationRepository.findByName("레아");

        // then
        assertThat(reservations)
                .extracting(Reservation::getId, Reservation::getStatus, Reservation::getWaitingRank)
                .containsExactly(
                        tuple(activeWaitingReservationId, ReservationStatus.WAITING, 1L),
                        tuple(canceledReservationId, ReservationStatus.CANCELED, null)
                );
    }

    @Test
    @DisplayName("이름으로 예약 조회 시 활성 예약이 없어도 취소 이력만 반환한다.")
    void findByName_success_returnsOnlyCanceledHistories() {
        // given
        Long themeId = insertTheme("레벨2 탈출");
        Long timeId = insertReservationTime(LocalTime.of(10, 0));
        Long canceledReservationId = insertReservation("레아", TARGET_DATE, timeId, themeId, 1L);
        deleteReservation(canceledReservationId);
        insertReservationHistory(canceledReservationId, "레아", TARGET_DATE, timeId, themeId, 1L);

        // when
        List<Reservation> reservations = reservationRepository.findByName("레아");

        // then
        assertThat(reservations)
                .extracting(Reservation::getId, Reservation::getStatus, Reservation::getWaitingRank)
                .containsExactly(tuple(canceledReservationId, ReservationStatus.CANCELED, null));
    }

    @Test
    @DisplayName("취소 이력은 활성 예약의 대기 순번 계산에 포함하지 않는다.")
    void findByDateAndThemeId_success_excludesCanceledHistoriesFromWaitingRank() {
        // given
        Long themeId = insertTheme("레벨2 탈출");
        Long timeId = insertReservationTime(LocalTime.of(10, 0));
        Long canceledReservationId = insertReservation("브라운", TARGET_DATE, timeId, themeId, 1L);
        deleteReservation(canceledReservationId);
        insertReservationHistory(canceledReservationId, "브라운", TARGET_DATE, timeId, themeId, 1L);
        Long reservedReservationId = insertReservation("레아", TARGET_DATE, timeId, themeId, 2L);
        Long waitingReservationId = insertReservation("포비", TARGET_DATE, timeId, themeId, 3L);

        // when
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(TARGET_DATE, themeId);

        // then
        assertThat(reservations)
                .extracting(Reservation::getId, Reservation::getStatus, Reservation::getWaitingRank)
                .containsExactly(
                        tuple(reservedReservationId, ReservationStatus.RESERVED, 0L),
                        tuple(waitingReservationId, ReservationStatus.WAITING, 1L)
                );
    }

    @Test
    @DisplayName("날짜와 테마에 해당하는 예약이 없으면 빈 목록을 반환한다.")
    void findByDateAndThemeId_success_returnsEmptyWhenReservationNotFound() {
        // given
        Long themeId = insertTheme("레벨2 탈출");

        // when
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(TARGET_DATE, themeId);

        // then
        assertThat(reservations).isEmpty();
    }

    private Long insertTheme(String name) {
        String sql = """
                INSERT INTO theme (name, description, thumbnail)
                VALUES (?, ?, ?)
                """;

        return insertAndReturnId(sql, preparedStatement -> {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, name + " 설명");
            preparedStatement.setString(3, "https://example.com/theme.png");
        });
    }

    private Long insertReservationTime(LocalTime startAt) {
        String sql = """
                INSERT INTO reservation_time (start_at)
                VALUES (?)
                """;

        return insertAndReturnId(sql, preparedStatement -> preparedStatement.setString(1, startAt.toString()));
    }

    private Long insertReservation(
            String name,
            LocalDate date,
            Long timeId,
            Long themeId,
            Long requestOrder
    ) {
        String sql = """
                INSERT INTO reservation (name, date, time_id, theme_id, request_order, created_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;

        return insertAndReturnId(sql, preparedStatement -> {
            preparedStatement.setString(1, name);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setLong(3, timeId);
            preparedStatement.setLong(4, themeId);
            preparedStatement.setLong(5, requestOrder);
        });
    }

    private void insertReservationHistory(
            Long reservationId,
            String name,
            LocalDate date,
            Long timeId,
            Long themeId,
            Long requestOrder
    ) {
        String sql = """
                INSERT INTO reservation_history (
                    reservation_id,
                    name,
                    date,
                    time_id,
                    theme_id,
                    request_order,
                    created_at,
                    canceled_at
                )
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;

        jdbcTemplate.update(sql, reservationId, name, Date.valueOf(date), timeId, themeId, requestOrder);
    }

    private void deleteReservation(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    private Long insertAndReturnId(String sql, PreparedStatementSetter statementSetter) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            statementSetter.setValues(preparedStatement);
            return preparedStatement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
