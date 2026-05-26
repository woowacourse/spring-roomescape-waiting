package roomescape.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Member {
    private final Long id;
    private String name;
    private String password;
    private Role role;

    public boolean isSamePassword(String otherPassword) {
        return this.password.equals(otherPassword);
    }
}
