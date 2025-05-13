package roomescape.business.domain;

import java.time.LocalDate;

public class Reservation {

    private final Long id;
    private final LocalDate date;
    private final Member member;
    private final PlayTime playTime;
    private final Theme theme;

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
