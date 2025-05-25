package roomescape.member;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.exception.custom.reason.member.MemberNotFoundException;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;

import java.util.List;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void create(final MemberRequest request) {
        validateDuplication(request);

        final Member notSavedMember = new Member(request.email(), request.password(), request.name(), MemberRole.MEMBER);
        memberRepository.save(notSavedMember);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Member getByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Member getById(final Long id) {
        return memberRepository.findById(id)
                .orElseThrow(MemberNotFoundException::new);
    }

    private void validateDuplication(final MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberEmailConflictException();
        }
    }
}
