package roomescape.application;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.MemberCreateDto;
import roomescape.application.dto.MemberDto;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.repository.MemberRepository;
import roomescape.exception.NotFoundException;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberDto registerMember(@Valid MemberCreateDto createDto) {
        Member memberWithoutId = Member.withoutId(
                createDto.name(),
                createDto.email(),
                createDto.password(),
                Role.USER
        );
        Member savedMember = memberRepository.save(memberWithoutId);
        return MemberDto.from(savedMember);
    }

    @Transactional(readOnly = true)
    public MemberDto getMemberDtoById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 사용자가 없습니다."));
        return MemberDto.from(member);
    }

    @Transactional(readOnly = true)
    public Member getMemberEntityById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 사용자가 없습니다."));
    }

    @Transactional(readOnly = true)
    public MemberDto getMemberDtoBy(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("이메일이 일치하는 사용자가 없습니다."));
        if (!member.matchPassword(password)) {
            throw new NotFoundException("비밀번호가 일치하지 않습니다.");
        }
        return MemberDto.from(member);
    }

    @Transactional(readOnly = true)
    public List<MemberDto> getAllMemberDtos() {
        List<Member> members = memberRepository.findAll();
        return MemberDto.from(members);
    }
}
