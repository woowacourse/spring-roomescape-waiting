package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "waiting",
        indexes = {@Index(name = "idx_waiting_created_at", columnList = "createdAt")}
)
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    private LocalDateTime createdAt;

    public static Waiting from(LocalDate date, Long memberId, Theme theme, ReservationTime time) {
        return new Waiting(null, date, memberId, theme, time, LocalDateTime.now());
    }
}
