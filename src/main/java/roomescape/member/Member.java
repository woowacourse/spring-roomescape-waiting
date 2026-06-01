package roomescape.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Member {
    private final Long id;
    private final String name;
    private final String password;
    private final Role role;

    public boolean isSamePassword(String otherPassword) {
        return this.password.equals(otherPassword);
    }
}
