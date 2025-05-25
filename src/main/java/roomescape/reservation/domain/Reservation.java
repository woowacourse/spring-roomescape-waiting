package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Embedded
    private ReservationDetails details;

    public Reservation() {
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        validateMember(member);

        this.id = id;
        this.member = member;
        this.details = new ReservationDetails(date, time, theme);
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("[ERROR] 회원 정보는 반드시 입력해야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationDetails getDetails() {
        return details;
    }

    public LocalDate getDate() {
        return details.getDate();
    }

    public ReservationTime getTime() {
        return details.getTime();
    }

    public Theme getTheme() {
        return details.getTheme();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reservation that)) return false;
        return Objects.equals(member, that.member) && Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(member, details);
    }
}
