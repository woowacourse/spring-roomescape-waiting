package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignUpRequest;
import roomescape.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberResponse save(MemberSignUpRequest memberSignUpRequest) {
        Member member = memberSignUpRequest.toMember();
        validateUniqueEmail(member.getEmail());
        Member savedMember = memberRepository.save(member);

        return MemberResponse.toResponse(savedMember);
    }

    private void validateUniqueEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("중복된 이메일이 존재합니다.");
        }
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
            .map(MemberResponse::toResponse)
            .toList();
    }
}
