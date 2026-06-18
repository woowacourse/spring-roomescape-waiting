package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import roomescape.domain.exception.InvalidDomainException;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "time_id", "theme_id", "order_index"}),
        @UniqueConstraint(columnNames = {"date", "time_id", "theme_id", "name"})
})
public class Waiting {
    private static final int MAX_NAME_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne(optional = false)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    protected Waiting() {
    }

    private Waiting(Long id, String name, LocalDate date,
                    ReservationTime time, Theme theme, int orderIndex) {
        validate(name, date, time, theme, orderIndex);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.orderIndex = orderIndex;
    }

    public static Waiting create(String name, LocalDate date,
                                 ReservationTime time, Theme theme, int order) {
        return new Waiting(null, name, date, time, theme, order);
    }

    public static Waiting withId(Long id, String name, LocalDate date,
                                 ReservationTime time, Theme theme, int order) {
        return new Waiting(id, name, date, time, theme, order);
    }

    public Waiting withOrderIndex(int newOrderIndex) {
        return new Waiting(this.id, this.name, this.date, this.time, this.theme, newOrderIndex);
    }

    public boolean isSameSlot(LocalDate date, Long timeId, Long themeId) {
        return this.date.equals(date)
                && this.time.getId().equals(timeId)
                && this.theme.getId().equals(themeId);
    }

    private static void validate(String name, LocalDate date,
                                 ReservationTime time, Theme theme, int order) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainException("대기자 이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidDomainException(
                    "대기자 이름은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다.");
        }
        if (date == null) {
            throw new InvalidDomainException("대기 날짜는 비어 있을 수 없습니다.");
        }
        if (time == null) {
            throw new InvalidDomainException("대기 시간은 비어 있을 수 없습니다.");
        }
        if (theme == null) {
            throw new InvalidDomainException("대기 테마는 비어 있을 수 없습니다.");
        }
        if (order < 1) {
            throw new InvalidDomainException("대기 순번은 1 이상이어야 합니다.");
        }
    }

    public boolean isOwnedBy(String name) {
        return this.name.equals(name);
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
