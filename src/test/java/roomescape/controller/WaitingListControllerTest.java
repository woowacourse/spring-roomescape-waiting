package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.ClearDbTest;
import roomescape.dto.response.ReservationTimeResult;
import roomescape.dto.response.ThemeResult;
import roomescape.dto.response.WaitingListResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ClearDbTest
class WaitingListControllerTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final String STRING_TOMORROW = TOMORROW.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    private long timeId;
    private long themeId;

    @BeforeEach
    void setUp() {
        Map<String, Object> timeParams = new HashMap<>();
        timeParams.put("startAt", "10:00");
        timeParams.put("endAt", "10:30");
        ReservationTimeResult time = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/times")
                .then().statusCode(201)
                .extract().jsonPath().getObject(".", ReservationTimeResult.class);
        timeId = time.id();

        Map<String, Object> themeParams = new HashMap<>();
        themeParams.put("name", "링");
        themeParams.put("description", "공포 테마");
        themeParams.put("thumbnailUrl", "http:~");
        ThemeResult theme = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/themes")
                .then().statusCode(201)
                .extract().jsonPath().getObject(".", ThemeResult.class);
        themeId = theme.id();
    }

    @Nested
    class 예약_대기_추가 {

        @Test
        void 성공() {
            Map<String, Object> reservationParams = new HashMap<>();
            reservationParams.put("name", "브라운");
            reservationParams.put("date", STRING_TOMORROW);
            reservationParams.put("timeId", timeId);
            reservationParams.put("themeId", themeId);
            reservationParams.put("amount", 50000);
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(reservationParams)
                    .when().post("/reservations")
                    .then().statusCode(201);

            Map<String, Object> waitingParams = new HashMap<>();
            waitingParams.put("name", "류시");
            waitingParams.put("date", STRING_TOMORROW);
            waitingParams.put("timeId", timeId);
            waitingParams.put("themeId", themeId);
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(waitingParams)
                    .when().post("/waiting-list")
                    .then().statusCode(201);

            Map<String, Object> params = new HashMap<>();
            params.put("name", "검프");
            params.put("date", STRING_TOMORROW);
            params.put("timeId", timeId);
            params.put("themeId", themeId);

            WaitingListResult response = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waiting-list")
                    .then().log().all()
                    .statusCode(201).extract()
                    .jsonPath().getObject(".", WaitingListResult.class);

            assertThat(response.waitingOrder()).isEqualTo(2);
            assertThat(response.name()).isEqualTo("검프");
            assertThat(response.date()).isEqualTo(TOMORROW);
            assertThat(response.timeId()).isEqualTo(timeId);
            assertThat(response.themeId()).isEqualTo(themeId);
        }

        @Test
        void 본인이_이미_예약한_슬롯에_대기_신청_시도시_422() {
            Map<String, Object> reservationParams = new HashMap<>();
            reservationParams.put("name", "검프");
            reservationParams.put("date", STRING_TOMORROW);
            reservationParams.put("timeId", timeId);
            reservationParams.put("themeId", themeId);
            reservationParams.put("amount", 50000);
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(reservationParams)
                    .when().post("/reservations")
                    .then().statusCode(201);

            Map<String, Object> params = new HashMap<>();
            params.put("name", "검프");
            params.put("date", STRING_TOMORROW);
            params.put("timeId", timeId);
            params.put("themeId", themeId);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waiting-list")
                    .then().log().all()
                    .statusCode(422);
        }

        @Test
        void 예약이_없는_슬롯에_대기_추가_시도시_422() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "검프");
            params.put("date", STRING_TOMORROW);
            params.put("timeId", timeId);
            params.put("themeId", themeId);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waiting-list")
                    .then().log().all()
                    .statusCode(422);
        }

        @Test
        void 이미_대기_중인_경우_422() {
            Map<String, Object> reservationParams = new HashMap<>();
            reservationParams.put("name", "브라운");
            reservationParams.put("date", STRING_TOMORROW);
            reservationParams.put("timeId", timeId);
            reservationParams.put("themeId", themeId);
            reservationParams.put("amount", 50000);
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(reservationParams)
                    .when().post("/reservations")
                    .then().statusCode(201);

            Map<String, Object> params = new HashMap<>();
            params.put("name", "검프");
            params.put("date", STRING_TOMORROW);
            params.put("timeId", timeId);
            params.put("themeId", themeId);

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waiting-list")
                    .then().statusCode(201);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/waiting-list")
                    .then().log().all()
                    .statusCode(422);
        }
    }

    @Nested
    class 예약_대기_삭제 {

        private long waitingId;

        @BeforeEach
        void setUp() {
            Map<String, Object> reservationParams = new HashMap<>();
            reservationParams.put("name", "브라운");
            reservationParams.put("date", STRING_TOMORROW);
            reservationParams.put("timeId", timeId);
            reservationParams.put("themeId", themeId);
            reservationParams.put("amount", 50000);
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(reservationParams)
                    .when().post("/reservations")
                    .then().statusCode(201);

            Map<String, Object> waitingParams = new HashMap<>();
            waitingParams.put("name", "검프");
            waitingParams.put("date", STRING_TOMORROW);
            waitingParams.put("timeId", timeId);
            waitingParams.put("themeId", themeId);
            waitingId = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(waitingParams)
                    .when().post("/waiting-list")
                    .then().statusCode(201)
                    .extract().jsonPath().getObject(".", WaitingListResult.class).id();
        }

        @Test
        void 성공() {
            Map<String, Object> deleteParams = new HashMap<>();
            deleteParams.put("name", "검프");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(deleteParams)
                    .when().delete("/waiting-list/" + waitingId)
                    .then().log().all()
                    .statusCode(204);

            List<WaitingListResult> remaining = RestAssured.given()
                    .when().get("/waiting-list?name=검프")
                    .then().statusCode(200)
                    .extract().jsonPath().getList(".", WaitingListResult.class);
            assertThat(remaining).isEmpty();
        }

        @Test
        void 타인_대기_삭제_시도시_403() {
            Map<String, Object> deleteParams = new HashMap<>();
            deleteParams.put("name", "류시");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(deleteParams)
                    .when().delete("/waiting-list/" + waitingId)
                    .then().log().all()
                    .statusCode(403);
        }
    }

    @Nested
    class 예약_대기_조회 {

        private long timeId2;
        private long themeId2;
        private long geumpWaitingId;
        private long ryusiWaiting1Id;
        private long ryusiWaiting2Id;

        @BeforeEach
        void setUp() {
            Map<String, Object> timeParams2 = new HashMap<>();
            timeParams2.put("startAt", "11:00");
            timeParams2.put("endAt", "11:30");
            ReservationTimeResult time2 = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(timeParams2)
                    .when().post("/times")
                    .then().statusCode(201)
                    .extract().jsonPath().getObject(".", ReservationTimeResult.class);
            timeId2 = time2.id();

            Map<String, Object> themeParams2 = new HashMap<>();
            themeParams2.put("name", "오즈의마법사");
            themeParams2.put("description", "판타지 테마");
            themeParams2.put("thumbnailUrl", "http:~");
            ThemeResult theme2 = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(themeParams2)
                    .when().post("/themes")
                    .then().statusCode(201)
                    .extract().jsonPath().getObject(".", ThemeResult.class);
            themeId2 = theme2.id();

            Map<String, Object> reservationParams = new HashMap<>();
            reservationParams.put("name", "브라운");
            reservationParams.put("date", STRING_TOMORROW);
            reservationParams.put("timeId", timeId);
            reservationParams.put("themeId", themeId);
            reservationParams.put("amount", 50000);
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(reservationParams)
                    .when().post("/reservations")
                    .then().statusCode(201);

            Map<String, Object> waiting1 = new HashMap<>();
            waiting1.put("name", "검프");
            waiting1.put("date", STRING_TOMORROW);
            waiting1.put("timeId", timeId);
            waiting1.put("themeId", themeId);
            geumpWaitingId = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(waiting1)
                    .when().post("/waiting-list")
                    .then().statusCode(201)
                    .extract().jsonPath().getObject(".", WaitingListResult.class).id();

            Map<String, Object> waiting2 = new HashMap<>();
            waiting2.put("name", "류시");
            waiting2.put("date", STRING_TOMORROW);
            waiting2.put("timeId", timeId);
            waiting2.put("themeId", themeId);
            ryusiWaiting1Id = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(waiting2)
                    .when().post("/waiting-list")
                    .then().statusCode(201)
                    .extract().jsonPath().getObject(".", WaitingListResult.class).id();

            Map<String, Object> reservationParams2 = new HashMap<>();
            reservationParams2.put("name", "브라운");
            reservationParams2.put("date", STRING_TOMORROW);
            reservationParams2.put("timeId", timeId2);
            reservationParams2.put("themeId", themeId2);
            reservationParams2.put("amount", 50000);
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(reservationParams2)
                    .when().post("/reservations")
                    .then().statusCode(201);

            Map<String, Object> waiting3 = new HashMap<>();
            waiting3.put("name", "류시");
            waiting3.put("date", STRING_TOMORROW);
            waiting3.put("timeId", timeId2);
            waiting3.put("themeId", themeId2);
            ryusiWaiting2Id = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(waiting3)
                    .when().post("/waiting-list")
                    .then().statusCode(201)
                    .extract().jsonPath().getObject(".", WaitingListResult.class).id();
        }

        @Test
        void 대기가_1건인_사용자는_1건이_조회() {
            List<WaitingListResult> responses = RestAssured.given().log().all()
                    .when().get("/waiting-list?name=검프")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", WaitingListResult.class);

            assertThat(responses).hasSize(1);

            WaitingListResult response = responses.getFirst();
            assertThat(response.id()).isEqualTo(geumpWaitingId);
            assertThat(response.waitingOrder()).isEqualTo(1);
            assertThat(response.name()).isEqualTo("검프");
            assertThat(response.date()).isEqualTo(TOMORROW);
            assertThat(response.timeId()).isEqualTo(timeId);
            assertThat(response.themeId()).isEqualTo(themeId);
        }

        @Test
        void 대기가_2건인_사용자는_2건이_조회() {
            List<WaitingListResult> responses = RestAssured.given().log().all()
                    .when().get("/waiting-list?name=류시")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", WaitingListResult.class);

            assertThat(responses).hasSize(2);

            WaitingListResult first = responses.get(0);
            assertThat(first.id()).isEqualTo(ryusiWaiting1Id);
            assertThat(first.waitingOrder()).isEqualTo(2);
            assertThat(first.timeId()).isEqualTo(timeId);
            assertThat(first.themeId()).isEqualTo(themeId);

            WaitingListResult second = responses.get(1);
            assertThat(second.id()).isEqualTo(ryusiWaiting2Id);
            assertThat(second.waitingOrder()).isEqualTo(1);
            assertThat(second.timeId()).isEqualTo(timeId2);
            assertThat(second.themeId()).isEqualTo(themeId2);
        }

        @Test
        void 없는_이름이면_빈_목록이_조회() {
            List<WaitingListResult> responses = RestAssured.given().log().all()
                    .when().get("/waiting-list?name=없는사람")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", WaitingListResult.class);

            assertThat(responses).isEmpty();
        }
    }
}
