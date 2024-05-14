package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.exception.time.DuplicatedTimeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.exception.time.ReservationReferencedTimeException;
import roomescape.web.dto.request.time.ReservationTimeRequest;
import roomescape.web.dto.response.time.AvailableReservationTimeResponse;
import roomescape.web.dto.response.time.ReservationTimeResponse;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponse> findAllReservationTime() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAllAvailableReservationTime(LocalDate date, Long themeId) {
        List<Long> unavailableTimeIds = reservationRepository.findTimeIdByDateAndThemeId(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(time -> toAvailableReservationTimeResponse(time, unavailableTimeIds))
                .toList();
    }

    private AvailableReservationTimeResponse toAvailableReservationTimeResponse(
            ReservationTime time, List<Long> unavailableTimeIds) {
        boolean alreadyBooked = isAlreadyBooked(time.getId(), unavailableTimeIds);
        return AvailableReservationTimeResponse.of(time, alreadyBooked);
    }

    private boolean isAlreadyBooked(Long targetTimeId, List<Long> unavailableTimeIds) {
        return unavailableTimeIds.contains(targetTimeId);
    }

    public ReservationTimeResponse saveReservationTime(ReservationTimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new DuplicatedTimeException();
        }
        ReservationTime reservationTime = request.toReservationTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

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
