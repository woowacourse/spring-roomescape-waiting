package roomescape.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginRequest;
import roomescape.member.dto.MemberResponse;
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
    public MemberResponse signup(SignupRequest request) {
        Member member = Member.restore(null, request.name(), request.email(), request.password());
        Member saved = memberRepository.save(member);
        return MemberResponse.from(saved);
    }

    public Member login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (!member.getPassword().equals(request.password())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        return member;
    }

    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
