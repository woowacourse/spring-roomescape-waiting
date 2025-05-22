package roomescape.member.domain;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import roomescape.member.exception.MemberRoleNotExistsException;

@AllArgsConstructor
public enum Role {
    ADMIN("admin"),
    USER("user");

    private final String name;

    public static Role findBy(String name) {
        return Arrays.stream(Role.values())
                .filter(role -> role.getName().equals(name))
                .findAny()
                .orElseThrow(MemberRoleNotExistsException::new);
    }

    public String getName() {
        return name;
    }
}
