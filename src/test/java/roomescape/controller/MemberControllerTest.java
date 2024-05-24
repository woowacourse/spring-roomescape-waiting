package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.response.MemberResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatList;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MemberControllerTest {

    private static final int INITIAL_MEMBER_COUNT = 2;

    @DisplayName("모든 사용자의 id와 이름을 반환한다.")
    @Test
    void should_return_all_members() {
        List<MemberResponse> allMembers = RestAssured.given().log().all()
                .when().get("/members")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MemberResponse.class);

        assertThat(allMembers).hasSize(INITIAL_MEMBER_COUNT);
        assertThatList(allMembers).map(MemberResponse::getId).containsExactly(1L, 2L);
        assertThatList(allMembers).map(MemberResponse::getName).containsExactly("에버", "관리자");
    }
}
