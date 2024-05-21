package roomescape.application.member;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.auth.TokenManager;
import roomescape.application.auth.dto.TokenPayload;
import roomescape.application.member.dto.request.MemberLoginRequest;
import roomescape.application.member.dto.request.MemberRegisterRequest;
import roomescape.application.member.dto.response.MemberResponse;
import roomescape.application.member.dto.response.TokenResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final TokenManager tokenManager;

    public MemberService(MemberRepository memberRepository, TokenManager tokenManager) {
        this.memberRepository = memberRepository;
        this.tokenManager = tokenManager;
    }

    @Transactional
    public MemberResponse register(MemberRegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        Member member = memberRepository.save(request.toMember());
        return MemberResponse.from(member);
    }

    @Transactional
    public TokenResponse login(MemberLoginRequest request) {
        Member member = memberRepository.getByEmail(request.email());
        if (!member.matchPassword(request.password())) {
            throw new IllegalArgumentException("이메일 / 비밀번호를 확인해 주세요.");
        }
        String token = tokenManager.createToken(TokenPayload.from(member));
        return new TokenResponse(token);
    }

    public MemberResponse findById(Long memberId) {
        Member member = memberRepository.getById(memberId);
        return MemberResponse.from(member);
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }
}
