package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignUpRequest;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberResponse save(MemberSignUpRequest memberSignUpRequest) {
        Member member = memberSignUpRequest.toMemberByUserRole();
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new IllegalArgumentException("중복된 이름 또는 이메일 입니다.");
        }
        Member savedMember = memberRepository.save(member);

        return MemberResponse.toResponse(savedMember);
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::toResponse)
                .toList();
    }
}
