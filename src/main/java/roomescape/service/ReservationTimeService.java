package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
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
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public ReservationTimeResponse saveReservationTime(ReservationTimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new DuplicatedTimeException();
        }
        ReservationTime reservationTime = request.toReservationTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    public List<ReservationTimeResponse> findAllReservationTime() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAllAvailableReservationTime(LocalDate date, Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(NotFoundTimeException::new);

        List<Long> unavailableTimeIds = reservationRepository.findByDateAndTheme(date, theme)
                .stream()
                .map(reservation -> reservation.getTime().getId())
                .toList();

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
