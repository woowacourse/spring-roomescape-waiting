package roomescape.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.dto.request.MemberRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.exception.DuplicatedEmailException;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse createMember(MemberRequest memberRequest) {
        Optional<Member> optionalMember = memberRepository.findByEmail(memberRequest.email());
        if (optionalMember.isPresent()) {
            throw new DuplicatedEmailException();
        }
        Member savedMember = memberRepository.save(memberRequest.toMember());
        return MemberResponse.from(savedMember);
    }
}
