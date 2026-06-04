package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.LoginRequest;
import roomescape.member.dto.SignupRequest;
import roomescape.member.repository.MemberRepository;

@SpringBootTest(properties = {
        "spring.sql.init.data-locations=",
        "spring.datasource.url=jdbc:h2:mem:service-test;DB_CLOSE_DELAY=-1"
})
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원가입하면 USER 권한으로 저장된다")
    void 회원가입_시_USER_권한으로_저장된다() {
        Member saved = memberService.signup(new SignupRequest("현미밥", "test@test.com", "1234"));

        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(saved.getName()).isEqualTo("현미밥");
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(memberRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("이메일과 비밀번호가 일치하면 로그인에 성공한다")
    void 로그인_성공() {
        memberService.signup(new SignupRequest("현미밥", "test@test.com", "1234"));

        Member member = memberService.login(new LoginRequest("test@test.com", "1234"));

        assertThat(member.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 예외가 발생한다")
    void 존재하지_않는_이메일_로그인_예외() {
        assertThatThrownBy(() -> memberService.login(new LoginRequest("none@test.com", "1234")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다")
    void 비밀번호_불일치_예외() {
        memberService.signup(new SignupRequest("현미밥", "test@test.com", "1234"));

        assertThatThrownBy(() -> memberService.login(new LoginRequest("test@test.com", "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("ID로 회원을 조회한다")
    void ID로_회원_조회() {
        Member saved = memberService.signup(new SignupRequest("현미밥", "test@test.com", "1234"));

        Member found = memberService.getById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("현미밥");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
    void 존재하지_않는_ID_조회_예외() {
        assertThatThrownBy(() -> memberService.getById(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }
}
