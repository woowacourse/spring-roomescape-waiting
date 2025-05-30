package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.request.ReservationSearchServiceRequest;
import roomescape.reservation.application.dto.response.ReservationServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public List<ReservationServiceResponse> getAll() {
        List<Reservation> reservation = reservationRepository.getAll();
        return reservation.stream()
            .map(ReservationServiceResponse::from)
            .toList();
    }

    public List<ReservationServiceResponse> getSearchedAll(
        ReservationSearchServiceRequest request) {
        List<Reservation> reservations = reservationRepository.getSearchReservations(
            request.themeId(),
            request.memberId(),
            request.dateFrom(),
            request.dateTo()
        );
        return reservations.stream()
            .map(ReservationServiceResponse::from)
            .toList();
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = reservationRepository.getById(id);
        reservationRepository.remove(reservation);
        promoteWaiting(
            reservation.getTheme(),
            reservation.getDate(),
            reservation.getTime()
        ).ifPresent(reservationRepository::save);
    }

    public Optional<Reservation> promoteWaiting(ReservationTheme theme, LocalDate date,
        ReservationTime time) {
        Optional<Waiting> waiting = waitingRepository
            .findAllByThemeAndDateAndTime(theme, date, time)
            .stream()
            .min(Comparator.comparing(Waiting::getId));

        if (waiting.isEmpty()) {
            return Optional.empty();
        }

        Waiting waitingToPromote = waiting.get();

        Reservation promotedReservation = Reservation.builder()
            .date(waitingToPromote.getDate())
            .time(waitingToPromote.getTime())
            .theme(waitingToPromote.getTheme())
            .member(waitingToPromote.getMember())
            .build();

        waitingRepository.delete(waitingToPromote);

        return Optional.of(promotedReservation);
    }
}
