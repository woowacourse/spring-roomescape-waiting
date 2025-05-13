package roomescape.business.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @ManyToOne
    private Member member;
    @ManyToOne
    private PlayTime playTime;
    @ManyToOne
    private Theme theme;

    public Reservation(final Long id, final LocalDate date, final Member member, final PlayTime playTime,
                       final Theme theme
    ) {
        validateDate(date);

        this.id = id;
        this.date = date;
        this.member = member;
        this.playTime = playTime;
        this.theme = theme;
    }

    public Reservation(final LocalDate date, final Member member, final PlayTime playTime, final Theme theme) {
        this(null, date, member, playTime, theme);
    }

    public Reservation() {

    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date 필드가 null 입니다.");
        }
    }

    public boolean isSamePlayTime(final PlayTime playTime) {
        return this.playTime.isSamePlayTime(playTime);
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

    public PlayTime getPlayTime() {
        return playTime;
    }

    public Theme getTheme() {
        return theme;
    }
}
