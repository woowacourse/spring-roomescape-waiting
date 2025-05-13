package roomescape.application;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.dto.MemberCreateDto;
import roomescape.application.dto.MemberDto;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.exception.NotFoundException;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberDto registerMember(@Valid MemberCreateDto createDto) {
        Member memberWithoutId = Member.withoutId(createDto.name(), createDto.email(), createDto.password());
        Long id = memberRepository.save(memberWithoutId);
        Member member = Member.assignId(id, memberWithoutId);
        return MemberDto.from(member);
    }

    public MemberDto getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 사용자가 없습니다."));
        return MemberDto.from(member);
    }

    public MemberDto getMemberBy(String email, String password) {
        Member member = memberRepository.findBy(email, password)
                .orElseThrow(() -> new NotFoundException("이메일과 비밀번호가 일치하는 사용자가 없습니다."));
        return MemberDto.from(member);
    }

    public List<MemberDto> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return MemberDto.from(members);

    }
}
