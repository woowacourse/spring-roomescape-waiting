package roomescape.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class AdminViewControllerTest {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private RequestSpecification requestSpecification;

    @BeforeEach
    void setUp() {
        String deleteSql = "DELETE FROM member WHERE email = :email";
        namedParameterJdbcTemplate.update(deleteSql, Map.of("email", "admin@email.com"));

        String insertSql = "INSERT INTO member(name, email, password, role) VALUES (:name, :email, :password, :role)";
        namedParameterJdbcTemplate.update(
                insertSql,
                Map.of(
                        "name", "admin",
                        "email", "admin@email.com",
                        "password", passwordEncoder.encode("password123"),
                        "role", "admin"
                )
        );

        String cookie = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"admin@email.com\",\"password\":\"password123\"}")
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        requestSpecification = RestAssured.given()
                .cookie("token", cookie);
    }

    @DisplayName("어드민 메인 페이지를 출력한다")
    @Test
    void checkAdminDisplay_Main() {
        requestSpecification.log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("어드민 예약 내역 페이지를 출력한다")
    @Test
    void checkAdminDisplay_Reservation() {
        requestSpecification.log().all()
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("어드민 예약 시간 관리 페이지를 출력한다")
    @Test
    void checkAdminDisplay_ReservationTime() {
        requestSpecification.log().all()
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("어드민 예약 테마 관리 페이지를 출력한다")
    @Test
    void checkAdminDisplay_Theme() {
        requestSpecification.log().all()
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }
}
