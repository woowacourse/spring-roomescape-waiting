package roomescape.fixture;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;

import java.time.LocalDateTime;

public class ReservationWaitingFixture {

    private Long id = 1L;
    private String name = "브라운";
    private LocalDateTime createdAt = LocalDateTime.of(2026, 8, 1, 10, 0);
    private Reservation reservation = ReservationFixture.create();

    private ReservationWaitingFixture() {
    }

    public static ReservationWaitingFixture builder() {
        return new ReservationWaitingFixture();
    }

    public static ReservationWaiting create() {
        return builder().build();
    }

    public ReservationWaitingFixture id(Long id) {
        this.id = id;
        return this;
    }

    public ReservationWaitingFixture name(String name) {
        this.name = name;
        return this;
    }

    public ReservationWaiting build() {
        return new ReservationWaiting(
                id,
                name,
                createdAt,
                reservation
        );
    }
}
