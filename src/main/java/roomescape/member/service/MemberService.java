package roomescape.member.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.request.MemberSignUpRequest;
import roomescape.auth.dto.response.MemberSignUpResponse;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.exception.ConflictException;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.dto.response.MemberNameSelectResponse;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberSignUpResponse signup(MemberSignUpRequest request) {
        String email = request.email();
        boolean alreadyExistMember = memberRepository.findByEmail(email).isPresent();
        if (alreadyExistMember) {
            throw new ConflictException("회원 정보가 이미 존재합니다.");
        }
        Member member = memberRepository.save(request.toMember());
        return MemberSignUpResponse.of(member, true);
    }

    public Member findExistingMemberById(Long id) {
        return memberRepository.findById(id).
            orElseThrow(() -> new NotFoundException("회원 정보가 존재하지 않습니다."));
    }

    public Member findExistingMemberByPrincipal(MemberPrincipal memberPrincipal) {
        return memberRepository.findByName(memberPrincipal.name())
            .orElseThrow(() -> new NotFoundException("회원 정보가 존재하지 않습니다."));
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public boolean isExistMemberById(Long id) {
        return memberRepository.findById(id).isPresent();
    }

    public List<MemberNameSelectResponse> findMemberNames() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
            .map(MemberNameSelectResponse::from)
            .toList();
    }

    public boolean existsByName(String name) {
        return memberRepository.findByName(name).isPresent();
    }
}
