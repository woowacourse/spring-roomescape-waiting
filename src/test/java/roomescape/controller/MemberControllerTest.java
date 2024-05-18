package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.ADMIN_NAME;

import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.response.MemberResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            memberRepository.deleteById(member.getId());
        }
    }

    @DisplayName("로그인시 토큰을 반환한다.")
    @Test
    void tokenLogin() {
        String accessToken = TestFixture.getAdminToken(memberRepository);

        MemberResponse member = RestAssured
                .given().log().all()
                .header("cookie", accessToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value()).extract().as(MemberResponse.class);

        assertThat(member.name()).isEqualTo(ADMIN_NAME);
    }
}
