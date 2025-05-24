package roomescape.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
import roomescape.member.dto.MemberCreateRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member findMemberByEmailAndPassword(final String email, final String password) {
        return memberRepository.findByEmailAndPassword(new Email(email), new Password(password))
                .orElseThrow(() -> new EntityNotFoundException("이메일 또는 패스워드가 잘못 되었습니다."));
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void create(final @Valid MemberCreateRequest request) {
        final Member member = new Member(
                request.name(),
                request.email(),
                request.password(),
                Role.MEMBER
        );
        memberRepository.save(member);
    }
}
