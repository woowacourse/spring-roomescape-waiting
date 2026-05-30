package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.global.exception.ConflictException;
import roomescape.reservation.application.dto.WaitingCreateCommand;
import roomescape.reservation.application.service.WaitingCommandService;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;

@ServiceTest
public class WaitingConcurrencyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Autowired
    private WaitingCommandService waitingCommandService;

    @Autowired
    private TestDataHelper testHelper;
    
    @DisplayName("동시에 같은 사용자가 같은 슬롯에 대기 생성 시 하나는 성공하고 나머지는 예외 발생을 테스트합니다.")
    @Test
    void save_waiting_concurrent_duplicate_exception() throws InterruptedException {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation("카야", ReservationFixture.futureReservationDate(), themeId, timeId);
        WaitingCreateCommand command = ReservationFixture.futureStarkWaitingCreateCommand(themeId, timeId, NOW);

        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyThreadCounter = new CountDownLatch(numberOfThreads);
        CountDownLatch callingThreadBlocker = new CountDownLatch(1);
        CountDownLatch completedThreadCounter = new CountDownLatch(numberOfThreads);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger exceptionCount = new AtomicInteger();

        for (int i = 0; i < numberOfThreads; i++) {
            executor.execute(() -> {
                try {
                    readyThreadCounter.countDown();
                    callingThreadBlocker.await();
                    waitingCommandService.save(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    exceptions.add(e);
                    exceptionCount.incrementAndGet();
                } finally {
                    completedThreadCounter.countDown();
                }
            });
        }
        readyThreadCounter.await();
        callingThreadBlocker.countDown();
        completedThreadCounter.await();
        executor.shutdown();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(successCount.get()).isEqualTo(1);
            softly.assertThat(exceptionCount.get()).isEqualTo(numberOfThreads - 1);
            softly.assertThat(exceptions).hasOnlyElementsOfType(ConflictException.class);
        });
    }
}
