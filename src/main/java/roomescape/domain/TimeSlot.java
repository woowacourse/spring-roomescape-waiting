package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalTime;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@EqualsAndHashCode(of = {"id"})
@Getter
@Accessors(fluent = true)
@ToString
@Entity
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;
    @OneToMany(mappedBy = "time")
    private Set<Reservation> reservations;

    public TimeSlot(final LocalTime startAt) {
        this(null, startAt);
    }

    public TimeSlot(final Long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    protected TimeSlot() {
    }

    public TimeSlot withId(final long id) {
        if (this.id == null) {
            this.id = id;
            return this;
        }
        throw new IllegalStateException("타임 슬롯 ID는 재할당할 수 없습니다. 현재 ID: " + this.id);
    }

    public boolean isTimeBefore(final LocalTime time) {
        return this.startAt.isBefore(time);
    }

    public boolean isSameAs(final TimeSlot timeSlot) {
        return this.id.equals(timeSlot.id());
    }
}
