package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import roomescape.exception.ArgumentNullException;
import roomescape.member.domain.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Embedded
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;

    private Waiting(final Long id, final Member member, final ReservationTime reservationTime, final Theme theme) {
        validateNull(member, reservationTime, theme);
        this.id = id;
        this.member = member;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    protected Waiting() {
    }

    private static void validateNull(Member member, ReservationTime reservationTime, Theme theme) {
        if (member == null) {
            throw new ArgumentNullException("member");
        }
        if (reservationTime == null) {
            throw new ArgumentNullException("reservationTime");
        }
        if (theme == null) {
            throw new ArgumentNullException("theme");
        }
    }

    public boolean isSameMember(Member member) {
        return Objects.equals(this.member, member);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Waiting waiting)) {
            return false;
        }
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Reservation convertToReservation() {
        return Reservation.builder()
                .reservationTime(reservationTime)
                .theme(theme)
                .member(member).build();
    }

    public static class Builder {

        private Long id;
        private Member member;
        private ReservationTime reservationTime;
        private Theme theme;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder member(Member member) {
            this.member = member;
            return this;
        }

        public Builder reservationTime(ReservationTime reservationTime) {
            this.reservationTime = reservationTime;
            return this;
        }

        public Builder theme(Theme theme) {
            this.theme = theme;
            return this;
        }

        public Waiting build() {
            return new Waiting(id, member, reservationTime, theme);
        }
    }
}
