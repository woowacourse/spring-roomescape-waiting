package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Entity
public class Waiting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waiting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private Time time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    protected Waiting() {
    }

    public Waiting(Member member, LocalDate date, Time time, Theme theme) {
        this(null, member, date, time, theme);
    }

    public Waiting(Long id, Member member, LocalDate date, Time time, Theme theme) {
        if (member == null) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "예약자는 필수입니다.");
        }
        if (date == null) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "예약 날짜는 필수입니다.");
        }
        if (time == null) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "예약 시간은 필수입니다.");
        }
        if (theme == null) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "테마는 필수입니다.");
        }
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation toReservation() {
        return new Reservation(member, date, time, theme);
    }

    public boolean isPast(Clock clock) {
        LocalDateTime waitingDateTime = LocalDateTime.of(date, time.getStartAt());
        return waitingDateTime.isBefore(LocalDateTime.now(clock));
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Waiting waiting)) {
            return false;
        }
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
