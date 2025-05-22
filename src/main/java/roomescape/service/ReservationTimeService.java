package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationSlots;
import roomescape.dto.request.CreateReservationTimeRequest;
import roomescape.entity.ConfirmedReservation;
import roomescape.entity.ReservationTime;
import roomescape.exception.custom.InvalidReservationTimeException;
import roomescape.repository.ConfirmReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ConfirmReservationRepository confirmReservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(
            ConfirmReservationRepository confirmReservationRepository,
            ReservationTimeRepository reservationTimeRepository
    ) {
        this.confirmReservationRepository = confirmReservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTime addReservationTime(CreateReservationTimeRequest request) {
        ReservationTime reservationTime = request.toReservationTime();
        if (reservationTimeRepository.existsByStartAt(reservationTime.getStartAt())) {
            throw new InvalidReservationTimeException("중복된 예약시간입니다");
        }
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public void deleteReservationTime(Long id) {
        if (confirmReservationRepository.existsByTimeId(id)) {
            throw new InvalidReservationTimeException("예약이 되어있는 시간은 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    public ReservationSlots getReservationSlots(Long themeId, LocalDate date) {
        List<ReservationTime> times = reservationTimeRepository.findAll();

        List<ConfirmedReservation> alreadyReservedReservations = confirmReservationRepository.findAllByDateAndThemeId(date, themeId);

        return new ReservationSlots(times, alreadyReservedReservations);
    }
}
