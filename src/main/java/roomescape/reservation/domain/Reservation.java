package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode(of = {"date", "themeId", "timeId"})
public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final Long themeId;
    private final Long timeId;
    private final ReservationStatus status;
    private final Long amount;
    private final LocalDateTime expiresAt;

    public Reservation withId(Long generatedId) {
        return Reservation.builder()
                .id(generatedId)
                .name(this.name)
                .date(this.date)
                .themeId(this.themeId)
                .timeId(this.timeId)
                .status(this.status)
                .amount(this.amount)
                .expiresAt(this.expiresAt)
                .build();
    }

    public Reservation update(LocalDate date, Long timeId) {
        return Reservation.builder()
                .id(this.id)
                .name(this.name)
                .date(date)
                .themeId(this.themeId)
                .timeId(timeId)
                .status(this.status)
                .amount(this.amount)
                .expiresAt(this.expiresAt)
                .build();
    }

    public boolean isOwner(String name) {
        return this.name.equals(name);
    }
}
