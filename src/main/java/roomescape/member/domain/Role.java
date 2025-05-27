package roomescape.member.domain;

import java.util.Arrays;
import roomescape.common.exception.NotFoundException;

public enum Role {

    MEMBER("1", "회원"),
    ADMIN("2", "관리자"),
    USER("3", "사용자");

    private final String code;
    private final String description;

    Role(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Role from(String value) {
        return Arrays.stream(values())
                .filter(role -> role.name().equals(value.toUpperCase()))
                .findFirst()
                .orElse(Role.USER);
    }

    public static Role ofCode(String code) {
        return Arrays.stream(values())
                .filter(role -> role.getCode().equals(code))
                .findAny()
                .orElseThrow(() -> new NotFoundException("해당 코드에 대한 권한이 존재하지 않습니다."));
    }

    public boolean isEqual(Role role) {
        return this.equals(role);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
