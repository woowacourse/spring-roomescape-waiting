package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.controller.dto.request.MemberRequest;
import roomescape.entity.Member;
import roomescape.exception.custom.DuplicatedException;
import roomescape.repository.JpaMemberRepository;

@Service
public class MemberService {

    private final JpaMemberRepository memberRepository;

    public MemberService(JpaMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    public Member addMember(MemberRequest request) {
        validateDuplicateMember(request);

        return memberRepository.save(
            Member.createUser(request.name(), request.email(), request.password()));
    }

    private void validateDuplicateMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicatedException("member");
        }
    }
}
