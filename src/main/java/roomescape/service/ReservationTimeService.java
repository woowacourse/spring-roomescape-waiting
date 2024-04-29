package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.dto.AvailableReservationTimeResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.service.exception.OperationNotAllowedException;
import roomescape.service.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponse> getAllReservationTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse addReservationTime(ReservationTimeRequest request) {
        ReservationTime reservationTime = request.toReservationTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(savedReservationTime);
    }

    public void deleteReservationTimeById(Long id) {
        findValidatedReservationTime(id);
        boolean exist = reservationRepository.existByReservationTimeId(id);
        if (exist) {
            throw new OperationNotAllowedException("해당 시간에 예약이 존재하기 때문에 삭제할 수 없습니다.");
        }

        reservationTimeRepository.deleteById(id);
    }

    public List<AvailableReservationTimeResponse> getReservationTimeBookedStatus(LocalDate date, Long themeId) {
        List<ReservationTime> bookedTimes = reservationTimeRepository.findByReservationDateAndThemeId(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(reservationTime -> AvailableReservationTimeResponse.from(
                        reservationTime,
                        bookedTimes.contains(reservationTime)
                ))
                .toList();
    }

    private ReservationTime findValidatedReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 예약 시간을 찾을 수 없습니다."));
    }
}
