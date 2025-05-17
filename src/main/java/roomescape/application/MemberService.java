package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.infrastructure.repository.MemberRepository;
import roomescape.presentation.dto.request.MemberCreateRequest;
import roomescape.presentation.dto.response.MemberResponse;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 해당 이메일로 가입한 회원이 존재하지 않습니다."));
    }

    public List<MemberResponse> getMembers() {
        List<Member> members = memberRepository.findAll();
        return MemberResponse.from(members);
    }

    @Transactional
    public MemberResponse createMember(MemberCreateRequest request) {
        Member member = Member.create(request.name(), Role.USER, request.email(), request.password());
        Member created = memberRepository.save(member);
        return MemberResponse.from(created);
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 해당 회원이 존재하지 않습니다."));
    }
}
