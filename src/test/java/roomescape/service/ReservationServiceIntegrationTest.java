package roomescape.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.RoomEscapeFixture;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.domain.reservation.RankedReservation;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.Status;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationServiceIntegrationTest {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        jdbcTemplate.update("insert into reservation_time(start_at) values ('10:00')");
        jdbcTemplate.update(
                "insert into theme(name, description, thumbnail_url) values ('공포', '무서워요', 'https://zeze.com')");
    }

    @Test
    void 동시에_10명이_첫_예약_요청시_1명만_승인상태가_된다() throws Exception {
        // 한 슬롯에 Approve된 예약은 반드시 1건 이하여야 한다.
        int threads = 10;
        var ready = new CountDownLatch(threads);
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(threads);
        var approved = new AtomicInteger();
        var waiting = new AtomicInteger();

        var pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            ReservationCreateRequest request = RoomEscapeFixture.reservationCreateRequestWithName(
                    new ReservationName(i + ""));
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    RankedReservation result = reservationService.reserve(request, LocalDateTime.now());

                    if (result.getReservation().getStatus() == Status.APPROVED) {
                        approved.incrementAndGet();
                    }
                    if (result.getReservation().getStatus() == Status.WAITING) {
                        waiting.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        done.await();

        assertThat(approved.get()).isEqualTo(1);
        assertThat(waiting.get()).isEqualTo(9);
    }
}

