package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.business.ReservationTimeCreationContent;
import roomescape.dto.business.ReservationTimeWithBookState;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.exception.local.AlreadyReservedTimeException;
import roomescape.exception.local.DuplicateReservationException;
import roomescape.exception.local.NotFoundReservationTimeException;
import roomescape.exception.local.NotFoundThemeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(
            ReservationTimeRepository timeRepository,
            ReservationRepository reservationRepository,
            ThemeRepository themeRepository
    ) {
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        List<ReservationTime> reservationTimes = timeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public List<ReservationTimeWithBookState> findReservationTimesWithBookState(long themeId, LocalDate date) {
        Theme theme = loadThemeById(themeId);
        return timeRepository.findReservationTimesWithBookState(theme, date);
    }

    public ReservationTimeResponse addReservationTime(ReservationTimeCreationContent request) {
        validateDuplicateTime(request.startAt());
        ReservationTime reservationTime = ReservationTime.createWithoutId(request.startAt());
        ReservationTime savedReservationTime = timeRepository.save(reservationTime);
        return new ReservationTimeResponse(savedReservationTime);
    }

    public void deleteReservationTimeById(Long id) {
        ReservationTime reservationTime = loadReservationTimeById(id);
        validateReservationInTime(reservationTime);
        timeRepository.deleteById(id);
    }

    private void validateDuplicateTime(LocalTime startAt) {
        boolean alreadyExistTime = timeRepository.existsByStartAt(startAt);
        if (alreadyExistTime) {
            throw new DuplicateReservationException();
        }
    }

    private void validateReservationInTime(ReservationTime reservationTime) {
        if (reservationRepository.existsByReservationTime(reservationTime)) {
            throw new AlreadyReservedTimeException();
        }
    }

    private ReservationTime loadReservationTimeById(Long reservationTimeId) {
        return timeRepository.findById(reservationTimeId)
                .orElseThrow(NotFoundReservationTimeException::new);
    }

    private Theme loadThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(NotFoundThemeException::new);
    }
}
