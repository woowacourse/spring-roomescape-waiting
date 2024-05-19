package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exceptions.DuplicationException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.ReservationTimeRequest;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(
            ReservationTimeRepository reservationTimeRepository,
            ReservationRepository ReservationRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = ReservationRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationTimeResponse addTime(ReservationTimeRequest reservationTimeRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeRequest.startAt())) {
            throw new DuplicationException("이미 존재하는 시간입니다.");
        }
        ReservationTime reservationTime = reservationTimeRepository.save(reservationTimeRequest.toReservationTime());
        return new ReservationTimeResponse(reservationTime);
    }

    public ReservationTimeResponse getReservationTimeById(Long id) {
        return new ReservationTimeResponse(reservationTimeRepository.getById(id));
    }

    public List<ReservationTimeResponse> findReservationTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public List<ReservationTimeResponse> findTimesWithAlreadyBooked(LocalDate date, Long themeId) {
        Theme theme = themeRepository.getById(themeId);
        List<Long> alreadyBookedTimeIds = reservationRepository.findByDateAndTheme(date, theme)
                .stream()
                .map(reservation -> reservation.getReservationTime().getId())
                .toList();

        return reservationTimeRepository.findAll()
                .stream()
                .map(reservationTime -> new ReservationTimeResponse(
                        reservationTime,
                        reservationTime.isBelongTo(alreadyBookedTimeIds)
                ))
                .toList();
    }

    public void deleteTime(Long id) {
        reservationTimeRepository.deleteById(id);
    }
}
