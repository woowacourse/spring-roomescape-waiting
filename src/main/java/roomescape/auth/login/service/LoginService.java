package roomescape.auth.login.service;

import org.springframework.stereotype.Service;
import roomescape.admin.domain.Admin;
import roomescape.admin.service.AdminService;
import roomescape.auth.exception.UnauthorizedException;
import roomescape.auth.token.JwtTokenManager;
import roomescape.auth.login.presentation.dto.LoginRequest;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@Service
public class LoginService {

    private final JwtTokenManager jwtTokenManager;
    private final AdminService adminService;
    private final MemberService memberService;

    public LoginService(JwtTokenManager jwtTokenManager, AdminService adminService, MemberService memberService) {
        this.jwtTokenManager = jwtTokenManager;
        this.adminService = adminService;
        this.memberService = memberService;
    }

    public String createAdminToken(final LoginRequest request) {
        validateAdminExistsAccount(request);

        Admin admin = adminService.findByEmail(request.email());
        validateAdminSamePassword(request, admin);

        return jwtTokenManager.createToken(admin.getId(), "ADMIN");
    }

    public String createMemberToken(final LoginRequest request) {
        validateMemberExistsAccount(request);

        Member member = memberService.findByEmail(request.email());
        validateMemberSamePassword(request, member);

        return jwtTokenManager.createToken(member.getId(), "MEMBER");
    }

    private static void validateAdminSamePassword(final LoginRequest request, final Admin admin) {
        if (!admin.isSamePassword(request.password())) {
            throw new UnauthorizedException("비밀번호가 틀립니다.");
        }
    }

    private static void validateMemberSamePassword(final LoginRequest request, final Member member) {
        if (!member.isSamePassword(request.password())) {
            throw new UnauthorizedException("비밀번호가 틀립니다.");
        }
    }

    private void validateAdminExistsAccount(final LoginRequest request) {
        boolean adminExist = adminService.isExistsByEmail(request.email());
        if (!adminExist) {
            throw new UnauthorizedException("계정이 존재하지 않습니다.");
        }
    }

    private void validateMemberExistsAccount(final LoginRequest request) {
        boolean memberExist = memberService.isExistsByEmail(request.email());
        if (!memberExist) {
            throw new UnauthorizedException("계정이 존재하지 않습니다.");
        }
    }

    public Admin findByAdminId(final Long id) {
        return adminService.findById(id);
    }

    public Member findByMemberId(final Long id) {
        return memberService.findById(id);
    }
}
