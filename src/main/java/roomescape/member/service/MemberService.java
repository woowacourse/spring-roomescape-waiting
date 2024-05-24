package roomescape.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import roomescape.auth.controller.dto.SignUpRequest;
import roomescape.auth.service.PasswordEncoder;
import roomescape.exception.custom.BadRequestException;
import roomescape.member.controller.dto.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    public MemberResponse create(SignUpRequest signUpRequest) {
        String encodedPassword = passwordEncoder.encode(signUpRequest.password());
        Member member = memberRepository.save(
                new Member(signUpRequest.name(), signUpRequest.email(), encodedPassword, Role.USER));
        return new MemberResponse(member.getId(), member.getName());
    }

    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException("해당 유저를 찾을 수 없습니다."));
    }
}
