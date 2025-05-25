package roomescape.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.SignUpRequestDto;
import roomescape.dto.member.MemberSignupResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.repository.JpaMemberRepository;

@Service
@Transactional
public class MemberCommandService {

    private final JpaMemberRepository memberRepository;

    public MemberCommandService(JpaMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberSignupResponseDto registerMember(SignUpRequestDto requestDto) {
        if (memberRepository.existsByEmail(requestDto.email())) {
            throw new DuplicateContentException("해당 이메일로 가입된 이력이 있습니다.");
        }

        Member member = Member.createWithoutId(requestDto.name(), requestDto.email(), Role.USER, requestDto.password());
        Member createdMember = memberRepository.save(member);
        return new MemberSignupResponseDto(createdMember.getId(), createdMember.getName(), createdMember.getEmail());
    }
}
