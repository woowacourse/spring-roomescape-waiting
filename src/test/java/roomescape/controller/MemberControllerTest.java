package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static roomescape.TestFixture.createMemberByName;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.controller.dto.request.RegisterMemberRequest;
import roomescape.controller.dto.response.MemberResponse;
import roomescape.controller.dto.response.RegisterUserResponse;

class MemberControllerTest extends AbstractRestDocsTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private DBHelper dbHelper;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("회원가입을 한다")
    void signup() {
        // given
        RegisterMemberRequest request = new RegisterMemberRequest(
                "test@test.com",
                "password123",
                "멍구"
        );

        // when & then
        RegisterUserResponse response = givenWithDocs("member-signup-post")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/members")
                .then().log().all()
                .statusCode(CREATED.value())
                .extract()
                .as(RegisterUserResponse.class);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.name()).isEqualTo("멍구")
        );
    }

    @Test
    @DisplayName("회원 목록을 조회한다")
    void getMembers() {
        // given
        dbHelper.insertMember(createMemberByName("멍구"));
        dbHelper.insertMember(createMemberByName("멍구2"));

        // when & then
        List<MemberResponse> responses = givenWithDocs("member-get")
                .when()
                .get("/members")
                .then().log().all()
                .statusCode(OK.value())
                .extract()
                .jsonPath().getList(".", MemberResponse.class);

        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses).extracting("name")
                        .containsExactlyInAnyOrder("멍구", "멍구2")
        );
    }
} 
