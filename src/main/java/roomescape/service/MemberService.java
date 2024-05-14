package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.dao.MemberDao;
import roomescape.domain.member.Member;
import roomescape.dto.MemberResponse;
import roomescape.dto.auth.TokenRequest;
import roomescape.dto.auth.TokenResponse;
import roomescape.auth.JwtTokenProvider;

import java.util.List;

@Service
public class MemberService {

    private final MemberDao memberDao;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberService(final MemberDao memberDao, final JwtTokenProvider jwtTokenProvider) {
        this.memberDao = memberDao;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenResponse createToken(final TokenRequest request) {
        final Member member = memberDao.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException(request.email() + "에 해당하는 사용자가 없습니다"));
        member.checkIncorrectPassword(request.password());
        final String accessToken = jwtTokenProvider.createToken(member);
        return new TokenResponse(accessToken);
    }

    public MemberResponse findById(final Long id) {
        final Member member = memberDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + "에 해당하는 사용자가 없습니다"));
        return new MemberResponse(member.getId(), member.getNameString(), member.getEmail(), member.getRole());
    }

    public MemberResponse findMemberByToken(final String accessToken) {
        final Long memberId = jwtTokenProvider.getMemberIdByToken(accessToken);
        return findById(memberId);
    }

    public List<MemberResponse> findAll() {
        final List<Member> members = memberDao.findAll();
        return members.stream()
                .map(MemberResponse::from)
                .toList();
    }
}
