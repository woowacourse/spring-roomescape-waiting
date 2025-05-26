package roomescape.reservation.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.util.SystemLocalDateTime;
import roomescape.member.domain.Member;
import roomescape.reservation.application.exception.NotReservationOwnerException;
import roomescape.reservation.application.exception.UnexpectedReservationStatusException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.presentation.dto.AdminWaitingReservationResponse;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.WaitingReservationRequest;

@Service
public class WaitingReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public WaitingReservationService(ReservationRepository reservationRepository,
                                     ReservationService reservationService) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    public ReservationResponse createWaitingReservation(WaitingReservationRequest request, Member member) {

        ReservationTime time = reservationService.getReservationTime(request.timeId());
        Theme theme = reservationService.getTheme(request.themeId());

        reservationService.validateDuplicatedReservation(request.date(), time, theme, member);

        Reservation reservation = reservationRepository.save(new Reservation(request.date(),
                time, theme, member, ReservationStatus.WAITING, SystemLocalDateTime.now()));

        return reservationService.mapToReservationResponse(reservation);
    }

    @Transactional
    public void denyWaitingReservation(Long reservationId) {

        Reservation targetReservation = reservationService.getReservation(reservationId);

        validateStatusIsWaiting(targetReservation);

        reservationRepository.delete(targetReservation);
    }

    @Transactional
    public void deleteWaitingReservation(Long reservationId, Member member) {

        Reservation targetReservation = reservationService.getReservation(reservationId);

        validateReservationOwner(member, targetReservation);
        validateStatusIsWaiting(targetReservation);

        reservationRepository.delete(targetReservation);
    }

    public List<AdminWaitingReservationResponse> getWaitingReservations() {
        return AdminWaitingReservationResponse.from(reservationRepository.findAllByStatus(ReservationStatus.WAITING));
    }

    private void validateStatusIsWaiting(Reservation targetReservation) {
        if (!targetReservation.isWaiting()) {
            throw new UnexpectedReservationStatusException(ReservationStatus.WAITING);
        }
    }

    private void validateReservationOwner(Member member, Reservation targetReservation) {
        if (!targetReservation.getMember().equals(member)) {
            throw new NotReservationOwnerException("예약의 주인이 아닙니다.");
        }
    }
}
