package roomescape.repository;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.Member;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location=classpath:/application.properties"})
class MemberRepositoryTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("이메일을 기준으로 유저를 조회한다.")
    @ParameterizedTest
    @CsvSource({"testDB@email.com,admin", "test2DB@email.com,user"})
    void findByEmail(String email, String name) {
        //given, when
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        //then
        assertThat(member.getName()).isEqualTo(name);
    }

    @DisplayName("이메일과 비밀번호를 기준으로 유저를 조회한다.")
    @ParameterizedTest
    @CsvSource({"testDB@email.com,1234,admin", "test2DB@email.com,1234,user"})
    void findByEmailAndPassword(String email, String password, String name) {
        //given, when
        Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        //then
        assertThat(member.getName()).isEqualTo(name);
    }
}
