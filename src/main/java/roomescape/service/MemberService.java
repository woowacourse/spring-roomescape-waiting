package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional
    public Member addMember(MemberRequest request) {
        validateDuplicateMember(request);

        return memberRepository.save(
            Member.createUser(
                    request.name(),
                    request.email(),
                    request.password()
            )
        );
    }

    @Transactional(readOnly = true)
    private void validateDuplicateMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicatedException("member");
        }
    }
}
