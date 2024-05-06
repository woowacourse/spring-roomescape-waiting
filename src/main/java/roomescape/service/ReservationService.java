package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.controller.reservation.dto.ReservationRequest;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.exception.DuplicateReservationException;
import roomescape.service.exception.PreviousTimeException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> getReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse addReservation(final ReservationRequest reservationRequest) {
        final ReservationTime time = findTime(reservationRequest);
        final Theme theme = findTheme(reservationRequest);

        final Reservation parsedReservation = reservationRequest.toDomain(time, theme);
        validateDuplicate(theme, time, parsedReservation);
        final LocalDateTime reservationDateTime = parsedReservation.getDate().atTime(time.getStartAt());
        validateBeforeDay(reservationDateTime);

        final Reservation savedReservation = reservationRepository.save(parsedReservation);
        return ReservationResponse.from(savedReservation);
    }

    public void deleteReservation(final Long id) {
        final Reservation fetchReservation = reservationRepository.fetchById(id);
        reservationRepository.deleteById(fetchReservation.getId());
    }

    private void validateBeforeDay(final LocalDateTime reservationDateTime) {
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new PreviousTimeException("지난 시간으로 예약할 수 없습니다.");
        }
    }

    private void validateDuplicate(final Theme theme, final ReservationTime time, final Reservation parsedReservation) {
        final boolean isExistsReservation =
                reservationRepository.existsByThemesAndDateAndTimeId(theme.getId(), time.getId(),
                        parsedReservation.getDate());
        if (isExistsReservation) {
            throw new DuplicateReservationException("중복된 시간으로 예약이 불가합니다.");
        }
    }

    private Theme findTheme(final ReservationRequest reservationRequest) {
        return themeRepository.fetchById(reservationRequest.themeId());
    }

    private ReservationTime findTime(final ReservationRequest reservationRequest) {
        return reservationTimeRepository.fetchById(reservationRequest.timeId());
    }
}
