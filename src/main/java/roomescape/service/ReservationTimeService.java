package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.AvailableTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ReservationTimeNotFoundException;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponse> findAllTimes() {
        List<ReservationTime> reservationTimeDaoAll = reservationTimeRepository.findAll();

        return reservationTimeDaoAll.stream()
                .map(ReservationTimeResponse::toDto)
                .toList();
    }

    public ReservationTimeResponse createTime(ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = reservationTimeRequest.toTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return new ReservationTimeResponse(
                savedReservationTime.getId(),
                savedReservationTime.getStartAt()
        );
    }

    public void deleteTimeById(Long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new ReservationTimeNotFoundException();
        }
        if (reservationRepository.findByTimeId(id).size() > 0) {
            throw new ExistedReservationException();
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<AvailableTimeResponse> findTimesByDateAndThemeIdWithBooked(LocalDate date, Long themeId) {
        return reservationTimeRepository.findByDateAndThemeIdWithBooked(date, themeId);
    }
}
