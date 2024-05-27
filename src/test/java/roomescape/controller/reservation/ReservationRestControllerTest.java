package roomescape.controller.reservation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.controller.dto.MemberReservationRequest;
import roomescape.domain.member.Member;
import roomescape.global.JwtManager;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.service.dto.member.MemberCreateRequest;
import roomescape.service.dto.theme.ThemeRequest;
import roomescape.service.dto.time.ReservationTimeRequest;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationRestControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtManager jwtManager;

    private final Member member = new Member("t1@t1.com", "t1", "러너덕", "MEMBER");
    private final Member admin = new Member("tt@tt.com", "tt", "재즈", "ADMIN");

    private String memberToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        initializeMemberData();
        memberToken = jwtManager.generateToken(member);
        adminToken = jwtManager.generateToken(admin);

        initializeTimesData();
        initializeThemeData();
    }

    private void initializeMemberData() {
        MemberCreateRequest memberRequest = new MemberCreateRequest("t1@t1.com", "t1", "러너덕");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberRequest)
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

    @DisplayName("멤버가 예약을 생성하는데 성공하면 응답과 201 상태 코드를 반환한다.")
    @Test
    void return_201_when_create_reservation_member() {
        MemberReservationRequest reservationCreate = new MemberReservationRequest(1L,
                "2100-08-05", 1L);

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("멤버가 예약을 조회하면 응답과 200 상태 코드를 반환한다.")
    void return_200_when_find_reservations_member() {
        MemberReservationRequest reservationCreate = new MemberReservationRequest(1L,
                "2100-08-05", 1L);

        Integer id = RestAssured.given().log().all()
                .cookie("token", memberToken)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .body()
                .path("id");

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .contentType(ContentType.JSON)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

    @DisplayName("멤버가 예약 대기를 생성하는데 성공하면 응답과 201 상태 코드를 반환한다.")
    @Test
    void return_201_when_create_reservation_waiting_member() {
        MemberReservationRequest reservationCreate = new MemberReservationRequest(1L,
                "2100-08-05", 1L);

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("멤버가 자신의 대기중인 예약을 삭제하는데 성공하면 응답과 204 상태코드를 반환한다.")
    @Test
    void return_204_when_delete_waiting_reservation() {
        MemberReservationRequest reservationCreate = new MemberReservationRequest(1L,
                "2100-08-05", 1L);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Integer id = RestAssured.given().log().all()
                .cookie("token", memberToken)
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
