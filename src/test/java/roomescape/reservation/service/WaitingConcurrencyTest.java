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
import roomescape.reservation.domain.ReservationSlot;
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

    /**
     * READ COMMITTED 격리 수준에서는 커밋 전 INSERT가 다른 트랜잭션에 보이지 않기 때문에 동시에 시작한 스레드들이 모두 {@code MAX(rank) = 0}을 읽어 rank 중복을 재현할
     * 수 있습니다.
     */
    @DisplayName("동시에 서로 다른 사용자가 같은 슬롯에 대기 생성 시 중복 순번이 생성되는 문제를 재현합니다.")
    @Test
    void save_waiting_concurrent_generate_duplicate_rank() throws InterruptedException {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation("피케이", ReservationFixture.futureReservationDate(), themeId, timeId);
        WaitingCreateCommand starkCommand = ReservationFixture.futureStarkWaitingCreateCommand(themeId, timeId, NOW);
        WaitingCreateCommand pinoCommand = ReservationFixture.futurePinoWaitingCreateCommand(themeId, timeId, NOW);
        WaitingCreateCommand neoCommand = ReservationFixture.futureNeoWaitingCreateCommand(themeId, timeId, NOW);

        List<WaitingCreateCommand> commands = List.of(starkCommand, pinoCommand, neoCommand);

        int numberOfThreads = 3;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyThreadCounter = new CountDownLatch(numberOfThreads);
        CountDownLatch callingThreadBlocker = new CountDownLatch(1);
        CountDownLatch completedThreadCounter = new CountDownLatch(numberOfThreads);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            int index = i;
            executor.execute(() -> {
                try {
                    readyThreadCounter.countDown();
                    callingThreadBlocker.await();
                    waitingCommandService.save(commands.get(index));
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    completedThreadCounter.countDown();
                }
            });
        }
        readyThreadCounter.await();
        callingThreadBlocker.countDown();
        completedThreadCounter.await();
        executor.shutdown();

        ReservationSlot slot = ReservationFixture.futureReservationSlot(themeId, timeId, LocalTime.of(10, 0));
        Integer starkRank = testHelper.findWaitingRank(ReservationFixture.userNameStark().name(), slot);
        Integer pinoRank = testHelper.findWaitingRank(ReservationFixture.userNamePino().name(), slot);
        Integer neoRank = testHelper.findWaitingRank(ReservationFixture.userNameNeo().name(), slot);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exceptions).isEmpty();
            softly.assertThat(starkRank).isEqualTo(1);
            softly.assertThat(pinoRank).isEqualTo(1);
            softly.assertThat(neoRank).isEqualTo(1);
        });
    }
}
