package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.timeslot.domain.TimeSlot;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private TimeSlot time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public Reservation(final LocalDate date, final TimeSlot time, final Theme theme, final Member member) {
        this(null, date, time, theme, member);
    }

    public boolean isSameTime(final TimeSlot time) {
        return this.time.equals(time);
    }

    public boolean isThemeSameId(final long id) {
        return theme.isSameId(id);
    }

    public boolean isMemberSameId(final long id) {
        return member.isSameId(id);
    }

    public boolean isDateBetween(final LocalDate from, final LocalDate to) {
        return (date.isEqual(from) || date.isAfter(from)) &&
                (date.isEqual(to) || date.isBefore(to));
    }

    public boolean isOwnedBy(final Member member) {
        return this.member.equals(member);
    }

    public boolean isPast(final LocalDate currentDate, final LocalTime currentTime) {
        return date.isBefore(currentDate) || (date.isEqual(currentDate) && time.isBefore(currentTime));
    }
}
