package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.api.dto.ReservationRequest;
import roomescape.application.ReservationApplicationService;
import roomescape.application.ReservationModificationUseCase;
import roomescape.application.query.ReservationWaitingQueryService;
import roomescape.domain.Slot;
import roomescape.domain.exception.ConflictException;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:reservation-modification-lock")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationModificationUseCaseLockTest {

    private static final LocalTime RESERVATION_START_AT = LocalTime.of(10, 0);
    private static final LocalDate RESERVATION_DATE = LocalDate.of(2030, 1, 1);
    private static final LocalDateTime WAITING_CREATED_AT = LocalDateTime.of(2026, 8, 1, 10, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationApplicationService reservationApplicationService;

    @Autowired
    private ReservationModificationUseCase reservationModificationUseCase;

    @MockitoSpyBean
    private ReservationWaitingQueryService reservationWaitingQueryService;

    private Long timeId;
    private Long themeId;
    private Long reservationId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");

        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", RESERVATION_START_AT);
        timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "민욱",
                RESERVATION_DATE,
                timeId,
                themeId
        );
        reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)",
                "대기자",
                RESERVATION_DATE,
                timeId,
                themeId,
                Timestamp.valueOf(WAITING_CREATED_AT)
        );
    }

    @Test
    void 예약_삭제_후_대기_승격_전_같은_슬롯_예약_생성은_실패하지_않고_대기한다() throws Exception {
        CountDownLatch deletedBeforePromotion = new CountDownLatch(1);
        CountDownLatch resumePromotion = new CountDownLatch(1);

        doAnswer(invocation -> {
            deletedBeforePromotion.countDown();
            await(resumePromotion);
            return invocation.callRealMethod();
        }).when(reservationWaitingQueryService).findFirstBySlot(any(Slot.class));

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<?> deleteReservation = executorService.submit(
                    () -> reservationModificationUseCase.deleteReservation(reservationId)
            );
            assertThat(deletedBeforePromotion.await(1, TimeUnit.SECONDS)).isTrue();

            ReservationRequest newReserverRequest = new ReservationRequest(
                    "새예약자",
                    RESERVATION_DATE,
                    timeId,
                    themeId
            );
            Future<?> saveReservation = executorService.submit(
                    () -> reservationApplicationService.save(newReserverRequest)
            );

            try {
                assertThatThrownBy(() -> saveReservation.get(500, TimeUnit.MILLISECONDS))
                        .isInstanceOf(TimeoutException.class);
            } finally {
                resumePromotion.countDown();
            }

            deleteReservation.get(3, TimeUnit.SECONDS);
            assertThatThrownBy(() -> saveReservation.get(3, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(ConflictException.class);
            assertThat(findReservationNamesBySlot()).containsExactly("대기자");
        } finally {
            executorService.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) throws InterruptedException {
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }

    private List<String> findReservationNamesBySlot() {
        return jdbcTemplate.queryForList(
                "SELECT name FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                String.class,
                RESERVATION_DATE,
                timeId,
                themeId
        );
    }
}
