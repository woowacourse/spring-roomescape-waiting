package roomescape.service.dto.result;

import java.util.Arrays;

public enum BookingType {
    RESERVED("예약"),
    WAITED("대기"),
    ;

    private final String displayName;

    BookingType(String displayName) {
        this.displayName = displayName;
    }

    public static BookingType from(String type) {
        return Arrays.stream(BookingType.values())
                .filter(bookingType -> bookingType.name().equalsIgnoreCase(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("해당 type을 BookingType으로 변환할 수 없습니다: " + type));
    }

    public String getDisplayName() {
        return displayName;
    }
}
