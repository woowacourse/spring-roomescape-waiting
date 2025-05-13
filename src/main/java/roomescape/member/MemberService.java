package roomescape.member;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(
            final MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    public void createMember(final MemberRequest request) {
        validateDuplicationEmail(request);

        final Member notSavedMember = new Member(request.email(), request.password(), request.name(), MemberRole.MEMBER);
        memberRepository.saveMember(notSavedMember);
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
