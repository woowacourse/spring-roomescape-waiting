package roomescape.application.member.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.member.command.dto.RegisterCommand;
import roomescape.domain.member.Email;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.infrastructure.error.exception.MemberException;

class CreateMemberServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    private CreateMemberService createMemberService;

    @BeforeEach
    void setUp() {
        createMemberService = new CreateMemberService(memberRepository);
    }

    @Test
    void 회원가입을_할_수_있다() {
        // given
        RegisterCommand registerCommand = new RegisterCommand("bello@email.com", "password", "벨로");

        // when
        createMemberService.register(registerCommand);

        // then
        assertThat(memberRepository.findByEmail(new Email(registerCommand.email())))
                .isPresent()
                .get()
                .extracting("name")
                .isEqualTo(registerCommand.name());
    }

    @Test
    void 중복된_이메일로_회원가입을_할_수_없다() {
        // given
        RegisterCommand registerCommand = new RegisterCommand("bello@email.com", "password", "벨로");
        createMemberService.register(registerCommand);

        // when, then
        assertThatCode(() -> createMemberService.register(registerCommand))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining("이미 같은 이메일을 가진 사용자가 존재합니다.");
    }
}
