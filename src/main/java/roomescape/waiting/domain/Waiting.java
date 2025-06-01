package roomescape.waiting.domain;

import jakarta.persistence.*;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Waiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationInformation reservationInformation;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member member;

    private LocalDateTime createdAt;

    protected Waiting() {
    }

    public Waiting(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member,
            final LocalDateTime createdAt
    ) {
        this(new ReservationInformation(date, time, theme), member, createdAt);
    }

    public Waiting(ReservationInformation reservationInformation, Member member, LocalDateTime createdAt) {
        this.reservationInformation = reservationInformation;
        this.member = member;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public ReservationInformation getReservationInformation() {
        return reservationInformation;
    }

    public LocalDate getDate() {
        return reservationInformation.getDate();
    }

    public ReservationTime getTime() {
        return reservationInformation.getTime();
    }

    public Theme getTheme() {
        return reservationInformation.getTheme();
    }

    public Member getMember() {
        return member;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
