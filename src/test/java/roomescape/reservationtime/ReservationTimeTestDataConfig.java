package roomescape.reservationtime;

import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.fixture.ReservationTimeFixture;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@TestConfiguration
public class ReservationTimeTestDataConfig {

    public static final LocalTime TIME_FIELD = LocalTime.of(2, 40);
    @Autowired
    private ReservationTimeRepository repository;

    private Long savedId;
    private ReservationTime savedReservationTime;

    @PostConstruct
    public void setUpTestData() {
        ReservationTime reservationTime = ReservationTimeFixture.create(TIME_FIELD);
        savedReservationTime = repository.save(reservationTime);
        savedId = savedReservationTime.getId();
    }

    public Long getSavedId() {
        return savedId;
    }

    public ReservationTime getSavedReservationTime() {
        return savedReservationTime;
    }
}
