package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Sql("/member.sql")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationControllerTest {

    @Autowired
    ReservationTimeRepository timeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    WaitingReservationRepository waitingReservationRepository;

    @Autowired
    MemberRepository memberRepository;


    @Test
    void 유저_예약_생성_조회_삭제() {
        // given
        Map<String, String> params = new HashMap<>();
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(1);
        params.put("date", localDate.toString());
        timeRepository.save(ReservationTime.from(LocalTime.of(10, 0)));
        themeRepository.save(Theme.of("name", "desc", "thumb"));
        params.put("timeId", "1");
        params.put("themeId", "1");

        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        // when
        // then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 동시_요청으로_동일한_시간대에_예약을_추가한다() throws InterruptedException {

        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 반드시 DB에서 조회해서 사용!
        final Member member = memberRepository.findByEmailAndPassword("admin@naver.com",
                        Password.createForMember("1234"))
                .orElseThrow();

        ReservationTime time = timeRepository.save(ReservationTime.from(LocalTime.of(10, 0)));
        Theme theme1 = themeRepository.save(Theme.of("name", "desc", "thumb"));
        Theme theme2 = themeRepository.save(Theme.of("name2", "desc2", "thumb2"));
        Theme theme3 = themeRepository.save(Theme.of("name3", "desc3", "thumb3"));

        Map<String, String> params = new HashMap<>();
        LocalDate localDate = LocalDate.of(2999, 1, 1);
        params.put("date", localDate.toString());
        params.put("timeId", time.getId().toString());
        params.put("themeId", theme1.getId().toString());

        reservationRepository.save(Reservation.builder()
                .date(LocalDate.of(2999, 1, 1))
                .time(time)
                .theme(theme2)
                .member(member)
                .build()
        );
        reservationRepository.save(Reservation.builder()
                .date(LocalDate.of(2999, 1, 1))
                .time(time)
                .theme(theme3)
                .member(member)
                .build()
        );

        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        // when
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    RestAssured.given()
                            .contentType(ContentType.JSON)
                            .cookie("token", token)
                            .body(params)
                            .when()
                            .post("/reservations");
                } finally {
                    latch.countDown(); // 반드시 finally에서!
                }
            }).start();
        }

        latch.await(); // 모든 Thread가 끝날 때까지 대기

        // then
        Thread.sleep(1000);

        long booked = reservationRepository.findByMember(member)
                .stream()
                .filter(r -> r.getDate().equals(localDate))
                .count();
        long waiting = waitingReservationRepository.findWaitingReservationByMember(member)
                .stream()
                .filter(w -> w.date().equals(localDate))
                .count();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(booked).isEqualTo(3); // 기존 2건 + 예약 1건 = 3건
            softly.assertThat(waiting).isEqualTo(4); // 대기 4건
        });
    }
}
