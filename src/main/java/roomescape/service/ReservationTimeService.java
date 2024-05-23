package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservationdetail.ReservationTime;
import roomescape.domain.reservationdetail.ReservationTimeRepository;
import roomescape.exception.time.DuplicatedTimeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.exception.time.ReservationReferencedTimeException;
import roomescape.service.dto.request.time.ReservationTimeRequest;
import roomescape.service.dto.response.time.AvailableReservationTimeResponse;
import roomescape.service.dto.response.time.ReservationTimeResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;

    public List<ReservationTimeResponse> findAllReservationTime() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAllAvailableReservationTime(LocalDate date, Long themeId) {
        List<ReservationTime> totalTimes = reservationTimeRepository.findAll();
        List<ReservationTime> reservedTimes = reservationTimeRepository.findAllReservedTimeByDateAndThemeId(
                date, themeId);

        return totalTimes.stream()
                .map(time -> AvailableReservationTimeResponse.of(time, reservedTimes))
                .toList();
    }

    @Transactional
    public ReservationTimeResponse saveReservationTime(ReservationTimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new DuplicatedTimeException();
        }
        ReservationTime reservationTime = request.toReservationTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    @Transactional
    public void deleteReservationTime(Long id) {
        ReservationTime reservationTime = findReservationTimeById(id);
        try {
            reservationTimeRepository.delete(reservationTime);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationReferencedTimeException();
        }
    }

    private ReservationTime findReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NotFoundTimeException::new);
    }
}
