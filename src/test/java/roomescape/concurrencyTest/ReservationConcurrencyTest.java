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
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
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

    @Test
    void 같은_슬롯에서_확정_예약과_첫번째_대기를_동시에_취소해도_확정_예약은_하나만_남는다() throws Exception {
        LocalDate date = LocalDate.now().plusDays(31);
        Long timeId = 3L;
        Long themeId = 1L;
        LocalDateTime baseNow = LocalDateTime.now().minusMinutes(1);

        ReservationResponse reserved = reservationService.save(
                baseNow,
                new ReservationRequest("취소확정", date, timeId, themeId)
        );
        ReservationResponse firstWaiting = reservationService.save(
                baseNow.plusSeconds(1),
                new ReservationRequest("취소대기1", date, timeId, themeId)
        );
        reservationService.save(
                baseNow.plusSeconds(2),
                new ReservationRequest("취소대기2", date, timeId, themeId)
        );

        runConcurrently(List.of(
                () -> reservationService.delete(LocalDateTime.now(), reserved.reservationId(), "취소확정"),
                () -> reservationService.delete(LocalDateTime.now(), firstWaiting.reservationId(), "취소대기1")
        ));


        System.out.println("reserved인 행 수 : " + countReservations(date, timeId, themeId, "RESERVED"));
        System.out.println("waiting인 행 수 : " + countReservations(date, timeId, themeId, "WAITING"));

        assertThat(countReservations(date, timeId, themeId, "RESERVED")).isEqualTo(1);
        assertThat(countReservations(date, timeId, themeId, "WAITING")).isZero();
        assertThat(findStatusByName("취소대기2")).isEqualTo("RESERVED");
    }

    @Test
    void 같은_슬롯에서_확정_예약과_첫번째_대기를_동시에_수정해도_기존_슬롯에는_확정_예약이_하나만_남는다() throws Exception {
        LocalDate date = LocalDate.now().plusDays(32);
        Long currentTimeId = 4L;
        Long firstTargetTimeId = 5L;
        Long secondTargetTimeId = 6L;
        Long themeId = 1L;
        LocalDateTime baseNow = LocalDateTime.now().minusMinutes(1);

        ReservationResponse reserved = reservationService.save(
                baseNow,
                new ReservationRequest("수정확정", date, currentTimeId, themeId)
        );
        ReservationResponse firstWaiting = reservationService.save(
                baseNow.plusSeconds(1),
                new ReservationRequest("수정대기1", date, currentTimeId, themeId)
        );
        reservationService.save(
                baseNow.plusSeconds(2),
                new ReservationRequest("수정대기2", date, currentTimeId, themeId)
        );

        reservationSlotRepository.save(date, firstTargetTimeId, themeId);
        reservationSlotRepository.save(date, secondTargetTimeId, themeId);

        runConcurrently(List.of(
                () -> reservationService.update(
                        reserved.reservationId(),
                        LocalDateTime.now(),
                        new ReservationRequest("수정확정", date, firstTargetTimeId, themeId)
                ),
                () -> reservationService.update(
                        firstWaiting.reservationId(),
                        LocalDateTime.now(),
                        new ReservationRequest("수정대기1", date, secondTargetTimeId, themeId)
                )
        ));

        assertThat(countReservations(date, currentTimeId, themeId, "RESERVED")).isEqualTo(1);
        assertThat(countReservations(date, currentTimeId, themeId, "WAITING")).isZero();
        assertThat(findStatusByName("수정대기2")).isEqualTo("RESERVED");
    }

    private void runConcurrently(List<ConcurrentTask> tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch readyLatch = new CountDownLatch(tasks.size());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(tasks.size());
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (ConcurrentTask task : tasks) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    task.run();
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
        startLatch.countDown();
        assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();

        executor.shutdown();
        assertThat(exceptions).isEmpty();
    }

    private int countReservations(LocalDate date, Long timeId, Long themeId, String status) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation r
                INNER JOIN reservation_slot rs
                    ON r.reservation_slot_id = rs.id
                WHERE rs.date = ?
                  AND rs.time_id = ?
                  AND rs.theme_id = ?
                  AND r.status = ?
                """, Integer.class, date, timeId, themeId, status);
        return count;
    }

    private String findStatusByName(String name) {
        return jdbcTemplate.queryForObject("""
                SELECT status
                FROM reservation
                WHERE name = ?
                """, String.class, name);
    }

    @FunctionalInterface
    private interface ConcurrentTask {
        void run() throws Exception;
    }
}
