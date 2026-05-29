package roomescape.reservationwaiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationWaitingServiceTest {

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long pastWaitingId;
    private Long futureReservationId1;
    private Long futureReservationId2;
    private ReservationWaitingResponse waitingResponse;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')");

        // 과거 예약 + 대기
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', ?, 1, 1)",
                LocalDate.now().minusDays(1));
        Long pastReservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        jdbcTemplate.update("INSERT INTO reservation_waiting (name, reservation_id) VALUES ('pastUser', ?)",
                pastReservationId);
        pastWaitingId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_waiting", Long.class);

        // 미래 예약 2개
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
        futureReservationId1 = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user2', '2099-12-01', 2, 1)");
        futureReservationId2 = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);

        // 현미밥의 대기 등록
        waitingResponse = reservationWaitingService.createWaiting(
                new ReservationWaitingRequest("현미밥", futureReservationId1));
    }

    @Test
    @DisplayName("예약 대기 생성 성공")
    void 예약_대기_생성_성공() {
        assertThat(waitingResponse.id()).isNotNull();
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기할 수 없다.")
    void 예약_대기_생성_실패() {
        assertThatThrownBy(() -> reservationWaitingService.createWaiting(
                new ReservationWaitingRequest("현미밥", futureReservationId1)))
                .isInstanceOf(BusinessException.class)
                .satisfies(
                        e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_WAITING))
                .hasMessage(ErrorCode.DUPLICATE_WAITING.getMessage());
    }

    @Test
    @DisplayName("예약 대기를 삭제할 수 있다.")
    void 예약_대기_삭제_성공() {
        reservationWaitingService.deleteWaiting(waitingResponse.id());
        assertThat(reservationWaitingService.getWaitingByName("현미밥")).isEmpty();
    }

    @Test
    @DisplayName("지난 예약 대기는 삭제할 수 없다.")
    void 예약_대기_삭제_실패() {
        assertThatThrownBy(() -> reservationWaitingService.deleteWaiting(pastWaitingId))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.PAST_WAITING_CANCEL))
                .hasMessage(ErrorCode.PAST_WAITING_CANCEL.getMessage());
    }

    @Test
    @DisplayName("사용자의 이름으로 대기 현황을 조회한다.")
    void 예약_대기_조회() {
        reservationWaitingService.createWaiting(new ReservationWaitingRequest("현미밥", futureReservationId2));
        assertThat(reservationWaitingService.getWaitingByName("현미밥")).hasSize(2);
    }
}
