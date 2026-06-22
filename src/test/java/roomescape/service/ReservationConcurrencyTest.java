package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.ServiceTest;
import roomescape.dao.ReservationDao;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;

/*
학습용 - 동시성 테스트

* [재현하려는 Race Condition]
     * ReservationService.create()의 검사(SELECT)-저장(INSERT) 구간은 원자적이지 않다.

     * T1: existsByThemeAndDateAndTime() → false ─┐  (아직 아무도 저장 안 함)
     * T2: existsByThemeAndDateAndTime() → false ─┘  (T1이 커밋 전이므로 보이지 않음)

     * T1: reservationDao.save()         → 성공  ✅
     * T2: reservationDao.save()         → 💥 DuplicateKeyException (slot_id UNIQUE 위반)

 * [동기화 전략 - bothCheckedLatch]
     * T1: callRealMethod() → false → countDown(2→1) → await ─┐
     * T2: callRealMethod() → false → countDown(1→0) → await ─┘ latch 열림
     * -> 두 스레드 동시에 save() 진입 보장
 */

class ReservationConcurrencyTest extends ServiceTest {

    @MockitoSpyBean
    private ReservationDao reservationDao;

    @Autowired
    private ReservationService reservationService;

    @Test
    void 동시에_예약을_생성하면_존재_검증와_저장_사이에_RaceCondition이_발생한다() throws InterruptedException {
        // given
        ReservationTime time = fixtureGenerator.saveReservationTime(LocalTime.of(10, 0));
        Theme theme = fixtureGenerator.saveTheme("테마", "설명", "https://thumbnail");
        LocalDate date = LocalDate.of(2026, 7, 1);
        LocalDateTime currentDateTime = LocalDateTime.of(2026, 6, 1, 10, 0);
        fixtureGenerator.saveSlot(date, time, theme);

        int threadCount = 2;

        // 두 스레드가 모두 existsByThemeAndDateAndTime()의 결과(false)를 확인한 뒤
        // 동시에 save()를 호출하도록 보장하는 동기화 지점
        CountDownLatch bothCheckedLatch = new CountDownLatch(threadCount);

        doAnswer(invocation -> {
            boolean result = (boolean) invocation.callRealMethod(); // 실제 쿼리 실행
            bothCheckedLatch.countDown();                           // 작업 끝났다는 신호보내면서 스레드 대기
            bothCheckedLatch.await(5, TimeUnit.SECONDS);            // 상대방도 false 확인까지 대기(count가 0이 될 때까지 기다림)
            return result;                                          // 타임아웃 안나면 true, 이후 두 스레드 동시에 save() 진입
        }).when(reservationDao).existsByThemeAndDateAndTime(anyLong(), any(LocalDate.class), anyLong());

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);          // 동시 출발 신호
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 전체 완료 대기

        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> caughtExceptions = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            String name = "예약자" + i;
            executorService.submit(() -> { // 스레드 작업 시작
                try {
                    startLatch.await(); // 대기 상태
                    ReservationRequest request = new ReservationRequest(name, date, time.getId(), theme.getId(), 10000L);
                    reservationService.create(request, currentDateTime);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    caughtExceptions.add(e);
                } finally {
                    doneLatch.countDown(); // 작업 끝 (2 -> 1, 1 ->0)
                }
            });
        }

        startLatch.countDown(); // count가 1 -> 0이 되면서 모든 스레드 동시 출발
        doneLatch.await(10, TimeUnit.SECONDS); // 모든 스레드 종료까지 대기
        executorService.shutdown();

        // then
        assertAll(
                () -> assertThat(successCount.get() + caughtExceptions.size()).isEqualTo(threadCount),
                () -> assertThat(reservationService.getReservations()).hasSize(1), // DB에는 1건만 저장됨

                () -> assertThat(caughtExceptions).hasSize(1),
                () -> assertThat(caughtExceptions.getFirst())
                        .isInstanceOf(DuplicateKeyException.class)
        );
    }

    @Test
    void 동시에_여러_예약을_생성해도_최종적으로_예약은_1건만_저장된다() throws InterruptedException {
        // given
        ReservationTime time = fixtureGenerator.saveReservationTime(LocalTime.of(10, 0));
        Theme theme = fixtureGenerator.saveTheme("테마", "설명", "https://thumbnail");
        LocalDate date = LocalDate.of(2026, 7, 1);
        LocalDateTime currentDateTime = LocalDateTime.of(2026, 6, 1, 10, 0);

        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            String name = "예약자" + i;

            executorService.submit(() -> {
                try {
                    startLatch.await();
                    ReservationRequest request = new ReservationRequest(name, date, time.getId(), theme.getId(), 10000L);
                    reservationService.create(request, currentDateTime);
                } catch (Exception ignored) {
                    // 중복 예약으로 인한 예외는 무시
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then
        assertThat(reservationService.getReservations())
                .hasSize(1);
    }
}
