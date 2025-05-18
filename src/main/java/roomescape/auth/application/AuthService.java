package roomescape.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthTokenProvider;
import roomescape.auth.ui.dto.CreateAccessTokenRequest;
import roomescape.exception.auth.AuthenticationException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthTokenProvider authTokenProvider;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public String createAccessToken(final CreateAccessTokenRequest request) {
        final Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("해당 이메일을 가진 회원이 존재하지 않습니다."));

        if (member.isWrongPassword(request.password())) {
            throw new AuthenticationException("비밀번호가 올바르지 않습니다.");
        }

        return authTokenProvider.createAccessToken(member.getId().toString(), member.getRole());
    }

    @Transactional(readOnly = true)
    public String getMemberNameById(final Long id) {
        return memberRepository.getByIdOrThrow(id)
                .getName();
    }
}
