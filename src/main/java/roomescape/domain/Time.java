package roomescape.domain;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "reservation_time")
public class Time {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    protected Time(){}

    public Time(LocalTime startAt) {
        this.startAt = startAt;
    }

    // JDBC → JPA 과정에서 깨지는 것을 방지하기 위한 코드로 곧 삭제 예정
    public Time(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}

