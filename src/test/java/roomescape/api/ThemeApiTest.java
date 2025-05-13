package roomescape.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.theme.dto.ThemeRequest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ThemeApiTest {

    private final JdbcTemplate jdbcTemplate;
    private final int port;

    public ThemeApiTest(
            @LocalServerPort final int port,
            @Autowired final JdbcTemplate jdbcTemplate
    ){
        this.port = port;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    @Nested
    @DisplayName("Post")
    class Post {

        @DisplayName("theme post 요청")
        @ParameterizedTest
        @MethodSource
        void post1(final Map<String, Object> body, final HttpStatus expectedStatusCode) {
            RestAssured.given().port(port).log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(expectedStatusCode.value());
        }

        static Stream<Arguments> post1(){
            return Stream.of(
                    Arguments.of(Map.of(
                            "name", "boogie",
                            "description", "부기입니다.",
                            "thumbnail", "http://www.google.com"
                    ), HttpStatus.CREATED),

                    Arguments.of(Map.of(
                            "description", "부기입니다.",
                            "thumbnail", "http://www.google.com"
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "name", "boogie",
                            "thumbnail", "http://www.google.com"
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "name", "boogie",
                            "description", "부기입니다."
                    ), HttpStatus.BAD_REQUEST),

                    Arguments.of(Map.of(
                            "name", "",
                            "description", "부기입니다.",
                            "thumbnail", "http://www.google.com"
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "name", "boogie",
                            "description", "",
                            "thumbnail", "http://www.google.com"
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "name", "boogie",
                            "description", "부기입니다.",
                            "thumbnail", ""
                    ), HttpStatus.BAD_REQUEST)
            );
        }

        @DisplayName("theme post 요청시 location header가 반환된다.")
        @Test
        void post2() {
            final ThemeRequest body = new ThemeRequest(
                    "boogie", "부기입니다.", "http://www.google.com"
            );

            RestAssured.given().port(port).log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(HttpStatus.CREATED.value())
                    .header("location", "/themes/1");
        }

    }

    @Nested
    @DisplayName("ReadAll")
    class ReadAll {

        @DisplayName("테마 전체 조회")
        @Test
        void read1() {
            RestAssured.given().log().all()
                    .port(port)
                    .when().get("/themes")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @DisplayName("theme 삭제")
        @Test
        void delete1() {
            jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES(?, ?, ?)", "", "", "");

            RestAssured.given().log().all()
                    .port(port)
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(204);
        }

        @DisplayName("존재하지 않는 theme를 삭제하면 404를 응답한다.")
        @Test
        void delete2() {
            RestAssured.given().log().all()
                    .port(port)
                    .when().delete("/themes/100")
                    .then().log().all()
                    .statusCode(404);
        }

    }

}
