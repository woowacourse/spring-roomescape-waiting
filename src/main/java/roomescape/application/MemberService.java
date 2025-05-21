package roomescape.application;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.MemberCreateServiceRequest;
import roomescape.application.dto.MemberServiceResponse;
import roomescape.domain.Role;
import roomescape.domain.entity.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberServiceResponse registerMember(@Valid MemberCreateServiceRequest createDto) {
        Member memberWithoutId = Member.withoutId(
                createDto.name(),
                createDto.email(),
                createDto.password(),
                Role.USER
        );
        Member savedMember = memberRepository.save(memberWithoutId);
        return MemberServiceResponse.from(savedMember);
    }

    public List<MemberServiceResponse> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return MemberServiceResponse.from(members);
    }

    public MemberServiceResponse getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 사용자가 없습니다."));
        return MemberServiceResponse.from(member);
    }

    public MemberServiceResponse getMemberBy(String email, String password) {
        Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new NotFoundException("이메일과 비밀번호가 일치하는 사용자가 없습니다."));
        return MemberServiceResponse.from(member);
    }
}
