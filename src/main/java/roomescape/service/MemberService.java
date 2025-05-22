package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.SignUpRequest;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.member.MemberSignupResponse;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member findMemberById(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("[ERROR] 유저를 찾을 수 없습니다. ID : " + id));
    }

    public List<MemberResponse> findAllMembers() {
        return memberRepository.findAll().stream()
                .map(member -> new MemberResponse(member.getId(), member.getName(), member.getEmail(),
                        member.getRole()))
                .toList();
    }

    public MemberSignupResponse registerMember(SignUpRequest requestDto) {
        Member member = Member.createWithoutId(requestDto.name(), requestDto.email(), Role.USER, requestDto.password());

        if (memberRepository.existsByEmail(requestDto.email())) {
            throw new DuplicateContentException("이메일은 중복될 수 없습니다.");
        }

        Member save = memberRepository.save(member);
        return new MemberSignupResponse(save.getId(), save.getName(),
                save.getEmail());
    }
}
