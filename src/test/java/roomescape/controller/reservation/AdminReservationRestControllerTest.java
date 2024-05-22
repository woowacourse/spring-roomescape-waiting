package roomescape.controller.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.controller.dto.AdminReservationRequest;
import roomescape.controller.dto.MemberReservationRequest;
import roomescape.domain.member.Member;
import roomescape.global.JwtManager;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.dto.WaitingReservationResponse;
import roomescape.service.dto.member.MemberCreateRequest;
import roomescape.service.dto.member.MemberResponse;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationTimeRequest;
import roomescape.service.dto.reservation.ReservationTimeResponse;
import roomescape.service.dto.theme.ThemeRequest;
import roomescape.service.dto.theme.ThemeResponse;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AdminReservationRestControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtManager jwtManager;

    private final Member member1 = new Member("t1@t1.com", "t1", "러너덕", "MEMBER");
    private final Member member2 = new Member("t2@t2.com", "t2", "영이", "MEMBER");
    private final Member admin = new Member("tt@tt.com", "tt", "재즈", "ADMIN");

    private String memberToken1;
    private String memberToken2;
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        initializeMemberData();
        memberToken1 = jwtManager.generateToken(member1);
        memberToken2 = jwtManager.generateToken(member2);
        adminToken = jwtManager.generateToken(admin);

        initializeTimesData();
        initializeThemeData();
    }

    private void initializeMemberData() {
        MemberCreateRequest memberRequest1 = new MemberCreateRequest("t1@t1.com", "t1", "러너덕");
        MemberCreateRequest memberRequest2 = new MemberCreateRequest("t2@t2.com", "t2", "영이");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberRequest1)
                .when().post("/members/signup")
                .then().log().all()
                .statusCode(201);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberRequest2)
                .when().post("/members/signup")
                .then().log().all()
                .statusCode(201);

        MemberCreateRequest adminRequest = new MemberCreateRequest("tt@tt.com", "tt", "재즈");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminRequest)
                .when().post("/members/signup")
                .then().log().all()
                .statusCode(201);
    }

    private void initializeThemeData() {
        ThemeRequest param = new ThemeRequest("공포", "공포는 무서워", "hi.jpg");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(param)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    private void initializeTimesData() {
        ReservationTimeRequest param = new ReservationTimeRequest("10:00");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(param)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약 목록을 조회하는데 성공하면 응답과 200 상태 코드를 반환한다.")
    @Test
    void return_200_when_find_all_reservations() {
        AdminReservationRequest reservationCreate = new AdminReservationRequest("tt@tt.com", 1L,
                "2100-08-05", 1L);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);

        List<ReservationResponse> actualResponse = RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/admin/reservations/confirmed")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ReservationResponse.class);

        ReservationResponse expectedResponse = new ReservationResponse(
                actualResponse.get(0).getId(), new MemberResponse("tt@tt.com", "재즈"),
                new ThemeResponse(1L, "공포", "공포는 무서워", "hi.jpg"),
                "2100-08-05",
                new ReservationTimeResponse(1L, "10:00"),
                "CONFIRMED"
        );

        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(List.of(expectedResponse));
    }

    @DisplayName("대기중인 예약 목록을 조회하는데 성공하면 응답과 200 상태코드를 반환한다.")
    @Test
    void return_200_when_find_all_waiting_reservations() {
        MemberReservationRequest reservationCreate = new MemberReservationRequest(1L,
                "2100-08-05", 1L);

        RestAssured.given().log().all()
                .cookie("token", memberToken1)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", memberToken2)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        List<WaitingReservationResponse> actual = RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", WaitingReservationResponse.class);

        List<WaitingReservationResponse> expected = List.of(
                new WaitingReservationResponse(2L, "영이", "공포", "2100-08-05", "10:00"),
                new WaitingReservationResponse(3L, "재즈", "공포", "2100-08-05", "10:00"));

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @DisplayName("어드민이 예약을 생성하는데 성공하면 응답과 201 상태 코드를 반환한다.")
    @Test
    void return_201_when_create_reservation_admin() {
        AdminReservationRequest reservationCreate = new AdminReservationRequest("tt@tt.com", 1L,
                "2100-08-05", 1L);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("어드민이 예약을 삭제하는데 성공하면 응답과 204 상태 코드를 반환한다.")
    @Test
    void return_204_when_delete_reservation() {
        MemberReservationRequest reservationCreate = new MemberReservationRequest(1L,
                "2100-08-05", 1L);

        Integer id = RestAssured.given().log().all()
                .cookie("token", memberToken1)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .body()
                .path("id");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/admin/reservations/" + id)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("어드민이 대기중인 예약을 삭제하는데 성공하면 응답과 204 상태코드를 반환한다.")
    @Test
    void return_204_when_delete_waiting_reservation() {
        MemberReservationRequest reservationCreate = new MemberReservationRequest(1L,
                "2100-08-05", 1L);

        RestAssured.given().log().all()
                .cookie("token", memberToken1)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer id = RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .body()
                .path("id");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/admin/reservations/waiting/" + id)
                .then().log().all()
                .statusCode(204);
    }
}
