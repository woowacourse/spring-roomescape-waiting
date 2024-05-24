package roomescape.service.reservation;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.global.handler.exception.CustomException;
import roomescape.global.handler.exception.ExceptionCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.reservation.dto.request.AvailableTimeRequest;
import roomescape.service.reservation.dto.request.ReservationTimeRequest;
import roomescape.service.reservation.dto.response.AvailableTimeResponse;
import roomescape.service.reservation.dto.response.ReservationTimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(ReservationRepository reservationRepository,
                                  ReservationTimeRepository reservationTimeRepository,
                                  ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationTimeResponse createReservationTime(ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = reservationTimeRequest.toEntity();

        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> findAvailableTimes(AvailableTimeRequest availableTimeRequest) {
        Theme findTheme = themeRepository.findById(availableTimeRequest.themeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        List<Reservation> reservations = reservationRepository.findAllByDateAndTheme(
                availableTimeRequest.date(),
                findTheme
        );

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(reservationTime -> AvailableTimeResponse.of(
                        reservationTime,
                        isReservedTime(reservations, reservationTime)
                ))
                .toList();
    }

    private boolean isReservedTime(List<Reservation> reservations, ReservationTime reservationTime) {
        return reservations.stream()
                .noneMatch(reservation -> reservation.isSameTime(reservationTime));
    }

    public void deleteReservationTime(Long id) {
        try {
            reservationTimeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ExceptionCode.TIME_IN_USE);
        }
    }
}
