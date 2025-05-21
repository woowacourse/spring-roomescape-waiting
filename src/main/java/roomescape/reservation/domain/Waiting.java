package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.ArgumentNullException;
import roomescape.exception.PastDateTimeReservationException;
import roomescape.member.domain.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private TimeSlot timeSlot;

    @ManyToOne
    private Theme theme;

    @Column(nullable = false)
    private LocalDate date;

    private Waiting(final Long id, final Member member, final LocalDate date, final TimeSlot timeSlot,
                    final Theme theme) {
        validateNull(member, date, timeSlot, theme);
        this.id = id;
        this.member = member;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    public Waiting() {
    }

    public static Waiting of(final Long id, final Member member, final LocalDate date,
                             final TimeSlot timeSlot, final Theme theme) {
        return new Waiting(id, member, date, timeSlot, theme);
    }

    public static Waiting createWithoutId(final Member member, final LocalDate date,
                                          final TimeSlot timeSlot, final Theme theme) {
        return new Waiting(null, member, date, timeSlot, theme);
    }

    private static void validateNull(Member member, LocalDate date, TimeSlot timeSlot, Theme theme) {
        if (member == null) {
            throw new ArgumentNullException("member");
        }
        if (date == null) {
            throw new ArgumentNullException("date");
        }
        if (timeSlot == null) {
            throw new ArgumentNullException("reservationTime");
        }
        if (theme == null) {
            throw new ArgumentNullException("theme");
        }
    }

    public boolean isSameMember(Member member) {
        return Objects.equals(this.member, member);
    }

    public void validateDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(date, timeSlot.getStartAt());
        if (LocalDateTime.now().isAfter(dateTime)) {
            throw new PastDateTimeReservationException();
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Theme getTheme() {
        return theme;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private Member member;
        private TimeSlot timeSlot;
        private Theme theme;
        private LocalDate date;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder member(Member member) {
            this.member = member;
            return this;
        }

        public Builder timeSlot(TimeSlot timeSlot) {
            this.timeSlot = timeSlot;
            return this;
        }

        public Builder theme(Theme theme) {
            this.theme = theme;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Waiting build() {
            return new Waiting(id, member, date, timeSlot, theme);
        }
    }
}
