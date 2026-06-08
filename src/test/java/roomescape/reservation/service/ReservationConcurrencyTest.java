package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.JdbcTimeRepository;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationConcurrencyTest {

    @Autowired private ReservationService reservationService;
    @Autowired private JdbcTimeRepository timeRepository;
    @Autowired private JdbcThemeRepository themeRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @DisplayName("같은 슬롯에 N명이 동시에 예약하면, RESERVED는 정확히 1개 저장되고 나머지는 WAITING 예약으로 저장된다.")
    @Test
    void 동시_생성_요청_시_RESERVED예약은_1개_생성() throws InterruptedException {
        // given
        Theme theme = insertTheme("테마");
        ReservationTime time = insertTime(
                LocalDateTime.of(2030, 6, 5, 10, 0),
                LocalDateTime.of(2030, 6, 5, 12, 0));

        int numThreads = 8;
        CountDownLatch doneSignal = new CountDownLatch(numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();

        for (int i = 0; i < numThreads; i++) {
            executorService.execute(() -> {
                try {
                    reservationService.create(
                            new ReservationSaveServiceRequest("same_user", theme.getId(), time.getId()));
                    successCount.getAndIncrement();
                } catch (DuplicateReservationException e) {
                    duplicateCount.getAndIncrement();
                } finally {
                    doneSignal.countDown();
                }
            });
        }
        doneSignal.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertAll(
                () -> assertThat(successCount.get()).isEqualTo(1),
                () -> assertThat(duplicateCount.get()).isEqualTo(numThreads - 1));
    }

    private Theme insertTheme(String name) {
        return themeRepository.save(
                new Theme(name, "설명", "imageUrl")
        );
    }

    private ReservationTime insertTime(LocalDateTime start, LocalDateTime end) {
        return timeRepository.save(start, end);
    }
}
