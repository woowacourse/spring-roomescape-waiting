package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import roomescape.domain.member.Member;
import roomescape.domain.theme.Theme;
import roomescape.system.exception.RoomescapeException;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Embedded
    private Date date;

    @ManyToOne
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation(Member Member, String rawDate, ReservationTime time, Theme theme,
        ReservationStatus status) {
        this(null, Member, rawDate, time, theme, status);
        validatePastReservation(LocalDate.parse(rawDate), time);
    }

    public Reservation(Long id, Member Member, String rawDate, ReservationTime time, Theme theme,
        ReservationStatus status) {
        this.id = id;
        this.member = Member;
        this.date = new Date(rawDate);
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    protected Reservation() {
    }

    private void validatePastReservation(LocalDate date, ReservationTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new RoomescapeException("과거 예약을 추가할 수 없습니다.");
        }
        if (date.isEqual(LocalDate.now()) && time.isBeforeNow()) {
            throw new RoomescapeException("과거 예약을 추가할 수 없습니다.");
        }
    }

    public void validateDuplication(List<Reservation> others) {
        if (others.stream()
            .anyMatch(other -> date.equals(other.date) &&
                time.equals(other.time) &&
                theme.equals(other.theme))) {
            throw new RoomescapeException("해당 시간에 예약이 이미 존재합니다.");
        }
    }

    public void validateNotWaiting() {
        if (isNotWaiting()) {
            throw new RoomescapeException("대기가 아닌 예약은 삭제할 수 없습니다.");
        }
    }

    public boolean isNotWaiting() {
        return !status.isWaiting();
    }

    public boolean isWaiting() {
        return status.isWaiting();
    }

    public void validateNotMyWaiting(Long memberId) {
        if (isNotMyWaiting(memberId)) {
            throw new RoomescapeException("다른 유저의 예약 대기는 삭제할 수 없습니다.");
        }
    }

    private boolean isNotMyWaiting(Long memberId) {
        return !(member.getId() == memberId);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return member.getName();
    }

    public Date getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
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
}
