package roomescape.domain.member.model;

import java.util.Arrays;
import java.util.Objects;
import roomescape.global.exception.ResourceNotFoundException;

public enum Role {
    USER, ADMIN;

    public static Role findByName(String name) {
        return Arrays.stream(Role.values())
                .filter(role -> Objects.equals(role.name(), name))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(name + "으로된 역할이 존재하지 않습니다"));
    }
}
