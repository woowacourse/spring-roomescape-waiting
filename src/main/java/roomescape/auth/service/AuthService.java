package roomescape.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.jwt.JwtTokenProvider;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.repository.MemberRepositoryInterface;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberRepositoryInterface memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public String findNameByToken(final String token) {
        final String payload = jwtTokenProvider.getPayload(token);

        return memberRepository.findNameByEmail(payload)
                .orElseThrow(() -> new DataNotFoundException("해당 회원 데이터가 존재하지 않습니다. email = " + payload));
    }

    public String createToken(final String email, final String password) {
        if (!checkInvalidLogin(email, password)) {
            throw new DataNotFoundException("No member information");
        }

        return jwtTokenProvider.createToken(email);
    }

    private boolean checkInvalidLogin(final String email, final String password) {
        return memberRepository.existsByEmailAndPassword(email, password);
    }
}
