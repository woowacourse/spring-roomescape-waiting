package roomescape.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.jwt.JwtTokenProvider;
import roomescape.common.exception.AccessDeniedException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepositoryInterface;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberRepositoryInterface memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public String findNameByToken(final String token) {
        final String payload = jwtTokenProvider.getPayload(token);

        return memberRepository.findNameByEmail(payload)
                .orElseThrow(() -> new DataNotFoundException("해당 회원 데이터가 존재하지 않습니다. email = " + payload));
    }

    @Transactional
    public String createToken(final String email, final String password) {
        if (!checkInvalidLogin(email, password)) {
            throw new DataNotFoundException("No member information");
        }

        return jwtTokenProvider.createToken(email);
    }

    @Transactional(readOnly = true)
    public void validateAdminByToken(final String token) {
        final String email = jwtTokenProvider.getPayload(token);
        final Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("회원 정보가 없습니다."));

        if (member.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("관리자만 접근 가능합니다.");
        }
    }

    private boolean checkInvalidLogin(final String email, final String password) {
        return memberRepository.existsByEmailAndPassword(email, password);
    }
}
