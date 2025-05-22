package roomescape.service.dto.result;

public enum BookingType {
    RESERVED("예약"),
    WAITED("대기"),
    ;

    private final String displayName;

    BookingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
