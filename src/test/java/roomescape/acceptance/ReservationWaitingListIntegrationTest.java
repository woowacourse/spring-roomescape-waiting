package roomescape.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.ReservationService;
import roomescape.service.WaitingListService;
import roomescape.service.dto.ReservationAvailableEvent;
import roomescape.service.dto.command.ReservationModifyCommand;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(value = "/clear.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationWaitingListIntegrationTest {

    private static final String USER_NAME = "브라운";
    private static final LocalDate RESERVATION_DATE = LocalDate.now().plusDays(1);
    private static final String RESERVATION_DATE_STRING = RESERVATION_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    private static final Long RESERVATION_ID = 1L;
    private static final Long RESERVATION_TIME_ID = 1L;
    private static final Long THEME_ID = 1L;

    @Autowired
    ReservationService reservationService;

    @MockitoSpyBean
    WaitingListService waitingListService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpData() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", USER_NAME, RESERVATION_DATE_STRING, RESERVATION_TIME_ID, THEME_ID);
    }

    @Nested
    class ReservationCancellationTest {

        @Test
        void 예약_취소가_발생하고_대기자가_존재하는_경우_예약_대기_승인이_정상적으로_처리된다() {
            // given
            jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "검프", RESERVATION_DATE_STRING, RESERVATION_TIME_ID, THEME_ID, LocalDateTime.now().minusDays(1));
            jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "류시", RESERVATION_DATE_STRING, RESERVATION_TIME_ID, THEME_ID, LocalDateTime.now().minusDays(1));

            // when
            reservationService.delete(RESERVATION_ID);

            // then
            Integer countForReservation = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
            assertThat(countForReservation).isEqualTo(1);
            Integer countForWaitingList = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
            assertThat(countForWaitingList).isEqualTo(1);
        }

        @Test
        void 예약_취소가_발생하고_대기자가_존재하지_않는_경우_예약_취소만_정상적으로_처리된다() {
            // when
            reservationService.delete(RESERVATION_ID);

            // then
            Integer countForReservation = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
            assertThat(countForReservation).isZero();
        }

        @Test
        void 예약_대기_승인이_실패해도_예약_취소가_롤백되지_않는다() {
            // given
            jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "검프", RESERVATION_DATE_STRING, RESERVATION_TIME_ID, THEME_ID, LocalDateTime.now().minusDays(1));

            String errorMessage = "예약 대기 승인에 실패했습니다.";
            doThrow(new RuntimeException(errorMessage))
                    .when(waitingListService).promoteWaitingListToReservation(any(ReservationAvailableEvent.class));

            // when
            reservationService.delete(RESERVATION_ID);

            // then
            Integer countForReservation = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
            assertThat(countForReservation).isZero();

            Integer countForWaitingList = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
            assertThat(countForWaitingList).isEqualTo(1);
        }
    }

    @Nested
    class ReservationModificationTest {

        @Test
        void 예약_변경이_발생하고_대기자가_존재하는_경우_예약_대기_승인이_정상적으로_처리된다() {
            // given
            jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "검프", RESERVATION_DATE_STRING, RESERVATION_TIME_ID, THEME_ID, LocalDateTime.now().minusDays(1));
            jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "류시", RESERVATION_DATE_STRING, RESERVATION_TIME_ID, THEME_ID, LocalDateTime.now().minusDays(1));

            LocalDate modifyingDate = RESERVATION_DATE.plusDays(1);
            ReservationModifyCommand modifyCommand = new ReservationModifyCommand(RESERVATION_ID, USER_NAME, modifyingDate, RESERVATION_TIME_ID);

            // when
            reservationService.modify(modifyCommand);

            // then
            Integer countForReservation = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
            assertThat(countForReservation).isEqualTo(2);
            LocalDate modifiedDate = jdbcTemplate.queryForObject("SELECT date from reservation WHERE id = ?", LocalDate.class, RESERVATION_ID);
            assertThat(modifiedDate).isEqualTo(modifyingDate);
            Integer countForWaitingList = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
            assertThat(countForWaitingList).isEqualTo(1);
        }

        @Test
        void 예약_변경이_발생하고_대기자가_존재하지_않는_경우_예약_변경만_정상적으로_처리된다() {
            // given
            LocalDate modifyingDate = RESERVATION_DATE.plusDays(1);
            ReservationModifyCommand modifyCommand = new ReservationModifyCommand(RESERVATION_ID, USER_NAME, modifyingDate, RESERVATION_TIME_ID);

            // when
            reservationService.modify(modifyCommand);

            // then
            Integer countForReservation = jdbcTemplate.queryForObject("SELECT time_id from reservation", Integer.class);
            assertThat(countForReservation).isEqualTo(1);
            LocalDate modifiedDate = jdbcTemplate.queryForObject("SELECT date from reservation WHERE id = ?", LocalDate.class, RESERVATION_ID);
            assertThat(modifiedDate).isEqualTo(modifyingDate);
        }

        @Test
        void 예약_대기_승인이_실패해도_예약_변경이_롤백되지_않는다() {
            // given
            jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "검프", RESERVATION_DATE_STRING, RESERVATION_TIME_ID, THEME_ID, LocalDateTime.now().minusDays(1));

            LocalDate modifyingDate = RESERVATION_DATE.plusDays(1);
            ReservationModifyCommand modifyCommand = new ReservationModifyCommand(RESERVATION_ID, USER_NAME, modifyingDate, RESERVATION_TIME_ID);

            String errorMessage = "예약 대기 승인에 실패했습니다.";
            doThrow(new RuntimeException(errorMessage))
                    .when(waitingListService).promoteWaitingListToReservation(any(ReservationAvailableEvent.class));

            // when
            reservationService.modify(modifyCommand);

            // then
            Integer countForReservation = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
            assertThat(countForReservation).isEqualTo(1);

            LocalDate modifiedDate = jdbcTemplate.queryForObject("SELECT date from reservation WHERE id = ?", LocalDate.class, RESERVATION_ID);
            assertThat(modifiedDate).isEqualTo(modifyingDate);

            Integer countForWaitingList = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
            assertThat(countForWaitingList).isEqualTo(1);
        }
    }
}
