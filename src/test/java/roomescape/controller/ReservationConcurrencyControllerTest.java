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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.DatabaseInitializer;
import roomescape.dao.ReservationWaitingDao;
import roomescape.domain.ReservationWaiting;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationConcurrencyControllerTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private ReservationWaitingDao waitingDao;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 동시에_같은_슬롯에_대기_신청_시_순번이_보장된다() throws InterruptedException {
        // given - 예약 먼저 생성
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        String date = LocalDate.now().plusDays(1).toString();
        createReservation("예약자", date, timeId, themeId);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when - 10명이 동시에 같은 슬롯에 대기 신청
        for (int index = 0; index < threadCount; index++) {
            final String name = "대기자" + index;
            executorService.submit(() -> {
                try {
                    createReservationWaiting(name, LocalDate.now().plusDays(1), timeId, themeId);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then - 10개 대기가 생성됐는지
        List<ReservationWaiting> waitings = waitingDao.select();
        assertThat(waitings).hasSize(10);

        // 순번이 1~10으로 중복 없이 부여됐는지
        List<Integer> orders = waitings.stream()
                .map(w -> waitingDao.countOrder(w.getSlot(), w.getId()))
                .toList();
        assertThat(orders).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    private int createTime(String startAt) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private int createTheme(String name, String description, String thumbnail) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "description", description, "thumbnail", thumbnail))
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private void createReservation(String name, String date, int timeId, int themeId) {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().statusCode(201);
    }

    private void createReservationWaiting(String name, LocalDate reservationDate, int timeId, int themeId) {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "reservationDate", reservationDate.toString(), "timeId", timeId, "themeId", themeId))
                .when().post("/waitings");
    }
}
