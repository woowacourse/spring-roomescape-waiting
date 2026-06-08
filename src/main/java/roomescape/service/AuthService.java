package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.MemberDao;
import roomescape.controller.dto.SignupRequest;

@Service
@Transactional(readOnly = true)
public class AuthService {

    public static final String LOGIN_MEMBER_ID = "loginMemberId";

    private final MemberDao memberDao;

    public AuthService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Member login(String loginId, String password) {
        Member member = memberDao.findByLoginId(loginId)
                .orElseThrow(this::invalidLogin);

        if (!member.matchesPassword(password)) {
            throw invalidLogin();
        }

        return member;
    }

    public Member getLoginMember(Long memberId) {
        return memberDao.findById(memberId)
                .orElseThrow(() -> new RoomescapeException(
                        DomainErrorCode.UNAUTHENTICATED,
                        "로그인이 필요합니다."
                ));
    }

    @Transactional
    public Member signup(SignupRequest request) {
        if (!request.password().equals(request.passwordConfirm())) {
            throw new RoomescapeException(DomainErrorCode.INVALID_INPUT, "비밀번호가 일치하지 않습니다.");
        }
        if (memberDao.existsByLoginId(request.loginId())) {
            throw duplicateMember();
        }

        Member member = new Member(null, request.loginId(), request.name(), request.password(), Role.USER);
        Long id;
        try {
            id = memberDao.save(member);
        } catch (DuplicateKeyException e) {
            throw duplicateMember();
        }
        return memberDao.findById(id)
                .orElseThrow(() -> new RoomescapeException(DomainErrorCode.UNAUTHENTICATED, "회원가입한 회원을 조회할 수 없습니다."));
    }

    private RoomescapeException duplicateMember() {
        return new RoomescapeException(DomainErrorCode.DUPLICATE_MEMBER, "이미 사용 중인 로그인 ID입니다.");
    }

    private RoomescapeException invalidLogin() {
        return new RoomescapeException(DomainErrorCode.INVALID_LOGIN, "로그인 ID 또는 비밀번호가 올바르지 않습니다.");
    }
}
