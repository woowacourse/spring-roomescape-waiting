package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.TimeWithBookedResponse;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;

@Service
@Transactional
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationTimeService(final ReservationTimeRepository reservationTimeRepository,
                                  final ReservationRepository reservationRepository,
                                  final ThemeRepository themeRepository,
                                  final WaitingRepository waitingRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAllTimes() {
        List<ReservationTime> reservationTimeDaoAll = reservationTimeRepository.findAll();

        return reservationTimeDaoAll.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeWithBookedResponse> findTimesByDateAndThemeIdWithBooked(LocalDate date, Long themeId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow(ThemeNotFoundException::new);
        List<Reservation> reservations = reservationRepository.findByDateAndTheme(date, theme);

        List<ReservationTime> bookedReservationTimes = reservations.stream()
                .map(Reservation::getReservationTime)
                .toList();
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(time -> TimeWithBookedResponse.of(time, bookedReservationTimes.contains(time)))
                .toList();
    }

    public ReservationTimeResponse createTime(ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = reservationTimeRequest.toTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    public void deleteTimeById(Long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new ReservationTimeNotFoundException();
        }
        if (existReservation(id) || existWaiting(id)) {
            throw new ExistedReservationException();
        }
        reservationTimeRepository.deleteById(id);
    }

    private boolean existReservation(Long id) {
        return !reservationRepository.findByReservationTimeId(id).isEmpty();
    }

    private boolean existWaiting(Long id) {
        return !waitingRepository.findByReservationTimeId(id).isEmpty();
    }
}
