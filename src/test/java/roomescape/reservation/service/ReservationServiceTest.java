package roomescape.reservation.service;

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
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.exception.business.PastTimeCancelException;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservationtime.service.ReservationTimeService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long pastReservationId;
    private Long futureReservationId1;
    private Long futureReservationId2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('18:00', '19:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마B', '설명B', 'https://b.com')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마C', '설명C', 'https://c.com')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마D', '설명D', 'https://d.com')");

        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', ?, 1, 1)",
                LocalDate.now().minusDays(1));
        pastReservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
        futureReservationId1 = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user2', '2099-12-01', 2, 1)");
        futureReservationId2 = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
    }

    @Test
    @DisplayName("예약 생성 성공")
    void 예약_생성_성공() {
        ReservationResponse response = reservationService.createReservation(
                new ReservationRequest("현미밥", LocalDate.now().plusDays(1), 1L, 1L));
        assertThat(response.id()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 예약 생성 시 예외 발생")
    void 존재하지_않는_timeId_예외() {
        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest("현미밥", LocalDate.now().plusDays(1), 999L, 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.TIME_NOT_FOUND))
                .hasMessage(ErrorCode.TIME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 themeId로 예약 생성 시 예외 발생")
    void 존재하지_않는_themeId_예외() {
        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest("현미밥", LocalDate.now().plusDays(1), 1L, 999L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.THEME_NOT_FOUND))
                .hasMessage(ErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약 삭제 후 해당 시간 예약 가능")
    void 예약_삭제_성공() {
        LocalDate date = LocalDate.of(2099, 12, 1);
        assertThat(reservationTimeService.getAvailableTimes(date, 1L)).hasSize(1);

        reservationService.deleteReservation(futureReservationId1);

        assertThat(reservationTimeService.getAvailableTimes(date, 1L)).hasSize(2);
    }

    @Test
    @DisplayName("이미 지난 예약은 취소할 수 없다")
    void 과거_예약_취소_불가() {
        assertThatThrownBy(() -> reservationService.deleteReservation(pastReservationId))
                .isInstanceOf(PastTimeCancelException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.PAST_RESERVATION_CANCEL))
                .hasMessage(ErrorCode.PAST_RESERVATION_CANCEL.getMessage());
    }

    @Test
    @DisplayName("예약 수정 성공")
    void 예약_수정_성공() {
        ReservationResponse response = reservationService.updateReservation(
                futureReservationId1, new ReservationUpdateRequest(LocalDate.of(2099, 12, 2), 2L));
        assertThat(response.date()).isEqualTo(LocalDate.of(2099, 12, 2));
    }

    @Test
    @DisplayName("이미 지난 예약은 수정할 수 없다")
    void 과거_예약_수정_불가() {
        assertThatThrownBy(() -> reservationService.updateReservation(
                pastReservationId, new ReservationUpdateRequest(LocalDate.of(2099, 12, 2), 2L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.PAST_RESERVATION_UPDATE))
                .hasMessage(ErrorCode.PAST_RESERVATION_UPDATE.getMessage());
    }

    @Test
    @DisplayName("변경하려는 날짜·시간이 과거면 수정 불가")
    void 새시간_과거면_수정_불가() {
        assertThatThrownBy(() -> reservationService.updateReservation(
                futureReservationId1, new ReservationUpdateRequest(LocalDate.now().minusDays(1), 2L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.PAST_TIME_RESERVATION))
                .hasMessage(ErrorCode.PAST_TIME_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("변경하려는 시간이 이미 예약된 경우 수정 불가")
    void 중복_예약_수정_불가() {
        assertThatThrownBy(() -> reservationService.updateReservation(
                futureReservationId2, new ReservationUpdateRequest(LocalDate.of(2099, 12, 1), 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.DUPLICATE_RESERVATION))
                .hasMessage(ErrorCode.DUPLICATE_RESERVATION.getMessage());
    }
}
