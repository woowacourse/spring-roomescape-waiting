package roomescape.integration.api.rest;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.RestAssuredTestBase;
import roomescape.integration.fixture.MemberDbFixture;

class MemberRestTest extends RestAssuredTestBase {

    @Autowired
    private MemberDbFixture memberDbFixture;

    @BeforeEach
    void setUp() {
        memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        memberDbFixture.leehyeonsu4888_지메일_gustn111느낌표두개();
    }

    @Test
    void 멤버_목록을_조회한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/members")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("한스"))
                .body("[1].name", is("한스"));
    }
} 
