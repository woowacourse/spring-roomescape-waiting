package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.time.AvailableReservationTime;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationScheduleRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.request.AvailableReservationTimeRequest;
import roomescape.service.request.CreateReservationTimeRequest;
import roomescape.service.response.AvailableReservationTimeResponse;
import roomescape.service.response.ReservationTimeResponse;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationScheduleRepository reservationScheduleRepository;

    public ReservationTimeService(
            final ReservationTimeRepository reservationTimeRepository,
            final ReservationRepository reservationRepository,
            final ReservationScheduleRepository reservationScheduleRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationScheduleRepository = reservationScheduleRepository;
    }

    public ReservationTimeResponse createReservationTime(final CreateReservationTimeRequest request) {
        LocalTime startAt = request.startAt();
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new IllegalArgumentException("이미 존재하는 시간입니다.");
        }
        ReservationTime created = reservationTimeRepository.save(new ReservationTime(null, startAt));
        return ReservationTimeResponse.from(created);
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return ReservationTimeResponse.from(reservationTimes);
    }

    public void deleteReservationTimeById(final Long id) {
        Optional<ReservationSchedule> schedule = reservationScheduleRepository.findByReservationTime_Id(id);
        if (schedule.isPresent() && reservationRepository.existsByScheduleId(schedule.get().getId())) {
            throw new IllegalArgumentException("해당 시간에 이미 예약이 존재하여 삭제할 수 없습니다.");
        }
        ReservationTime reservationTime = getReservationTime(id);
        reservationTimeRepository.deleteById(reservationTime.getId());
    }

    public ReservationTime getReservationTime(final Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("예약 시간을 찾을 수 없습니다."));
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(
            final AvailableReservationTimeRequest request
    ) {
        List<AvailableReservationTime> allAvailableReservationTimes = reservationScheduleRepository.findAllAvailableReservationSchedules(
                request.date(),
                request.themeId()
        );
        return AvailableReservationTimeResponse.from(allAvailableReservationTimes);
    }
}
