package roomescape.fixture.db;

import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@RequiredArgsConstructor
@Component
public class ReservationDateTimeDbFixture {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationDateTime 내일_열시() {
        ReservationTime time = ReservationTime.open(LocalTime.of(10, 0));
        reservationTimeRepository.save(time);
        return ReservationDateTime.create(
                ReservationDateFixture.예약날짜_내일,
                time
        );
    }

    public ReservationDateTime 내일_열한시() {
        ReservationTime time = ReservationTime.open(LocalTime.of(11, 0));
        reservationTimeRepository.save(time);
        return ReservationDateTime.create(
                ReservationDateFixture.예약날짜_내일,
                time
        );
    }

    public ReservationDateTime _7일전_열시() {
        ReservationTime time = ReservationTime.open(LocalTime.of(10, 0));
        reservationTimeRepository.save(time);
        return new ReservationDateTime(
                ReservationDateFixture.예약날짜_7일전,
                time
        );
    }
}
