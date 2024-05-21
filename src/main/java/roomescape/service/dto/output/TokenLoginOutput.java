package roomescape.service.dto.output;

import roomescape.domain.user.Member;
import roomescape.domain.user.Role;

public record TokenLoginOutput(long id, String name, String email, String password, String role) {
    public static TokenLoginOutput toOutput(final Member member) {
        return new TokenLoginOutput(member.getId(), member.getName(), member.getEmail(), member.getPassword(), member.getRole());
    }

    public boolean isAdmin() {
        return Role.ADMIN.getValue()
                .equals(role);
    }
}
