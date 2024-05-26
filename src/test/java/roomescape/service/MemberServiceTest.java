package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.dto.LoginMember;
import roomescape.dto.TokenInfo;
import roomescape.dto.request.TokenRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.dto.response.TokenResponse;
import roomescape.fixture.MemberFixtures;
import roomescape.infrastructure.TokenGenerator;
import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "classpath:test-db-clean.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class MemberServiceTest {

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;
    @Autowired
    private ReservationService reservationService;

    @DisplayName("토큰 요청정보로 토큰을 발급한다.")
    @Test
    void createToken() {
        String email = "test@email.com";
        String password = "1234";
        Member daon = MemberFixtures.createAdminMemberDaon(email, password);
        memberRepository.save(daon);
        TokenRequest tokenRequest = new TokenRequest(email, password);

        TokenResponse result = memberService.createToken(tokenRequest);
        TokenInfo extract = tokenGenerator.extract(result.accessToken());

        assertAll(
                () -> assertThat(extract.payload()).isEqualTo(email),
                () -> assertThat(extract.memberRole()).isEqualTo(daon.getRole())
        );
    }

    @DisplayName("이메일과 비밀번호가 일치하지 않으면 예와가 발생한다.")
    @Test
    void createTokenInvalidEmailOrPassword() {
        String email = "test@email.com";
        String password = "1234";

        TokenRequest tokenRequest = new TokenRequest(email, password);

        assertThatThrownBy(() -> memberService.createToken(tokenRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

    @DisplayName("로그인 회원 정보가 유효하지 않으면 예외가 발생한다.")
    @Test
    void loginCheckInvalidInfo() {
        LoginMember loginMember = new LoginMember(1L);

        assertThatThrownBy(() -> memberService.loginCheck(loginMember))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원 입니다");
    }

    @DisplayName("토큰으로 로그인 회원을 조회한다.")
    @Test
    void findLoginMemberByToken() {
        String email = "test@email.com";
        String password = "1234";
        Member daon = MemberFixtures.createAdminMemberDaon(email, password);
        Member member = memberRepository.save(daon);
        TokenInfo tokenInfo = new TokenInfo(email, member.getRole());
        String token = tokenGenerator.createToken(tokenInfo);

        LoginMember result = memberService.findLoginMemberByToken(token);

        assertThat(result.id()).isEqualTo(member.getId());
    }

    @DisplayName("토큰 내 이메일 정보에 해당하는 회원이 없다면 에외가 발생한다.")
    @Test
    void findLoginMemberByTokenWithInvalidEmail() {
        String email = "test@email.com";
        String password = "1234";
        Member daon = MemberFixtures.createAdminMemberDaon(email, password);
        TokenInfo tokenInfo = new TokenInfo(email, daon.getRole());
        String token = tokenGenerator.createToken(tokenInfo);

        assertThatThrownBy(() -> memberService.findLoginMemberByToken(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원 입니다");
    }

    @DisplayName("토큰 정보가 관리자 역할이 있는지 검증한다.")
    @CsvSource(value = {"ADMIN,true", "USER,false"})
    @ParameterizedTest
    void hasAdminRole(MemberRole given, boolean expected) {
        String email = "test@email.com";
        String password = "1234";
        Member daon = MemberFixtures.createMemberDaon(email, password, given);
        Member member = memberRepository.save(daon);
        TokenInfo tokenInfo = new TokenInfo(email, member.getRole());
        String token = tokenGenerator.createToken(tokenInfo);

        boolean result = memberService.hasAdminRole(token);

        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("모든 회원 정보를 조회한다.")
    @Test
    void findAll() {
        Member daon = MemberFixtures.createAdminMemberDaon("abc@abc.com");
        Member daon1 = MemberFixtures.createAdminMemberDaon("abc1@abc.com");
        Member daon2 = MemberFixtures.createAdminMemberDaon("abc2@abc.com");
        memberRepository.save(daon);
        memberRepository.save(daon1);
        memberRepository.save(daon2);

        List<MemberResponse> result = memberService.findAll();

        assertThat(result).hasSize(3);
    }
}
