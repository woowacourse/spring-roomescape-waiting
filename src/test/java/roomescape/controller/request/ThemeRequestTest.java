package roomescape.controller.request;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ThemeRequestTest {

    @DisplayName("요청된 데이터의 이름이 null 혹은 비어있는 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_invalid_name(String name) {
        ThemeRequest request = new ThemeRequest(name, "d", "t");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 설명이 null 혹은 비어있는 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_invalid_description(String description) {
        ThemeRequest request = new ThemeRequest(description, "d", "t");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 썸네일이 null 혹은 비어있는 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_invalid_thumbnail(String thumbnail) {
        ThemeRequest request = new ThemeRequest(thumbnail, "d", "t");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터가 유효한 경우 예외가 발생하지 않는다.")
    @ParameterizedTest
    @CsvSource(value = {"nnn,ddd,ttt", "111,222,333", " n , d   , t"})
    void should_not_throw_exception_when_valid_data(String name, String description, String thumbnail) {
        ThemeRequest request = new ThemeRequest(name, description, thumbnail);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }
}
