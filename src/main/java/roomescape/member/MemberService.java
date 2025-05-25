package roomescape.member;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;

import java.util.List;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void createMember(final MemberRequest request) {
        validateDuplicationEmail(request);

        final Member notSavedMember = new Member(request.email(), request.password(), request.name(), MemberRole.MEMBER);
        memberRepository.save(notSavedMember);
    }

    @Transactional(readOnly = true)
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
