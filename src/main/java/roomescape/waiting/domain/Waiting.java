package roomescape.waiting.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.ThemeName;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public Waiting(final LocalDateTime createdAt, final Reservation reservation, final Member member) {
        this.createdAt = createdAt;
        this.reservation = reservation;
        this.member = member;
    }

    protected Waiting() {

    }

    public static Waiting register(final Member member, final Reservation reservation) {
        if (reservation.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("지나간 날짜와 시간에 예약 대기할 수 없습니다.");
        }
        return new Waiting(LocalDateTime.now(), reservation, member);
    }

    public boolean hasOwner(final Member other) {
        return this.member.equals(other);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Member getMember() {
        return member;
    }

    public ThemeName getThemeName() {
        return reservation.getThemeName();
    }

    public LocalDate getDate() {
        return reservation.getDate();
    }

    public LocalTime getStartAt() {
        return reservation.getStartAt();
    }

    public MemberName getMemberName() {
        return member.getName();
    }

    @Override
    public final boolean equals(final Object o) {
        if (!(o instanceof final Waiting waiting)) {
            return false;
        }

        return Objects.equals(getId(), waiting.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
