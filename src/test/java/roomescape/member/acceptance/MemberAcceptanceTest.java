package roomescape.member.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.helper.TestHelper;
import roomescape.member.dto.request.MemberRequest.MemberCreateRequest;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberAcceptanceTest {

    private static final String DEFAULT_EMAIL = "miso@email.com";
    private static final String DEFAULT_PASSWORD = "miso";
    private static final String DEFAULT_NAME = "미소";

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        Member member = new Member(0L, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD, RoleType.ADMIN);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("회원을 생성한다.")
    void createMember() {
        // given
        var request = new MemberCreateRequest("테스트", "test@email.com", "password");

        // when & then
        TestHelper.post("/members", request)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", is("테스트"))
                .body("email", is("test@email.com"))
                .body("role", is("USER"));
    }

    @Test
    @DisplayName("모든 회원을 조회한다.")
    void getAllMembers() {
        // given
        TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when & then
        TestHelper.get("/members")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].name", is(DEFAULT_NAME));
    }

    @Test
    @DisplayName("회원을 삭제한다.")
    void deleteMember() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when & then
        TestHelper.deleteWithToken("/members/" + 1, token)
                .then()
                .statusCode(HttpStatus.OK.value());

        TestHelper.get("/members")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("존재하지 않는 회원을 삭제하면 예외가 발생한다.")
    void deleteNonExistentMember() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when & then
        TestHelper.deleteWithToken("/members/999", token)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("존재하지 않는 id 입니다."));
    }
}
