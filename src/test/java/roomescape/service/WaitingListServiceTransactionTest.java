package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationAvailableEvent;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingListRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(value = "/clear.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WaitingListServiceTransactionTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final String STRING_TOMORROW = TOMORROW.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

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
        jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "검프", STRING_TOMORROW, "1", "1", LocalDateTime.now().minusDays(1));
        jdbcTemplate.update("INSERT INTO waiting_list (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)", "류시", STRING_TOMORROW, "1", "1", LocalDateTime.now().minusDays(1));
    }

    @Test
    void 예약_대기_승인이_정상적으로_처리된다() {
        // given
        ReservationAvailableEvent event = new ReservationAvailableEvent(TOMORROW, 1L, 1L);

        // when
        waitingListService.handleReservationCanceled(event);

        // then
        Integer countForReservation = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
        assertThat(countForReservation).isEqualTo(1);
        Integer countForWaitingList = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
        assertThat(countForWaitingList).isEqualTo(1);
    }

    @Test
    void 예약_대기_승인_도중_예약_추가가_실패하면_기존_예약_대기가_삭제되지_않는다() {
        // given
        ReservationAvailableEvent event = new ReservationAvailableEvent(TOMORROW, 1L, 1L);

        String errorMessage = "예약 추가에 실패했습니다.";
        doThrow(new RuntimeException(errorMessage))
                .when(reservationRepository).save(any(Reservation.class));

        // when & then
        assertThatThrownBy(() -> waitingListService.handleReservationCanceled(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(errorMessage);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from waiting_list", Integer.class);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void 예약_대기_승인_도중_기존_예약_대기_삭제에_실패하면_예약이_추가되지_않는다() {
        // given
        ReservationAvailableEvent event = new ReservationAvailableEvent(TOMORROW, 1L, 1L);

        String errorMessage = "예약 대기 삭제에 실패했습니다.";
        doThrow(new RuntimeException(errorMessage))
                .when(waitingListRepository).deleteById(1L);

        // when & then
        assertThatThrownBy(() -> waitingListService.handleReservationCanceled(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(errorMessage);

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
        assertThat(count).isZero();
    }
}
