package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.AvailableReservationTimeResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.exception.OperationNotAllowedException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(
            ReservationTimeRepository reservationTimeRepository,
            ReservationRepository reservationRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponse> findReservationTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse createReservationTime(ReservationTimeRequest request) {
        ReservationTime reservationTime = request.toReservationTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(savedReservationTime);
    }

    public void deleteReservationTimeById(Long id) {
        boolean exist = reservationRepository.existsByReservationTimeId(id);
        if (exist) {
            throw new OperationNotAllowedException("해당 시간에 예약이 존재하기 때문에 삭제할 수 없습니다.");
        }
        reservationTimeRepository.delete(getReservationTime(id));
    }

    public List<AvailableReservationTimeResponse> findReservationTimesWithBookedStatus(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<ReservationTime> reservedTimes = reservationRepository.findAllByDateAndThemeId(date, themeId).stream()
                .map(Reservation::getReservationTime)
                .toList();

        return reservationTimes.stream()
                .map(reservationTime -> AvailableReservationTimeResponse.from(
                        reservationTime,
                        reservedTimes.contains(reservationTime)
                ))
                .toList();
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 예약 시간을 찾을 수 없습니다."));
    }
}
