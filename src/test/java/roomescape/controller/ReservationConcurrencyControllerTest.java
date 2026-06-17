package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.domain.ReservationWaiting;

class ReservationConcurrencyControllerTest extends ControllerTestSupport {

    @Autowired
    private ReservationWaitingRepository waitingDao;

    @Test
    void 동시에_같은_슬롯에_대기_신청_시_순번이_보장된다() throws InterruptedException {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        String date = LocalDate.now().plusDays(1).toString();
        createReservation("예약자", date, timeId, themeId).statusCode(201);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int index = 0; index < threadCount; index++) {
            final String name = "대기자" + index;
            executorService.submit(() -> {
                try {
                    RestAssured.given()
                            .contentType(ContentType.JSON)
                            .body(Map.of("name", name, "reservationDate", LocalDate.now().plusDays(1).toString(),
                                    "timeId", timeId, "themeId", themeId))
                            .when().post("/waitings");
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        List<ReservationWaiting> waitings = waitingDao.findAll();
        assertThat(waitings).hasSize(10);

        List<Integer> orders = waitings.stream()
                .map(w -> waitingDao.countOrder(w.getSlot(), w.getId()))
                .toList();
        assertThat(orders).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }
}
