package roomescape.login.application.port.in;

import roomescape.member.domain.AuthenticatedMember;

public interface LoginUseCase {
    AuthenticatedMember login(String name, String password);
}
