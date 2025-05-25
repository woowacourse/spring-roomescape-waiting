package roomescape.member.application;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.application.dto.MemberRequest;
import roomescape.member.application.dto.MemberResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.domain.PasswordEncoder;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.exception.EmailAlreadyExistsException;

@Service
@AllArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberResponse create(MemberRequest request) {
        validateEmailDuplicated(request);

        Member member = new Member(
                new Name(request.name()),
                new Email(request.email()),
                new Password(request.password(), passwordEncoder)
        );
        return MemberResponse.from(memberRepository.save(member));
    }

    private void validateEmailDuplicated(MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }
}
