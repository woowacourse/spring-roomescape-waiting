package roomescape.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @Valid
    @NotNull
    @Embedded
    private ReservationInfo reservationInfo;

    private Reservation(Long id, Member member, ReservationInfo reservationInfo) {
        this.id = id;
        this.member = member;
        this.reservationInfo = reservationInfo;
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme, Member member) {
        this(null, member, new ReservationInfo(date, time, theme));
    }

    public Reservation(Member member, ReservationInfo reservationInfo) {
        this(null, member, reservationInfo);
    }

    protected Reservation() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return reservationInfo.getDate();
    }

    public ReservationTime getTime() {
        return reservationInfo.getTime();
    }

    public Theme getTheme() {
        return reservationInfo.getTheme();
    }

    public Member getMember() {
        return member;
    }

    public ReservationInfo getReservationInfo() {
        return reservationInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reservationInfo, member);
    }
}
