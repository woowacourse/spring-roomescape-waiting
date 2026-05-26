package roomescape.e2e;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ThemeControllerE2ETest extends BaseE2ETest {

    @BeforeEach
    void setUp() {
        seedTheme("테마1");
        seedTheme("테마2");
    }

    @Nested
    class Get {

        @Test
        @DisplayName("전체 테마를 조회하면 200을 반환한다")
        void findAll() {
            RestAssured.given()
                    .when().get("/themes")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("size()", org.hamcrest.Matchers.is(2));
        }

        @Test
        @DisplayName("ID로 테마를 조회하면 200을 반환한다")
        void findById() {
            RestAssured.given()
                    .when().get("/themes/1")
                    .then().statusCode(HttpStatus.OK.value())
                    .body("name", org.hamcrest.Matchers.equalTo("테마1"));
        }

        @Test
        @DisplayName("존재하지 않는 테마는 404를 반환한다")
        void findByIdNotFound() {
            RestAssured.given()
                    .when().get("/themes/999")
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    class AvailableTimes {

        @Test
        @DisplayName("특정 테마와 날짜의 가용 시간을 조회하면 200을 반환한다")
        void findAvailableTimes() {
            seedTime(LocalTime.of(10, 0));
            seedTime(LocalTime.of(12, 0));
            LocalDate target = LocalDate.now().plusDays(1);

            RestAssured.given()
                    .when().get("/themes/1/times?localDate=" + target)
                    .then().statusCode(HttpStatus.OK.value())
                    .body("size()", org.hamcrest.Matchers.is(2));
        }
    }

    @Nested
    class Populars {

        @Test
        @DisplayName("인기 테마 조회는 200을 반환한다")
        void findPopulars() {
            RestAssured.given()
                    .when().get("/themes/populars?limit=10&days=7")
                    .then().statusCode(HttpStatus.OK.value());
        }

        @Test
        @DisplayName("limit가 음수면 400을 반환한다")
        void invalidLimit() {
            RestAssured.given()
                    .when().get("/themes/populars?limit=-1&days=7")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }
}
