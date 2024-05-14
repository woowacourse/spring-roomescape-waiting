package roomescape.service.dto.input;

import roomescape.domain.user.Member;
import roomescape.domain.user.Role;

public record MemberCreateInput(String name, String email, String password, Role role) {
    public MemberCreateInput(final String name, final String email, final String password){
        this(name,email,password,Role.USER);
    }
    public Member toMember() {
        return Member.from(null, name, email, password,role.getValue());
    }
}
