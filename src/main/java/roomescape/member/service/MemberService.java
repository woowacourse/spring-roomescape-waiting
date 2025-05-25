package roomescape.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.request.MemberSignUpRequest;
import roomescape.auth.dto.response.MemberSignUpResponse;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.exception.ConflictException;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.dto.response.MemberNameSelectResponse;
import roomescape.member.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberSignUpResponse signup(MemberSignUpRequest request) {
        checkAvailableSignup(request);
        Member member = memberRepository.save(request.toMember());
        return MemberSignUpResponse.of(member, true);
    }

    private void checkAvailableSignup(MemberSignUpRequest request) {
        String email = request.email();
        boolean existsMember = memberRepository.findByEmail(email).isPresent();
        validateConflictMember(existsMember);
    }

    private void validateConflictMember(boolean existsMember) {
        if (existsMember) {
            throw new ConflictException("회원 정보가 이미 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public Member getExistingMemberByMemberId(Long id) {
        return memberRepository.findById(id).
                orElseThrow(() -> new NotFoundException("회원 정보가 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public Member findExistingMemberByPrincipal(MemberPrincipal memberPrincipal) {
        return memberRepository.findByName(memberPrincipal.name())
                .orElseThrow(() -> new NotFoundException("회원 정보가 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<MemberNameSelectResponse> findMemberNames() {
        List<Member> members = memberRepository.findAll();
        return convertMemberNameSelectResponseTo(members);
    }

    private List<MemberNameSelectResponse> convertMemberNameSelectResponseTo(List<Member> members) {
        return members.stream()
                .map(MemberNameSelectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return memberRepository.findByName(name).isPresent();
    }
}
