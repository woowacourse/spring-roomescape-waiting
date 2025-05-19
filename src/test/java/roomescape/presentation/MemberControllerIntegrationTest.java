package roomescape.presentation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.business.domain.Member;
import roomescape.persistence.repository.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("전체 회원 목록을 조회하면 회원 리스트를 응답한다")
    void readAll_ReturnsMemberList() {
        // given
        final Member expectedMember1 = new Member("이름1", "USER", "이메일1", "비밀번호1");
        final Member expectedMember2 = new Member("이름2", "USER", "이메일2", "비밀번호2");

        memberRepository.save(expectedMember1);
        memberRepository.save(expectedMember2);

        // when & then
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/members")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2))
                .body("name", hasItems("이름1", "이름2"));
    }

    @Test
    @DisplayName("회원이 존재하지 않을 때 전체 회원 목록을 조회하면 빈 리스트를 응답한다")
    void readAll_WhenNoMembers_ReturnsEmptyList() {
        // when
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/members")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(0));
    }
}
