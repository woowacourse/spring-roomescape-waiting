package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.dto.reservation.MyReservationResponse;
import roomescape.dto.reservation.ReservationFilterParam;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse create(final Reservation reservation) {
        validateDate(reservation.getDate());

        final boolean isReserved = reservationRepository.existsByDateAndTime_IdAndTheme_Id(
                reservation.getDate(), reservation.getReservationTimeId(), reservation.getThemeId());
        if (isReserved) {
            throw new IllegalArgumentException("해당 시간대에 예약이 모두 찼습니다.");
        }

        final Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    private void validateDate(final LocalDate date) {
        if (LocalDate.now().isAfter(date) || LocalDate.now().equals(date)) {
            throw new IllegalArgumentException("이전 날짜 혹은 당일은 예약할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        final List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllBy(final ReservationFilterParam filterParam) {
        final List<Reservation> reservations = reservationRepository.findByTheme_IdAndMember_IdAndDateBetween(
                filterParam.themeId(), filterParam.memberId(), filterParam.dateFrom(), filterParam.dateTo()
        );
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void delete(final Long id) {
        final Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 예약이 없습니다."));
        final Optional<Waiting> optionalWaiting = waitingRepository.findTopByDateAndTime_IdAndTheme_IdOrderById(
                reservation.getDate(), reservation.getReservationTimeId(), reservation.getThemeId());
        if (optionalWaiting.isPresent()) {
            final Waiting waiting = optionalWaiting.get();
            waitingRepository.deleteById(waiting.getId());
            reservationRepository.save(waiting.toReservation());
        }
        reservationRepository.deleteById(id);
    }

    public List<MyReservationResponse> findMyReservations(final Long id) {
        final List<Reservation> reservations = reservationRepository.findByMember_Id(id);
        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
