package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.reservation.exception.InvalidReservationException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;


@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Embedded
    private ReservationInfo info;

    protected Reservation() {
    }

    public Reservation(final Member member, final ReservationInfo info) {
        this.member = member;
        this.info = info;
    }

    public static Reservation createUpcomingReservationWithUnassignedId(final Member member,
                                                                        final ReservationInfo reservationInfo) {
        validateDateTime(reservationInfo);
        return new Reservation(member, reservationInfo);
    }

    private static void validateDateTime(ReservationInfo reservationInfo) {
        if (LocalDateTime.of(reservationInfo.getDate(), reservationInfo.getTime().getStartAt())
                .isBefore(LocalDateTime.now())) {
            throw new InvalidReservationException("예약 시간이 현재 시간보다 이전일 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationInfo getInfo() {
        return info;
    }

    public ReservationTime getTime() {
        return info.getTime();
    }

    public Theme getTheme() {
        return info.getTheme();
    }

    public LocalDate getDate() {
        return info.getDate();
    }

    public Long getTimeId() {
        return info.getIdOfTime();
    }

    public Long getThemeId() {
        return info.getIdOfTheme();
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof final Reservation that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
