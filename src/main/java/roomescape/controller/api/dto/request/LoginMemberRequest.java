package roomescape.controller.api.dto.request;

import roomescape.domain.user.Email;
import roomescape.domain.user.Member;
import roomescape.domain.user.Name;
import roomescape.domain.user.Password;
import roomescape.domain.user.Role;

public record LoginMemberRequest(long id, String email, String password, String name, String role) {
    public Member toMember() {
        return new Member(id, new Name(name), new Email(email), new Password(password), Role.from(role));
    }
}
