package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "waiting")
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private Time time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    protected Waiting() {
    }

    public Waiting(String memberName, ThemeSlot themeSlot) {
        this(null, memberName, themeSlot.getDate(), themeSlot.getTime(), themeSlot.getTheme());
    }

    public Waiting(Long id, String memberName, LocalDate date, Time time, Theme theme) {
        validate(memberName, date, time, theme);
        this.id = id;
        this.memberName = memberName;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validate(String memberName, LocalDate date, Time time, Theme theme) {
        if (memberName == null || memberName.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수이며 비어있을 수 없습니다.");
        }
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜는 필수입니다.");
        }
        if (time == null) {
            throw new IllegalArgumentException("유효하지 않은 예약 시간대입니다.");
        }
        if (theme == null) {
            throw new IllegalArgumentException("유효하지 않은 테마입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getMemberName() {
        return memberName;
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

    public boolean isOwnedBy(String memberName) {
        return this.memberName.equals(memberName);
    }
}
