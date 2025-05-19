package roomescape.application.member.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.member.command.dto.RegisterCommand;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.infrastructure.error.exception.MemberException;

@Service
@Transactional
public class CreateMemberService {

    private final MemberRepository memberRepository;

    public CreateMemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void register(RegisterCommand registerCommand) {
        if (memberRepository.existsByEmail(new Email(registerCommand.email()))) {
            throw new MemberException("이미 같은 이메일을 가진 사용자가 존재합니다.");
        }
        Member member = new Member(
                registerCommand.name(),
                new Email(registerCommand.email()),
                registerCommand.password()
        );
        memberRepository.save(member);
    }
}
