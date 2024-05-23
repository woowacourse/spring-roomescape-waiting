package roomescape.domain.reservation;

import jakarta.persistence.Column;
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
import roomescape.domain.exception.DomainValidationException;
import roomescape.domain.member.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;

    protected Waiting() {
    }

    public Waiting(
            LocalDate date,
            Member member,
            ReservationTime time,
            Theme theme
    ) {
        this(null, date, member, time, theme);
    }

    public Waiting(
            Long id,
            LocalDate date,
            Member member,
            ReservationTime time,
            Theme theme
    ) {
        validate(date, member, time, theme);

        this.id = id;
        this.date = date;
        this.member = member;
        this.time = time;
        this.theme = theme;
    }

    public static Waiting create(
            LocalDateTime currentDateTime,
            LocalDate date,
            Member member,
            ReservationTime time,
            Theme theme
    ) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());

        if (reservationDateTime.isBefore(currentDateTime)) {
            String message = String.format("지나간 날짜/시간에 대한 예약 대기은 불가능합니다. (예약 날짜: %s, 예약 시간: %s)", date,
                    time.getStartAt());

            throw new DomainValidationException(message);
        }

        return new Waiting(date, member, time, theme);
    }

    private void validate(
            LocalDate date,
            Member member,
            ReservationTime time,
            Theme theme
    ) {
        if (date == null) {
            throw new DomainValidationException("날짜는 필수 값입니다.");
        }

        if (member == null) {
            throw new DomainValidationException("회원은 필수 값입니다.");
        }

        if (time == null) {
            throw new DomainValidationException("예약 시간은 필수 값입니다.");
        }

        if (theme == null) {
            throw new DomainValidationException("테마는 필수 값입니다.");
        }
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Waiting that = (Waiting) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Member getMember() {
        return member;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
