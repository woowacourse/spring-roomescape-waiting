package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;

public class ReservationDeleteDto {
    @NotNull
    private String name;

    public ReservationDeleteDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
