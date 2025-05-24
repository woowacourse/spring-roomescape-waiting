package roomescape.member;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.PasswordEncoder;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void createMember(final MemberRequest request) {
        validateDuplicationEmail(request);

        final String encodedPassword = passwordEncoder.encode(request.password());

        final Member notSavedMember = new Member(request.email(), encodedPassword, request.name(), MemberRole.MEMBER);
        memberRepository.save(notSavedMember);
    }

    public List<MemberResponse> readAllMember() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    private void validateDuplicationEmail(final MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberEmailConflictException();
        }
    }
}
