package roomescape.holiday.domain;

import java.time.LocalDate;

public record Holiday(Long id, LocalDate date) {
    public Holiday(LocalDate date) {
        this(null, date);
    }

    public Holiday withId(Long id) {
        return new Holiday(id, this.date);
    }
}
