package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;

@Service
public class ReservationDeleteService {
    private final ReservationRepository reservationRepository;

    public ReservationDeleteService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void delete(final Long id) {
        reservationRepository.findById(id)
                .filter(reservation -> !reservation.isWaiting())
                .ifPresent(reservation -> {
                    updateWaitReservation(reservation);
                    reservationRepository.deleteById(id);
                });
    }

    private void updateWaitReservation(final Reservation reservation) {
        final LocalDate date = reservation.getDate();
        final Long timeId = reservation.getTimeId();
        final Long themeId = reservation.getThemeId();
        reservationRepository.findFirstByDateAndTime_IdAndTheme_IdAndStatus(date, timeId, themeId, ReservationStatus.WAITING)
                .ifPresent(Reservation::changeToReserved);
    }

    public void deleteByIdAndOwner(final Long id, final Member member) {
        if (reservationRepository.existsByIdAndMember_Id(id, member.getId())) {
            deleteWaitingById(id);
        }
    }

    public void deleteWaitingById(final Long id) {
        reservationRepository.findById(id)
                .filter(Reservation::isWaiting)
                .ifPresent(reservation -> reservationRepository.deleteById(id));
    }
}
