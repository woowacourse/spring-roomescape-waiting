package roomescape.store;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql("/cleanup.sql")
@SqlMergeMode(MergeMode.MERGE)
public class StoreControllerTest {

    private static final String INSERT_THREE_STORES_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점'),
                   (2, '홍대점'),
                   (3, '판교점');
            """;

    @Test
    @Sql(statements = INSERT_THREE_STORES_SQL)
    void 매장_목록을_조회한다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/stores")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3))
                .body("name", containsInAnyOrder("강남점", "홍대점", "판교점"));
    }

    @Test
    void 매장이_없으면_빈_목록을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/stores")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @Sql(statements = INSERT_THREE_STORES_SQL)
    void 매장_목록은_비로그인도_접근_가능하다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/stores")
                .then().log().all()
                .statusCode(200);
    }
}
