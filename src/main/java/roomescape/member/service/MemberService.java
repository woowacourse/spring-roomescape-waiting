package roomescape.member.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginRequest;
import roomescape.member.dto.SignupRequest;
import roomescape.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member signup(SignupRequest request) {
        Member member = Member.of(request.name(), request.email(), request.password());
        return memberRepository.save(member);
    }

    public Member login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));
        if (!member.getPassword().equals(request.password())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다.");
        }
        return member;
    }

    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));
    }
}
