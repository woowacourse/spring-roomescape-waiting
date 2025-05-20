package roomescape.unit.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import roomescape.domain.member.Reserver;
import roomescape.dto.member.LoginRequestDto;
import roomescape.dto.member.SignupRequestDto;
import roomescape.infrastructure.auth.jwt.JwtTokenProvider;
import roomescape.service.member.MemberService;
import roomescape.unit.repository.member.FakeMemberRepository;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReserverServiceTest {

    private MemberService memberService;
    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET_KEY = "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET_KEY);
        memberService = new MemberService(new BCryptPasswordEncoder(), new FakeMemberRepository(), jwtTokenProvider);
    }

    @Test
    void 회원가입_테스트() {
        long id = memberService.signup(new SignupRequestDto("praisebak", "password", "투다"));
        Reserver reserverById = memberService.getMemberById(id);
        assertThat(id).isEqualTo(reserverById.getId());
    }

    @Test
    void 로그인시_유저가_존재하면_토큰을_생성한다() {
        memberService.signup(new SignupRequestDto("praisebak", "password", "투다"));

        String token = memberService.login(new LoginRequestDto("praisebak", "password"));

        assertThat(token).isNotNull();
    }

    @Test
    void id로_유저를_가져올_수_있다() {
        SignupRequestDto signupRequestDto = new SignupRequestDto("praisebak", "password", "투다");
        long id = memberService.signup(signupRequestDto);
        Reserver reserverById = memberService.getMemberById(id);

        assertThat(reserverById.getUsername().equals(signupRequestDto.email()));
    }

    @Test
    void 토큰으로_유저를_가져올_수_있다() {
        SignupRequestDto signupRequestDto = new SignupRequestDto("praisebak", "password", "투다");
        long id = memberService.signup(signupRequestDto);
        Reserver reserverById = memberService.getMemberById(id);

        String token = jwtTokenProvider.createToken(reserverById);

        Reserver reserverByToken = memberService.getMemberByToken(token);
        assertThat(reserverByToken.getUsername()).isEqualTo(reserverById.getUsername());
    }

    @Test
    void 회원가입에_중복_email을_가진_유저는_허용하지_않는다() {
        SignupRequestDto signupRequestDto = new SignupRequestDto("praisebak", "password", "투다");
        SignupRequestDto duplicateSignupRequestDto = new SignupRequestDto("praisebak", "password", "투다");
        memberService.signup(signupRequestDto);
        assertThatThrownBy(() -> memberService.signup(duplicateSignupRequestDto)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    void 모든_유저를_가져올_수_있다() {
        SignupRequestDto signupRequestDto = new SignupRequestDto("praisebak", "password", "투다");
        memberService.signup(signupRequestDto);
        SignupRequestDto secondSignupRequestDto = new SignupRequestDto("praisebak2", "password", "투다");
        memberService.signup(secondSignupRequestDto);

        assertThat(memberService.findAll()).hasSize(2);
    }
}
