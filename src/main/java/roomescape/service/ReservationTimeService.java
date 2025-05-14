package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.request.AvailableTimeRequest;
import roomescape.dto.request.CreateReservationTimeRequest;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationSlots;
import roomescape.entity.ReservationTime;
import roomescape.exception.custom.InvalidReservationTimeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationRepository reservationRepository,
                                  ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTime addReservationTime(CreateReservationTimeRequest request) {
        ReservationTime reservationTime = request.toReservationTime();
        if (reservationTimeRepository.existsByTime(reservationTime.getTime())) {
            throw new InvalidReservationTimeException("중복된 예약시간입니다");
        }
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public void deleteReservationTime(Long id) {
        if (reservationRepository.existsByTime_Id(id)) {
            throw new InvalidReservationTimeException("예약이 되어있는 시간은 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    public ReservationSlots getReservationSlots(AvailableTimeRequest request) {
        List<ReservationTime> times = reservationTimeRepository.findAll();

        List<Reservation> alreadyReservedReservations = reservationRepository.findAllByDateAndTheme_Id(request.date(),
                request.themeId());

        return new ReservationSlots(times, alreadyReservedReservations);
    }
}
