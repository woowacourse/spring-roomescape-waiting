package roomescape.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingListRepository;
import roomescape.service.dto.ReservationAvailableEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(value = "/clear.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WaitingListServiceIntegrationTest {

    private static final LocalDate RESERVATION_DATE = LocalDate.now().plusDays(1);
    private static final String RESERVATION_DATE_STRING = RESERVATION_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    private static final Long RESERVATION_TIME_ID = 1L;
    private static final Long THEME_ID = 1L;

    @Autowired
    WaitingListService waitingListService;

    @MockitoSpyBean
    WaitingListRepository waitingListRepository;

    @MockitoSpyBean
    ReservationRepository reservationRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpData() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "검프", RESERVATION_DATE_STRING, RESERVATION_TIME_ID, THEME_ID, LocalDateTime.now().minusDays(1));
        jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "류시", RESERVATION_DATE_STRING, RESERVATION_TIME_ID, THEME_ID, LocalDateTime.now().minusDays(1));
    }

    @Test
    void 예약_대기_승인이_정상적으로_처리된다() {
        // given
        ReservationAvailableEvent event = new ReservationAvailableEvent(RESERVATION_DATE, RESERVATION_TIME_ID, THEME_ID);

        // when
        waitingListService.promoteWaitingListToReservation(event);

        // then
        Integer countForReservation = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
        assertThat(countForReservation).isEqualTo(1);
        Integer countForWaitingList = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
        assertThat(countForWaitingList).isEqualTo(1);
    }

    @Test
    void 예약_대기_승인_도중_예약_추가가_실패하면_기존_예약_대기가_삭제되지_않는다() {
        // given
        ReservationAvailableEvent event = new ReservationAvailableEvent(RESERVATION_DATE, RESERVATION_TIME_ID, THEME_ID);

        String errorMessage = "예약 추가에 실패했습니다.";
        doThrow(new RuntimeException(errorMessage))
                .when(reservationRepository).save(any(Reservation.class));

        // when & then
        assertThatThrownBy(() -> waitingListService.promoteWaitingListToReservation(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(errorMessage);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void 예약_대기_승인_도중_기존_예약_대기_삭제에_실패하면_예약이_추가되지_않는다() {
        // given
        ReservationAvailableEvent event = new ReservationAvailableEvent(RESERVATION_DATE, RESERVATION_TIME_ID, THEME_ID);

        String errorMessage = "예약 대기 삭제에 실패했습니다.";
        doThrow(new RuntimeException(errorMessage))
                .when(waitingListRepository).deleteById(1L);

        // when & then
        assertThatThrownBy(() -> waitingListService.promoteWaitingListToReservation(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(errorMessage);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
        assertThat(count).isZero();
    }

    @Test
    void 예약_대기_승인_과정에서_오류가_발생하면_서버에_로그를_남긴다() {
        // given
        Logger logger = (Logger) LoggerFactory.getLogger("waiting.approval.failure");
        ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);

        ReservationAvailableEvent event = new ReservationAvailableEvent(RESERVATION_DATE, RESERVATION_TIME_ID, THEME_ID);

        String errorMessage = "DB 저장 실패";
        doThrow(new RuntimeException(errorMessage))
                .when(reservationRepository).save(any(Reservation.class));

        // when & then
        assertThatThrownBy(() -> waitingListService.promoteWaitingListToReservation(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(errorMessage)
                .satisfies(e -> {
                    List<ILoggingEvent> errorLogs = logAppender.list.stream()
                            .filter(iLoggingEvent -> iLoggingEvent.getLevel() == Level.ERROR)
                            .toList();
                    assertThat(errorLogs).hasSize(1);
                    assertThat(errorLogs.getFirst().getFormattedMessage())
                            .contains("예약 대기 승인에 실패했습니다.");
                });

        logger.detachAppender(logAppender);
    }
}
