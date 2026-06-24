package roomescape.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.ReservationApplicationService;
import roomescape.domain.ReservationWaiting;
import roomescape.exception.ProblemType;
import roomescape.fixture.ReservationWaitingFixture;
import roomescape.service.ReservationWaitingService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 예약 취소 시 '승계(주인 교체) + 대기 삭제'를 한 트랜잭션으로 묶었을 때의 실패 처리를 검증한다.
 *
 * <p>(1) 대기 삭제가 실패하면 앞선 주인 교체까지 함께 롤백되는지(원자성),
 * (2) 락 대기·동시성 충돌 같은 일시적 DB 실패가 409(재시도 안내)로 응답되는지를 본다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:cancelfailtest")
class ReservationCancellationFailureTest {

    @Autowired
    private ReservationApplicationService applicationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private ReservationWaitingService reservationWaitingService;

    private Long reservationId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");

        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        Long timeId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time ORDER BY id DESC LIMIT 1", Long.class);
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "https://example.com/horror.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject(
                "SELECT id FROM theme ORDER BY id DESC LIMIT 1", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, reservation_status) VALUES (?, ?, ?, ?, 'CONFIRM')",
                "티뉴", LocalDate.of(2026, 8, 5), timeId, themeId
        );
        reservationId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation ORDER BY id DESC LIMIT 1", Long.class);
    }

    @Test
    void 예약_취소로_대기를_승계하는_중_대기_삭제가_실패하면_먼저_바뀐_예약_주인도_함께_롤백되어_원래_예약자가_유지된다() {
        ReservationWaiting promoted = ReservationWaitingFixture.builder().id(99L).name("민욱").build();
        given(reservationWaitingService.findEarliestByReservationId(reservationId))
                .willReturn(Optional.of(promoted));
        willThrow(new DataAccessResourceFailureException("대기 삭제 중 DB 접근 실패"))
                .given(reservationWaitingService).deleteById(99L);

        assertThatThrownBy(() -> applicationService.cancelMyReservation(reservationId, "티뉴"))
                .isInstanceOf(DataAccessException.class);

        String owner = jdbcTemplate.queryForObject(
                "SELECT name FROM reservation WHERE id = ?", String.class, reservationId);
        assertThat(owner).isEqualTo("티뉴");
    }

    @Test
    void 동시성_충돌로_취소가_실패하면_409와_재시도_안내를_반환한다() throws Exception {
        given(reservationWaitingService.findEarliestByReservationId(reservationId))
                .willThrow(new CannotAcquireLockException("lock timeout"));

        mockMvc.perform(delete("/reservations/me/{id}", reservationId).param("name", "티뉴"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(ProblemType.CONCURRENCY_CONFLICT.uri().toString()));
    }
}
