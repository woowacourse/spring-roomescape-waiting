package roomescape.rest_assured_learning_test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Disabled
class RestAssuredTest {

    @Test
    @DisplayName("RestAssured Get 요청 기본")
    void get_default() {
        given()
                .baseUri("https://jsonplaceholder.typicode.com")
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }


    @Test
    @DisplayName("RestAssured Get 요청 심화1- queryParam")
    void get_deep1() {
        given()
                .baseUri("https://jsonplaceholder.typicode.com")
                .queryParam("userId", 1)
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .body("JSON_PATH", notNullValue());
    }

    @Test
    @DisplayName("RestAssured Get 요청 심화2- pathParam")
    void get_deep2() {
        given()
                .baseUri("https://jsonplaceholder.typicode.com")
                .pathParam("id", 1)
                .when()
                .get("/posts/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(1));
    }

    @Test
    @DisplayName("RestAssured Post 요청")
    void post() {
        String requestBody = """
        {
            "title": "hello",
            "body": "rest assured study",
            "userId": 1
        }
        """;

        given()
                .baseUri("https://jsonplaceholder.typicode.com")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("title", equalTo("hello"));
    }

    @Test
    @DisplayName("RestAssured Put 요청")
    void put() {
        String requestBody = """
            {
              "id": 1,
              "title": "updated title",
              "body": "updated body",
              "userId": 1
            }
            """;

        given()
                .baseUri("https://jsonplaceholder.typicode.com")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .put("/posts/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("updated title"))
                .body("body", equalTo("updated body"))
                .body("userId", equalTo(1));
    }

    @Test
    @DisplayName("RestAssured DELETE 요청")
    void delete() {
        given()
                .baseUri("https://jsonplaceholder.typicode.com")
                .when()
                .delete("/posts/1")
                .then()
                .statusCode(200);
    }
}
