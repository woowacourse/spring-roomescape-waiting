package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.api.dto.ReservationUpdateRequest;
import roomescape.application.ReservationCancellationUseCase;
import roomescape.application.command.ReservationCommandService;
import roomescape.application.command.ReservationWaitingCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ReservationWaitingQueryService;
import roomescape.config.FixedClockConfig;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ForbiddenException;
import roomescape.repository.ReservationJdbcRepository;
import roomescape.repository.ReservationTimeJdbcRepository;
import roomescape.repository.ReservationWaitingJdbcRepository;
import roomescape.repository.ReservationWaitingQueryJdbcRepository;

@JdbcTest
@Import({
        ReservationCancellationUseCase.class,
        ReservationCommandService.class,
        ReservationWaitingCommandService.class,
        ReservationQueryService.class,
        ReservationTimeQueryService.class,
        ReservationWaitingQueryService.class,
        ReservationJdbcRepository.class,
        ReservationTimeJdbcRepository.class,
        ReservationWaitingJdbcRepository.class,
        ReservationWaitingQueryJdbcRepository.class,
        FixedClockConfig.class
})
class ReservationCancellationUseCaseIntegrationTest {

    private static final LocalTime RESERVATION_START_AT = LocalTime.of(10, 0);
    private static final LocalDateTime FIRST_WAITING_CREATED_AT = LocalDateTime.of(2026, 8, 1, 10, 0);
    private static final LocalDateTime SECOND_WAITING_CREATED_AT = LocalDateTime.of(2026, 8, 1, 10, 5);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationCancellationUseCase reservationCancellationUseCase;

    @Autowired
    private Clock clock;

