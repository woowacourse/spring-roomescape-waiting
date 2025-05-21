package roomescape.waiting.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;

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

    public Waiting(final Long id, final LocalDateTime createdAt, final Reservation reservation, final Member member) {
        this.id = id;
        this.createdAt = createdAt;
        this.reservation = reservation;
        this.member = member;
    }

    public Waiting() {

    }

    public static Waiting register(final Member member, final Reservation reservation) {
        if (reservation.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("지나간 날짜와 시간에 예약 대기할 수 없습니다.");
        }
        return new Waiting(null, LocalDateTime.now(), reservation, member);
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

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Waiting waiting = (Waiting) o;
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
