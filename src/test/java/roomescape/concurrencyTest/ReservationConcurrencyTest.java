package roomescape.concurrencyTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.dto.ReservationRequest;
import roomescape.service.ReservationService;

@SpringBootTest
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 같은_슬롯에_동시에_예약하면_확정_예약은_하나만_생긴다() throws Exception {
        LocalDate date = LocalDate.now().plusDays(30);
        Long timeId = 2L;
        Long themeId = 1L;

        // getOrCreate 동시성 이슈와 분리하기 위해 슬롯은 미리 만들어둔다.
        reservationSlotRepository.save(date, timeId, themeId);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            int index = i;

            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    ReservationRequest request = new ReservationRequest(
                            "사용자" + index,
                            date,
                            timeId,
                            themeId
                    );

                    reservationService.save(LocalDateTime.now(), request);
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        executor.shutdown();

        assertThat(exceptions).isEmpty();

        Integer reservedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation r
                INNER JOIN reservation_slot rs
                    ON r.reservation_slot_id = rs.id
                WHERE rs.date = ?
                  AND rs.time_id = ?
                  AND rs.theme_id = ?
                  AND r.status = 'RESERVED'
                """, Integer.class, date, timeId, themeId);

        Integer waitingCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation r
                INNER JOIN reservation_slot rs
                    ON r.reservation_slot_id = rs.id
                WHERE rs.date = ?
                  AND rs.time_id = ?
                  AND rs.theme_id = ?
                  AND r.status = 'WAITING'
                """, Integer.class, date, timeId, themeId);

        assertThat(reservedCount).isEqualTo(1);
        assertThat(waitingCount).isEqualTo(threadCount - 1);
//        System.out.println("reserved인 행 수 : " + reservedCount);
//        System.out.println("waiting인 행 수 : " + waitingCount);
    }
}