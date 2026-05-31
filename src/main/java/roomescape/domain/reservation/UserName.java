package roomescape.domain.reservation;

public record UserName(
        String value
) {
    public static final int NAME_MAX_LENGTH = 10;

    public UserName {
        if (value.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 비어 있을 수 없습니다.");
        }

        if (value.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("예약자 이름은 %d자를 초과할 수 없습니다.".formatted(NAME_MAX_LENGTH));
        }
    }

    public static UserName parse(String value) {
        return new UserName(value);
    }

    public boolean isOwnedBy(String name) {
        return value.equals(name);
    }
}
