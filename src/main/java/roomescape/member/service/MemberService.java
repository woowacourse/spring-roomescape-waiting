package roomescape.member.service;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.request.MemberCreationRequest;
import roomescape.auth.dto.response.MemberCreationUpResponse;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.exception.ConflictException;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.dto.response.MemberNameResponse;
import roomescape.member.repository.MemberRepository;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberCreationUpResponse create(MemberCreationRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new ConflictException("회원 정보가 이미 존재합니다.");
        }
        return MemberCreationUpResponse.of(
            memberRepository.save(request.toMember()),
            true
        );
    }

    public Member findByIdOrThrow(Long id) {
        return memberRepository.findById(id).
            orElseThrow(() -> new NotFoundException("존재하지 않은 회원 정보입니다."));
    }

    public Member findByPrincipalOrThrow(MemberPrincipal memberPrincipal) {
        return memberRepository.findByName(memberPrincipal.name())
            .orElseThrow(() -> new NotFoundException("존재하지 않은 회원 정보입니다."));
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public boolean existsById(Long id) {
        return memberRepository.existsById(id);
    }

    public List<MemberNameResponse> findNames() {
        return memberRepository.findAll().stream()
            .map(MemberNameResponse::from)
            .toList();
    }

    public boolean existsByName(String name) {
        return memberRepository.existsByName(name);
    }

    public Optional<Member> findByEmailAndPassword(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return memberRepository.findByEmailAndPassword(email, password);
    }
}
