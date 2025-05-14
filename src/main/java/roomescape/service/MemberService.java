package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.SignUpRequestDto;
import roomescape.dto.member.MemberResponseDto;
import roomescape.dto.member.MemberSignupResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.UnauthorizationException;
import roomescape.repository.MemberRepository;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member findMemberById(long id) {
        return memberRepository.findById(id).orElseThrow(() -> new UnauthorizationException("[ERROR] 유저를 찾을 수 없습니다. ID : " + id));
    }

    public List<MemberResponseDto> findAllMembers() {
        return memberRepository.findAll().stream()
                .map(member -> new MemberResponseDto(member.getId(), member.getName(), member.getEmail(), member.getRole()))
                .toList();
    }

    public MemberSignupResponseDto registerMember(SignUpRequestDto requestDto) {
        try {
            Member member = Member.createWithoutId(requestDto.name(), requestDto.email(), Role.USER, requestDto.password());
            Member save = memberRepository.save(member);
            MemberSignupResponseDto memberSignupResponseDto = new MemberSignupResponseDto(save.getId(), save.getName(), save.getEmail());
            return memberSignupResponseDto;
        } catch (IllegalStateException e) {
            throw new DuplicateContentException(e.getMessage());
        }
    }
}
