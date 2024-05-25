package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.MEMBER2;

import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.response.MemberResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("모든 멤버를 조회한다.")
    @Test
    void findAll() {
        // given
        memberRepository.save(MEMBER1);
        memberRepository.save(MEMBER2);

        // when
        List<MemberResponse> members = memberService.findAll().responses();

        // then

        assertThat(members).hasSize(2);
        assertThat(members).extracting("name").containsExactly(MEMBER1.getName(), MEMBER2.getName());
    }
}
