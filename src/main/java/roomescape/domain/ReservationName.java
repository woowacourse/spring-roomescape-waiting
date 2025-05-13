package roomescape.domain;

public class ReservationName {

    private final Long id;
    private final String name;

    public ReservationName(Long id, String name) {
        validateName(name);

        this.id = id;
        this.name = name;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 예약자의 이름은 1글자 이상으로 이루어져야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
