package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.dto.business.MemberCreationContent;
import roomescape.dto.response.MemberProfileResponse;
import roomescape.exception.local.DuplicatedEmailException;
import roomescape.exception.local.NotFoundMemberException;
import roomescape.repository.MemberRepository;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member getMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
    }

    public List<MemberProfileResponse> findAllMemberProfile() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(MemberProfileResponse::new)
                .toList();
    }

    public MemberProfileResponse addMember(MemberCreationContent request) {
        validateDuplicatedEmail(request.email());
        Member member = Member.createWithoutId(request.role(), request.name(), request.email(), request.password());
        Member savedMember = memberRepository.save(member);
        return new MemberProfileResponse(savedMember);
    }

    private void validateDuplicatedEmail(String email) {
        boolean isDuplicatedEmail = memberRepository.existsByEmail(email);
        if (isDuplicatedEmail) {
            throw new DuplicatedEmailException();
        }
    }
}
