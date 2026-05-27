package roomescape;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.support.ReservationTestHelper;

public class WaitingStepTest extends IntegrationTest {

    private static final LocalDate FUTURE_DATE = LocalDate.of(2050, 12, 31);

    @Autowired
    private ReservationTestHelper helper;

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        timeId = helper.insertTime(LocalTime.of(10, 0));
        themeId = helper.insertTheme("테마A", "설명", "url");
        // 슬롯에 예약을 미리 1건 넣어둠
        helper.insertReservation("브라운", FUTURE_DATE, timeId, themeId);
    }

    @Nested
    @DisplayName("대기 신청")
    class CreateWaiting {
        @Test
        @DisplayName("예약된 슬롯에 대기 신청하면 순번 1로 생성된다")
        void 첫_대기() {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "콘");
            body.put("date", FUTURE_DATE.toString());
            body.put("timeId", timeId);
            body.put("themeId", themeId);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON).body(body)
                    .when().post("/user/waitings")
                    .then().log().all()
                    .statusCode(201)
                    .body("name", is("콘"))
                    .body("orderIndex", is(1));
        }

        @Test
        @DisplayName("두 번째 대기는 순번 2를 받는다")
        void 두번째_대기() {
            helper.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);

            Map<String, Object> body = new HashMap<>();
            body.put("name", "모카");
            body.put("date", FUTURE_DATE.toString());
            body.put("timeId", timeId);
            body.put("themeId", themeId);

            RestAssured.given()
                    .contentType(ContentType.JSON).body(body)
                    .when().post("/user/waitings")
                    .then().statusCode(201)
                    .body("orderIndex", is(2));
        }

        @Test
        @DisplayName("예약이 없는 슬롯에 대기 신청 시 400")
        void 예약_없는_슬롯() {
            Long otherTimeId = helper.insertTime(LocalTime.of(11, 0));

            Map<String, Object> body = new HashMap<>();
            body.put("name", "콘");
            body.put("date", FUTURE_DATE.toString());
            body.put("timeId", otherTimeId);
            body.put("themeId", themeId);

            RestAssured.given()
                    .contentType(ContentType.JSON).body(body)
                    .when().post("/user/waitings")
                    .then().statusCode(400)
                    .body("message", is("예약 가능한 시간입니다. 대기가 아닌 예약을 신청해 주세요."));
        }

        @Test
        @DisplayName("같은 사용자가 같은 슬롯에 중복 대기 시 400")
        void 중복_대기() {
            helper.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);

            Map<String, Object> body = new HashMap<>();
            body.put("name", "콘");
            body.put("date", FUTURE_DATE.toString());
            body.put("timeId", timeId);
            body.put("themeId", themeId);

            RestAssured.given()
                    .contentType(ContentType.JSON).body(body)
                    .when().post("/user/waitings")
                    .then().statusCode(400)
                    .body("message", is("이미 해당 시간에 대기 신청한 내역이 있습니다."));
        }
    }

    @Nested
    @DisplayName("대기 취소")
    class CancelWaiting {

        @Test
        @DisplayName("본인 대기를 취소하면 204")
        void 본인_취소() {
            Long waitingId = helper.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);

            RestAssured.given()
                    .when().delete("/user/waitings/" + waitingId + "?name=콘")
                    .then().statusCode(204);
        }

        @Test
        @DisplayName("다른 사람의 대기를 취소하려 하면 404")
        void 타인_취소_거부() {
            Long waitingId = helper.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);

            RestAssured.given()
                    .when().delete("/user/waitings/" + waitingId + "?name=모카")
                    .then().statusCode(404);
        }

        @Test
        @DisplayName("중간 대기 취소 시 뒤 대기의 순번이 당겨진다")
        void 중간_취소_순번_재정렬() {
            Long w1 = helper.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);
            Long w2 = helper.insertWaiting("모카", FUTURE_DATE, timeId, themeId, 2);
            Long w3 = helper.insertWaiting("핀", FUTURE_DATE, timeId, themeId, 3);

            RestAssured.given()
                    .when().delete("/user/waitings/" + w2 + "?name=모카")
                    .then().statusCode(204);

            // 핀의 순번이 3 → 2
            assert helper.findWaitingOrder(w3) == 2 : "핀의 순번이 2가 되어야 함";
            // 콘은 1 그대로
            assert helper.findWaitingOrder(w1) == 1 : "콘의 순번은 1 유지";
        }
    }
}
