package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.member.Member;

@Entity
public class Reservation {

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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    protected Reservation() {
    }

    public Reservation(
            LocalDate date,
            Member member,
            ReservationTime time,
            Theme theme,
            ReservationStatus status
    ) {
        this(null, date, member, time, theme, status);
    }

    public Reservation(
            Long id,
            LocalDate date,
            Member member,
            ReservationTime time,
            Theme theme,
            ReservationStatus status
    ) {
        validate(date, member, time, theme, status);

        this.id = id;
        this.date = date;
        this.member = member;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation create(
            LocalDateTime currentDateTime,
            LocalDate date,
            Member member,
            ReservationTime time,
            Theme theme,
            ReservationStatus status
    ) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());

        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new IllegalArgumentException("지나간 날짜/시간에 대한 예약은 불가능합니다.");
        }

        return new Reservation(date, member, time, theme, status);
    }

    private void validate(
            LocalDate date,
            Member member,
            ReservationTime time,
            Theme theme,
            ReservationStatus status
    ) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 필수 값입니다.");
        }

        if (member == null) {
            throw new IllegalArgumentException("회원은 필수 값입니다.");
        }

        if (time == null) {
            throw new IllegalArgumentException("예약 시간은 필수 값입니다.");
        }

        if (theme == null) {
            throw new IllegalArgumentException("테마는 필수 값입니다.");
        }

        if (status == null) {
            throw new IllegalArgumentException("예약 상태는 필수 값입니다.");
        }
    }

    public void updateToReserved() {
        if (status != ReservationStatus.WAITING) {
            throw new IllegalArgumentException("예약 대기 상태에서만 예약으로 변경할 수 있습니다.");
        }

        status = ReservationStatus.RESERVED;
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

    public ReservationStatus getStatus() {
        return status;
    }
}
