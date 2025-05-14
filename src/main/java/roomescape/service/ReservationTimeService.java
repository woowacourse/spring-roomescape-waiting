package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.AvailableTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository, ThemeRepository themeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
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
        if (reservationRepository.findByReservationTimeId(id).size() > 0) {
            throw new ExistedReservationException();
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<AvailableTimeResponse> findTimesByDateAndThemeIdWithBooked(LocalDate date, Long themeId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow(ThemeNotFoundException::new);
        List<Reservation> reservations = reservationRepository.findByDateAndTheme(date, theme);

        List<ReservationTime> bookedReservationTimes = reservations.stream().map(Reservation::getReservationTime)
                .toList();
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(reservationTime -> new AvailableTimeResponse(reservationTime.getId(), reservationTime.getStartAt(),
                        bookedReservationTimes.contains(reservationTime)))
                .toList();
    }
}
