package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.exception.DomainValidationException;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.detail.ReservationDetail;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationDetail detail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    protected Waiting() {
    }

    public Waiting(ReservationDetail detail, Member member) {
        this(null, detail, member);
    }

    public Waiting(Long id, ReservationDetail detail, Member member) {
        validate(detail, member);

        this.id = id;
        this.detail = detail;
        this.member = member;
    }

    public static Waiting create(
            LocalDateTime currentDateTime,
            ReservationDetail detail,
            Member member
    ) {
        if (detail.isBefore(currentDateTime)) {
            String message = String.format("지나간 날짜/시간에 대한 예약 대기는 불가능합니다. (예약 날짜: %s, 예약 시간: %s)",
                    detail.getDate(), detail.getTime().getStartAt());

            throw new DomainValidationException(message);
        }

        return new Waiting(detail, member);
    }

    private void validate(ReservationDetail reservationDetail, Member member) {
        if (reservationDetail == null) {
            throw new DomainValidationException("예약 상세는 필수 값입니다.");
        }

        if (member == null) {
            throw new DomainValidationException("회원은 필수 값입니다.");
        }
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Waiting waiting)) {
            return false;
        }

        return this.getId() != null && Objects.equals(getId(), waiting.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public Long getId() {
        return id;
    }

    public ReservationDetail getDetail() {
        return detail;
    }

    public Member getMember() {
        return member;
    }
}
