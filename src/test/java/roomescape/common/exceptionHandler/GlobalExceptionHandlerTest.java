package roomescape.common.exceptionHandler;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.exceptionHandler.dto.ExceptionResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GlobalExceptionHandlerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void beforeEach() {
        RestAssured.port = this.port;
    }

    @Test
    @DisplayName("IllegalArgumentException 처리 테스트")
    void IllegalArgumentException_Handler_Test() {
        // given
        ExceptionResponse expected = new ExceptionResponse("[ERROR] IllegalArgumentException 예외 테스트",
                "/illegalArgumentException");
        // when
        Response response = RestAssured.given().log().all()
                .when().get("/illegalArgumentException")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("NullPointerException 처리 테스트")
    void NullPointerException_Handler_Test() {
        // given
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 서버의 오류입니다. 관리자에게 문의해주세요.",
                "/nullPointerException");
        // when
        Response response = RestAssured.given().log().all()
                .when().get("/nullPointerException")
                .then().log().all()
                .statusCode(500)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("HttpMessageNotReadableException 처리 테스트")
    void HttpMessageNotReadableException_Handler_Test() {
        // given
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 요청 입력이 잘못되었습니다.",
                "/httpMessageNotReadableException");
        // when
        Response response = RestAssured.given().log().all()
                .when().get("/httpMessageNotReadableException")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("IllegalArgumentException 감싼 HttpMessageNotReadableException 처리 테스트")
    void IllegalArgumentException_and_HttpMessageNotReadableException_Handler_Test() {
        // given
        ExceptionResponse expected = new ExceptionResponse("[ERROR] IllegalArgumentException 감싼 예외 테스트",
                "/httpMessageNotReadableException2");
        // when
        Response response = RestAssured.given().log().all()
                .when().get("/httpMessageNotReadableException2")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("예상치 못한 오류 처리 테스트")
    void Exception_Handler_Test() {
        // given
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 예상치 못한 서버 오류입니다. 서버에 문의해주세요.",
                "/unknown");
        // when
        Response response = RestAssured.given().log().all()
                .when().get("/unknown")
                .then().log().all()
                .statusCode(500)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @TestConfiguration
    static class TestControllerConfig {
        @RestController
        static class TestController {
            @GetMapping("/illegalArgumentException")
            public void illegalArgumentException() {
                throw new IllegalArgumentException("IllegalArgumentException 예외 테스트");
            }

            @GetMapping("/nullPointerException")
            public void nullPointerException() {
                throw new NullPointerException("NullPointerException 예외 테스트");
            }

            @GetMapping("/httpMessageNotReadableException")
            public void httpMessageNotReadableException() {
                throw new HttpMessageNotReadableException("HttpMessageNotReadableException 예외 테스트");
            }

            @GetMapping("/httpMessageNotReadableException2")
            public void httpMessageNotReadableException2() {
                try {
                    throw new IllegalArgumentException("IllegalArgumentException 감싼 예외 테스트");
                } catch (IllegalArgumentException e) {
                    throw new HttpMessageNotReadableException(e.getMessage(), e);
                }
            }

            @GetMapping("/unknown")
            public void unknownException() {
                throw new IllegalStateException("예상치 못한 오류");
            }
        }
    }
}
