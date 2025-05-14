package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.repository.MemberRepository;
import roomescape.entity.Member;
import roomescape.dto.request.MemberRequest;
import roomescape.exception.custom.DuplicatedException;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    public Member addMember(MemberRequest request) {
        validateDuplicateMember(request);

        return memberRepository.save(
            new Member(request.name(), request.email(), request.password()));
    }

    private void validateDuplicateMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicatedException("member");
        }
    }
}
