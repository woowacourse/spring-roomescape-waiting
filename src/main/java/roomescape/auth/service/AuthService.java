package roomescape.auth.service;

import org.springframework.stereotype.Service;

import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.LoginResponse;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.exception.LoginFailException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberDao;

@Service
public class AuthService {

    private final MemberDao memberDao;
    private final JwtTokenHandler jwtTokenHandler;

    public AuthService(final MemberDao memberDao, final JwtTokenHandler jwtTokenHandler) {
        this.memberDao = memberDao;
        this.jwtTokenHandler = jwtTokenHandler;
    }

    public LoginResponse login(final LoginRequest request) {
        Member member = memberDao.findByEmailAndPassword(request.email(), request.password())
                .orElseThrow(() -> new LoginFailException("이메일 또는 비밀번호가 잘못 되었습니다."));

        String tokenValue = jwtTokenHandler.createToken(member.getId().toString(), member.getRole());
        return new LoginResponse(tokenValue);
    }

    public Member findById(final Long id) {
        return memberDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자 입니다."));
    }
}
