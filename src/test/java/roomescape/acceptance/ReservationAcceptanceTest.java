package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.reservation.dto.ReservationRequest;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@Sql("/truncate-with-time-and-theme.sql")
class ReservationAcceptanceTest extends AcceptanceTest {
    private LocalDate date;
    private long timeId;
    private long themeId;
    private long reservationId;
    private String guest1Token;
    private String guest2Token;
    private String adminToken;
    private String liniToken;

    @BeforeEach
    void init() {
        date = LocalDate.now().plusDays(1);
        timeId = 1;
        themeId = 1;

        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin123", "admin@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");

        guest1Token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("guest123", "guest@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");

        guest2Token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("guest123", "guest2@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");
    }

    @DisplayName("예약 추가 성공 테스트")
    @Test
    void createReservation() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", guest1Token)
                .body(new ReservationRequest(date, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(201).body("id", is(greaterThan(0)));
    }

    @DisplayName("예약 추가 실패 테스트 - 중복 일정 오류")
    @TestFactory
    Stream<DynamicTest> createDuplicatedReservation() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations");
                }),
                DynamicTest.dynamicTest("같은 일정으로 예약 생성을 시도하면 400 응답을 반환한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(400)
                            .body("message", is("선택하신 테마와 일정은 이미 예약이 존재합니다."));
                })
        );
    }

    @DisplayName("예약 추가 실패 테스트 - 일정 오류")
    @Test
    void createInvalidScheduleReservation() {
        //given
        LocalDate invalidDate = LocalDate.now().minusDays(1);

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", guest1Token)
                .body(new ReservationRequest(invalidDate, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(400).body("message", is("현재보다 이전으로 일정을 설정할 수 없습니다."));
    }

    @DisplayName("모든 예약 내역 조회 테스트")
    @TestFactory
    Stream<DynamicTest> findAllReservations() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations");
                }),
                DynamicTest.dynamicTest("모든 예약 내역을 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().get("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(1));
                })
        );
    }

    @DisplayName("예약 취소 성공 테스트")
    @TestFactory
    Stream<DynamicTest> deleteReservationSuccess() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 생성하고, 식별자를 반환한다.", () -> {
                    reservationId = (int) RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().body().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().delete("/reservations/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("모든 예약 내역을 조회하면 남은 예약은 0개이다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().get("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(0));
                })
        );
    }

    @DisplayName("예약 취소 성공 테스트 - 예약 id 미존재")
    @TestFactory
    Stream<DynamicTest> deleteReservationSuccessWithUnknownId() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 삭제한다.", () -> {
                    int unknownId = 0;
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().delete("/reservations/" + unknownId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("모든 예약 내역을 조회하면 남은 예약은 0개이다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().get("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(0));
                })
        );
    }

    @DisplayName("예약 취소 실패 테스트 - 본인 예약 아님")
    @TestFactory
    Stream<DynamicTest> cannotDeleteReservationSuccess() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 생성하고, 식별자를 반환한다.", () -> {
                    reservationId = (int) RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().body().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("본인이 하지 않은 예약을 삭제하려고 하면 401 응답을 한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().delete("/reservations/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(403).body("message", is("예약을 삭제할 권한이 없습니다."));
                }),
                DynamicTest.dynamicTest("모든 예약 내역을 조회하면 남은 예약은 1개이다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().get("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(1));
                })
        );
    }

    @DisplayName("이미 일정이 지난 예약을 삭제할 수 없다.")
    @TestFactory
    @Sql(value={"/truncate.sql","/insert-past-reservation.sql"})
    Stream<DynamicTest> cannotDeletePastReservation() {
        return Stream.of(
                DynamicTest.dynamicTest("리니가 로그인을 한다", () -> {
                    liniToken = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("guest123", "guest2@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("리니가 일정이 지난 예약을 삭제하려고 하면 예외가 발생한다.", () -> {
                    long reservationId = 1;
                    RestAssured.given().log().all()
                            .cookie("token", liniToken)
                            .when().delete("/reservations/"+reservationId)
                            .then().log().all()
                            .assertThat().statusCode(400).body("message", is("이미 지난 예약(대기)은 삭제할 수 없습니다."));
                })
        );
    }

    @DisplayName("예약을 취소한 상태에서 예약 요청을 보내면 예약된다.")
    @TestFactory
    Stream<DynamicTest> deleteAndCreateReservation() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest1이 예약을 삭제한다.", () -> {
                    int unknownId = 0;
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().delete("/reservations/" + unknownId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("guest1이 다시 예약을 요청하면, 예약으로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                })
        );
    }

    @DisplayName("예약 대기자가 있던 상황에서 예약을 취소한 후, 다시 요청하면 예약 대기로 생성된다")
    @TestFactory
    Stream<DynamicTest> deleteAndCreateWaiting() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest2가 guest1과 동일한 테마와 일정으로 예약을 요청하고, 1번째 예약 대기 상태로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("1번째 예약 대기"));
                }),
                DynamicTest.dynamicTest("guest1이 예약을 삭제한다.", () -> {
                    int unknownId = 0;
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().delete("/reservations/" + unknownId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("guest2의 예약 대기가 예약으로 변경되었다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().get("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("[0].status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest1이 다시 예약을 요청하면, 예약 대기로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("1번째 예약 대기"));
                })
        );
    }

    @DisplayName("예약 요청 시, 동일한 테마와 일정으로 예약 존재 여부에 따라 예약/예약 대기 상태로 생성된다.")
    @TestFactory
    Stream<DynamicTest> createReservationByWaitingOrReserved() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest2가 guest1과 동일한 테마와 일정으로 예약을 요청하고, 1번째 예약 대기 상태로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("1번째 예약 대기"));
                })
        );
    }


    @DisplayName("이미 일정이 지난 예약 대기를 삭제할 수 없다.")
    @TestFactory
    @Sql(value={"/truncate.sql","/insert-past-waiting.sql"})
    Stream<DynamicTest> cannotDeletePastWaiting() {
        return Stream.of(
                DynamicTest.dynamicTest("리니가 로그인을 한다", () -> {
                    liniToken = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("lini123", "lini2@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("리니가 일정이 지난 예약 대기을 삭제하려고 하면 예외가 발생한다.", () -> {
                    long reservationId = 1;
                    RestAssured.given().log().all()
                            .cookie("token", liniToken)
                            .queryParam("waiting",true)
                            .when().delete("/reservations/"+reservationId)
                            .then().log().all()
                            .assertThat().statusCode(400).body("message", is("이미 지난 예약(대기)은 삭제할 수 없습니다."));
                })
        );
    }

    @DisplayName("이미 예약이 있는데, 또 예약/예약 대기 요청을 하면 예외를 발생시킨다.")
    @TestFactory
    Stream<DynamicTest> cannotCreateReservationBecauseAlreadyReserved() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 예약을 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest1이 동일한 테마와 일정으로 다시 예약을 요청하면 예외가 발생한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(400).body("message", is("이미 예약(대기) 상태입니다."));
                })
        );
    }

    @DisplayName("예약 대기에 대하여 예약 취소 요청을 하면 예외가 발생한다.")
    @TestFactory
    Stream<DynamicTest> cannotDeleteReservationBecauseStillWaiting() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 예약을 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest2가 동일한 테마와 일정으로 다시 예약을 요청하면 예약 대기로 생성된다.", () -> {
                    reservationId = (int )RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("1번째 예약"))
                            .extract().body().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("guest2가 예약 대기에 대하여 예약 취소를 요청하면 예외가 발생한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .queryParam("waiting",true)
                            .when().delete("/reservations/"+reservationId)
                            .then().log().all()
                            .assertThat().statusCode(400).body("message", is("예약 대기 상태입니다. 예약 대기 삭제로 요청해주세요."));
                })
        );
    }

    @DisplayName("예약이 취소되면 바로 다음 예약 대기가 예약으로 전환되며, 전환 후 예약 대기를 취소하려고 하면 예외가 발생한다.")
    @TestFactory
    Stream<DynamicTest> changeToReserved() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    reservationId = (int) RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().body().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("guest2가 guest1과 동일한 테마와 일정으로 예약을 요청하고, 1번째 예약 대기 상태로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("1번째 예약 대기"));
                }),
                DynamicTest.dynamicTest("guest1의 예약이 취소된다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().delete("/reservations/"+reservationId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("guest2의 예약 대기가 예약으로 변경되었다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().get("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("[0].status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest2가 예약 대기를 삭제하려고 하면, 이미 예약으로 전환되어서 예외가 발생한다", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .queryParam("waiting",true)
                            .when().delete("/reservations/"+reservationId)
                            .then().log().all()
                            .assertThat().statusCode(400).body("message", is("이미 예약으로 전환되었습니다."));
                }),
                DynamicTest.dynamicTest("guest2가 예약을 삭제한다", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().delete("/reservations/"+reservationId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                })
        );
    }

    @DisplayName("이미 예약 대기가 있는데, 또 예약/예약 대기 요청을 하면 예외를 발생시킨다.")
    @TestFactory
    Stream<DynamicTest> cannotCreateReservationBecauseAlreadyWaiting() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest2가 guest1과 동일한 테마와 일정으로 예약을 요청하고, 1번째 예약 대기 상태로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("1번째 예약 대기"));
                }),
                DynamicTest.dynamicTest("guest2가 동일한 테마와 일정으로 다시 예약을 요청하면 예외가 발생한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(400).body("message", is("이미 예약(대기) 상태입니다."));
                })
        );
    }

    @DisplayName("일정이 지난 예약 외 현재 이후 예약만 조회한다.")
    @TestFactory
    @Sql(value={"/truncate.sql","/insert-past-reservation.sql"})
    Stream<DynamicTest> doNotShowPastReservations() {
        return Stream.of(
                DynamicTest.dynamicTest("리니가 로그인을 한다", () -> {
                    liniToken = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("guest123", "guest2@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("리니가 본인의 예약 내역을 조회하면, 일정이 지난 예약만 있으므로 0개 내역이 조회된다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", liniToken)
                            .when().post("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("size()", is(0));
                }),
                DynamicTest.dynamicTest("리니가 새로운 예약을 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", liniToken)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201);
                }),
                DynamicTest.dynamicTest("리니가 본인의 예약 내역을 조회하면, 일정이 지나지 않은 1개 내역이 조회된다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().get("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(1));
                })
        );
    }

    @DisplayName("일정이 지난 예약 대기 외 현재 이후 예약 대기만 조회한다.")
    @TestFactory
    @Sql(value={"/truncate.sql", "/insert-past-reservation.sql"})
    Stream<DynamicTest> doNotShowWaitings() {
        return Stream.of(
                DynamicTest.dynamicTest("리니가 로그인을 한다", () -> {
                    liniToken = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("guest123", "guest2@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("리니가 본인의 예약 내역을 조회하면, 일정이 지난 예약 대기만 있으므로 0개 내역이 조회된다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", liniToken)
                            .when().post("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("size()", is(0));
                })
        );
    }
}
