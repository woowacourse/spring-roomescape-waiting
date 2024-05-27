package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationContent reservationContent;

    private LocalDateTime createdAt;

    public Reservation() {
    }

    public Reservation(Member member, ReservationContent reservationContent) {
        this.member = member;
        this.reservationContent = reservationContent;
    }

    public Reservation(Member member, LocalDate date, Time time, Theme theme) {
        this(member, new ReservationContent(date, time, theme));
    }

    public Reservation(Long id, Member member, ReservationContent reservationContent, LocalDateTime createdAt) {
        this.id = id;
        this.member = member;
        this.reservationContent = reservationContent;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isReservedDateBetween(LocalDate start, LocalDate end) {
        return start.isBefore(reservationContent.getDate()) && end.isAfter(reservationContent.getDate());
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Long getMemberId() {
        return member.getId();
    }

    public ReservationContent getReservationContent() {
        return reservationContent;
    }

    public Long getReservationContentId() {
        return reservationContent.getId();
    }

    public Time getTime() {
        return reservationContent.getTime();
    }

    public Long getTimeId() {
        return reservationContent.getTime().getId();
    }

    public Theme getTheme() {
        return reservationContent.getTheme();
    }

    public Long getThemeId() {
        return reservationContent.getTheme().getId();
    }

    public LocalDate getDate() {
        return reservationContent.getDate();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(member, that.member)
                && Objects.equals(reservationContent, that.reservationContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, reservationContent);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", member=" + member +
                ", reservationContent=" + reservationContent +
                '}';
    }

}
