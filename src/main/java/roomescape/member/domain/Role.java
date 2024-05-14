package roomescape.member.domain;

import java.util.Arrays;
import java.util.List;
import roomescape.exceptions.NotFoundException;

public enum Role {
    ADMIN("admin"),
    USER("user");

    private final String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    public static Role getByDbValue(String dbValue) {
        List<Role> matchingRoles = Arrays.stream(Role.values())
                .filter(role -> role.getDbValue().equals(dbValue))
                .toList();

        if (matchingRoles.isEmpty()) {
            throw new NotFoundException(dbValue + "와 매칭되는 Role을 찾을 수 없습니다.");
        } else if (matchingRoles.size() > 1) {
            throw new IllegalStateException(dbValue + "와 매칭되는 Role이 유일하지 않습니다.");
        }

        return matchingRoles.get(0);
    }

    public String getDbValue() {
        return dbValue;
    }
}
