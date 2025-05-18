package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.SignUpRequestDto;
import roomescape.dto.member.MemberResponseDto;
import roomescape.dto.member.MemberSignupResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.UnauthorizationException;
import roomescape.repository.JpaMemberRepository;

@Service
@Transactional
public class MemberService {

    private final JpaMemberRepository memberRepository;

    public MemberService(JpaMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public Member findMemberById(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new UnauthorizationException("[ERROR] 유저를 찾을 수 없습니다. ID : " + id));
    }

    @Transactional(readOnly = true)
    public List<MemberResponseDto> findAllMembers() {
        return memberRepository.findAll().stream()
                .map(member -> new MemberResponseDto(member.getId(), member.getName(), member.getEmail(),
                        member.getRole()))
                .toList();
    }

    public MemberSignupResponseDto registerMember(SignUpRequestDto requestDto) {
        Member member = Member.createWithoutId(requestDto.name(), requestDto.email(), Role.USER, requestDto.password());

        if (memberRepository.existsByEmail(requestDto.email())) {
            throw new DuplicateContentException("이메일은 중복될 수 없습니다.");
        }

        Member save = memberRepository.save(member);
        MemberSignupResponseDto memberSignupResponseDto = new MemberSignupResponseDto(save.getId(), save.getName(),
                save.getEmail());
        return memberSignupResponseDto;
    }
}
