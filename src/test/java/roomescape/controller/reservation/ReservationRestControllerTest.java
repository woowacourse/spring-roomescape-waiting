package roomescape.controller.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
class ReservationRestControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtManager jwtManager;

    private final Member member = new Member(1L, "t1@t1.com", "123", "러너덕", "MEMBER");
    private final Member admin = new Member(2L, "t2@t2.com", "124", "재즈", "ADMIN");

    private String memberToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        memberToken = jwtManager.generateToken(member);
        adminToken = jwtManager.generateToken(admin);
        initializeMemberData();
        initializeTimesData();
        initializeThemeData();
    }

    private void initializeMemberData() {
        MemberCreateRequest createRequest = new MemberCreateRequest("t1@t1.com", "123", "재즈");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(createRequest)
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
        AdminReservationRequest reservationCreate = new AdminReservationRequest(1L, 1L,
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
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ReservationResponse.class);

        ReservationResponse expectedResponse = new ReservationResponse(
                1L, new MemberResponse(1L, "재즈"),
                new ThemeResponse(1L, "공포", "공포는 무서워", "hi.jpg"),
                "2100-08-05",
                new ReservationTimeResponse(1L, "10:00")
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(List.of(expectedResponse));
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

    @DisplayName("어드민이 예약을 생성하는데 성공하면 응답과 201 상태 코드를 반환한다.")
    @Test
    void return_201_when_create_reservation_admin() {
        AdminReservationRequest reservationCreate = new AdminReservationRequest(1L, 1L,
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

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .contentType(ContentType.JSON)
                .body(reservationCreate)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);
    }
}