    private Long timeId;
    private Long themeId;
    private LocalDate future;
    private LocalDate anotherFuture;
    private LocalDate past;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now(clock);
        future = today.plusDays(1);
        anotherFuture = today.plusDays(2);
        past = today.minusDays(1);
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", RESERVATION_START_AT);
        timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);
    }

    @Test
    void delete는_대기가_있으면_예약을_삭제하고_첫_대기자를_예약으로_만든다() {
        Long reservationId = insertReservation("민욱", future);
        Long firstWaitingId = insertWaiting("브라운", future, FIRST_WAITING_CREATED_AT);
        Long secondWaitingId = insertWaiting("티뉴", future, SECOND_WAITING_CREATED_AT);

        reservationCancellationUseCase.delete(reservationId);

        assertThat(countReservationsById(reservationId)).isZero();
        assertThat(findReservationNamesBySlot(future)).containsExactly("브라운");
        assertThat(countWaitingsBySlot(future)).isEqualTo(1);
        assertThat(countWaitingsById(firstWaitingId)).isZero();
        assertThat(countWaitingsById(secondWaitingId)).isEqualTo(1);
    }

    @Test
    void deleteMine은_대기가_있어도_본인_예약이_아니면_예약자를_바꾸지_않는다() {
        Long reservationId = insertReservation("티뉴", future);
        insertWaiting("브라운", future, FIRST_WAITING_CREATED_AT);

        assertThatThrownBy(() -> reservationCancellationUseCase.deleteMine(reservationId, "민욱"))
                .isInstanceOf(ForbiddenException.class);

        assertThat(findReservationName(reservationId)).isEqualTo("티뉴");
        assertThat(countWaitingsBySlot(future)).isEqualTo(1);
    }

    @Test
    void deleteMine은_대기가_있어도_지난_예약이면_예약자를_바꾸지_않는다() {
        Long reservationId = insertReservation("민욱", past);
        insertWaiting("브라운", past, FIRST_WAITING_CREATED_AT);

        assertThatThrownBy(() -> reservationCancellationUseCase.deleteMine(reservationId, "민욱"))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(findReservationName(reservationId)).isEqualTo("민욱");
        assertThat(countWaitingsBySlot(past)).isEqualTo(1);
    }

    @Test
    void deleteMine은_대기가_없고_미래_예약이면_예약을_삭제한다() {
        Long reservationId = insertReservation("민욱", future);

        reservationCancellationUseCase.deleteMine(reservationId, "민욱");

        assertThat(countReservationsById(reservationId)).isZero();
    }

    @Test
    void deleteMine은_대기가_있고_미래_예약이면_예약을_삭제하고_첫_대기자를_예약으로_만든다() {
        Long reservationId = insertReservation("민욱", future);
        Long firstWaitingId = insertWaiting("브라운", future, FIRST_WAITING_CREATED_AT);
        Long secondWaitingId = insertWaiting("티뉴", future, SECOND_WAITING_CREATED_AT);

        reservationCancellationUseCase.deleteMine(reservationId, "민욱");

        assertThat(countReservationsById(reservationId)).isZero();
        assertThat(findReservationNamesBySlot(future)).containsExactly("브라운");
        assertThat(countWaitingsBySlot(future)).isEqualTo(1);
        assertThat(countWaitingsById(firstWaitingId)).isZero();
        assertThat(countWaitingsById(secondWaitingId)).isEqualTo(1);
    }

    @Test
    void updateMine은_기존_슬롯에_대기가_없으면_예약만_새_슬롯으로_옮긴다() {
        Long reservationId = insertReservation("민욱", future);
        ReservationUpdateRequest request = new ReservationUpdateRequest(anotherFuture, timeId);

        reservationCancellationUseCase.updateMine(reservationId, "민욱", request);

        assertThat(findReservationNamesBySlot(future)).isEmpty();
        assertThat(findReservationNamesBySlot(anotherFuture)).containsExactly("민욱");
    }

    @Test
    void updateMine은_기존_슬롯에_대기가_있으면_예약을_옮기고_첫_대기자를_기존_슬롯의_예약으로_만든다() {
        Long reservationId = insertReservation("민욱", future);
        Long firstWaitingId = insertWaiting("브라운", future, FIRST_WAITING_CREATED_AT);
        Long secondWaitingId = insertWaiting("티뉴", future, SECOND_WAITING_CREATED_AT);
        ReservationUpdateRequest request = new ReservationUpdateRequest(anotherFuture, timeId);

        reservationCancellationUseCase.updateMine(reservationId, "민욱", request);

        assertThat(findReservationNamesBySlot(future)).containsExactly("브라운");
        assertThat(findReservationNamesBySlot(anotherFuture)).containsExactly("민욱");
        assertThat(countWaitingsBySlot(future)).isEqualTo(1);
        assertThat(countWaitingsById(firstWaitingId)).isZero();
        assertThat(countWaitingsById(secondWaitingId)).isEqualTo(1);
    }

    private Long insertReservation(String name, LocalDate date) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name,
                date,
                timeId,
                themeId
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE name = ? AND date = ?",
                Long.class,
                name,
                date
        );
    }

    private Long insertWaiting(String name, LocalDate date, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)",
                name,
                date,
                timeId,
                themeId,
                Timestamp.valueOf(createdAt)
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_waiting WHERE name = ? AND date = ?",
                Long.class,
                name,
                date
        );
    }

    private String findReservationName(Long reservationId) {
        return jdbcTemplate.queryForObject(
                "SELECT name FROM reservation WHERE id = ?",
                String.class,
                reservationId
        );
    }

    private List<String> findReservationNamesBySlot(LocalDate date) {
        return jdbcTemplate.queryForList(
                "SELECT name FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ? ORDER BY id",
                String.class,
                date,
                timeId,
                themeId
        );
    }

    private int countReservationsById(Long reservationId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?",
                Integer.class,
                reservationId
        );
    }

    private int countWaitingsById(Long waitingId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_waiting WHERE id = ?",
                Integer.class,
                waitingId
        );
    }

    private int countWaitingsBySlot(LocalDate date) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_waiting WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class,
                date,
                timeId,
                themeId
        );
    }
}
