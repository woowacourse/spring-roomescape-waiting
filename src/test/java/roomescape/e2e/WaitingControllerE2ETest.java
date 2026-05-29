package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class WaitingControllerE2ETest extends BaseE2ETest {

    private String userSession;
    private Long userId;
    private Long otherUserId;
    private Long reserverId;
    private Long timeId;
    private Long themeId;
    private Long storeId;

    @BeforeEach
    void setUp() {
        storeId = seedStore("강남점");
        userId = seedMember("유저", "user@test.com", "USER");
        otherUserId = seedMember("타인", "other@test.com", "USER");
        reserverId = seedMember("예약자", "reserver@test.com", "USER");
        timeId = seedTime(LocalTime.of(13, 0));
        themeId = seedTheme("테마A");
        userSession = loginAs("user@test.com");
    }

    @Nested
    class Post {

        @Test
        @DisplayName("기존 예약이 있는 슬롯에 대기를 생성하면 201을 반환한다")
        void createsWaiting() {
            LocalDate date = LocalDate.now().plusDays(1);
            seedReservation(reserverId, timeId, themeId, storeId, date);

            HashMap<String, Object> body = new HashMap<>();
            body.put("memberId", userId);
            body.put("date", date.toString());
            body.put("timeId", timeId);
            body.put("themeId", themeId);
            body.put("storeId", storeId);

            RestAssured.given()
                    .sessionId(userSession)
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/waitings")
                    .then().statusCode(HttpStatus.CREATED.value())
                    .body("name", org.hamcrest.Matchers.equalTo("유저"))
                    .body("rank", org.hamcrest.Matchers.equalTo(1));
        }

        @Test
        @DisplayName("같은 슬롯에 두 번째로 대기하면 rank가 2로 발급된다")
        void secondWaitingHasRankTwo() {
            LocalDate date = LocalDate.now().plusDays(1);
            seedReservation(reserverId, timeId, themeId, storeId, date);
            seedWaiting(otherUserId, timeId, themeId, storeId, date);

            HashMap<String, Object> body = new HashMap<>();
            body.put("memberId", userId);
            body.put("date", date.toString());
            body.put("timeId", timeId);
            body.put("themeId", themeId);
            body.put("storeId", storeId);

            RestAssured.given()
                    .sessionId(userSession)
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/waitings")
                    .then().statusCode(HttpStatus.CREATED.value())
                    .body("rank", org.hamcrest.Matchers.equalTo(2));
        }

        @Test
        @DisplayName("예약이 없는 슬롯에 대기를 신청하면 400를 반환한다")
        void rejectsWhenNoReservation() {
            HashMap<String, Object> body = new HashMap<>();
            body.put("memberId", userId);
            body.put("date", LocalDate.now().plusDays(1).toString());
            body.put("timeId", timeId);
            body.put("themeId", themeId);
            body.put("storeId", storeId);

            RestAssured.given()
                    .sessionId(userSession)
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/waitings")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("자신의 예약 슬롯에 대기를 신청하면 400을 반환한다")
        void rejectsWhenOwnReservation() {
            LocalDate date = LocalDate.now().plusDays(1);
            seedReservation(userId, timeId, themeId, storeId, date);

            HashMap<String, Object> body = new HashMap<>();
            body.put("memberId", userId);
            body.put("date", date.toString());
            body.put("timeId", timeId);
            body.put("themeId", themeId);
            body.put("storeId", storeId);

            RestAssured.given()
                    .sessionId(userSession)
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/waitings")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class Get {

        @Test
        @DisplayName("내 대기 목록을 조회하면 200을 반환하고 rank가 포함된다")
        void findMyWaitings() {
            LocalDate date = LocalDate.now().plusDays(1);
            seedWaiting(userId, timeId, themeId, storeId, date);
            seedWaiting(otherUserId, timeId, themeId, storeId, date);

            RestAssured.given()
                    .sessionId(userSession)
                    .when().get("/waitings")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("size()", org.hamcrest.Matchers.is(1))
                    .body("[0].rank", org.hamcrest.Matchers.equalTo(1));
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("본인 대기를 삭제하면 204를 반환한다")
        void deletesOwnWaiting() {
            Long waitingId = seedWaiting(userId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(userSession)
                    .when().delete("/waitings/" + waitingId)
                    .then().statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        @DisplayName("타인 대기를 삭제하면 404를 반환한다")
        void deleteOthersWaiting() {
            Long otherWaitingId = seedWaiting(otherUserId, timeId, themeId, storeId, LocalDate.now().plusDays(1));

            RestAssured.given()
                    .sessionId(userSession)
                    .when().delete("/waitings/" + otherWaitingId)
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }
}
