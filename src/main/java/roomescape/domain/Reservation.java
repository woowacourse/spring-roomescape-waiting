package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation(final Long id, final Member member, final LocalDate date, final ReservationTime time, final Theme theme,
                       final ReservationStatus status) {
        this.id = id;
        this.member = Objects.requireNonNull(member, "예약 회원은 null일 수 없습니다.");
        this.date = Objects.requireNonNull(date, "예약 날짜는 null일 수 없습니다.");
        this.time = Objects.requireNonNull(time, "예약 시간은 null일 수 없습니다.");
        this.theme = Objects.requireNonNull(theme, "예약 테마는 null일 수 없습니다.");
        this.status = Objects.requireNonNull(status, "예약 상태는 null일 수 없습니다.");
    }

    public Reservation(final Member member, final LocalDate date, final ReservationTime time, final Theme theme, final ReservationStatus status) {
        this(null, member, date, time, theme, status);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
