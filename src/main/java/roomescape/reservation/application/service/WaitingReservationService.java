package roomescape.reservation.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.util.SystemLocalDateTime;
import roomescape.member.domain.Member;
import roomescape.reservation.application.exception.NotReservationOwnerException;
import roomescape.reservation.application.exception.ReservationNotFoundException;
import roomescape.reservation.application.exception.ReservationTimeNotFoundException;
import roomescape.reservation.application.exception.ThemeNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.presentation.dto.AdminWaitingReservationResponse;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.WaitingReservationRequest;

@Service
public class WaitingReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public WaitingReservationService(ReservationRepository reservationRepository,
                                     ReservationTimeRepository timeRepository,
                                     ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationResponse createWaitingReservation(WaitingReservationRequest request, Member member) {

        ReservationTime time = getReservationTime(request.timeId());
        Theme theme = getTheme(request.themeId());

        Reservation reservation = reservationRepository.save(new Reservation(request.date(),
                time, theme, member, ReservationStatus.WAITING, SystemLocalDateTime.now()));

        return mapToReservationResponse(reservation);
    }

    @Transactional
    public void denyWaitingReservation(Long id) {
        Reservation targetReservation = getReservation(id);

        reservationRepository.delete(targetReservation);
    }

    @Transactional
    public void deleteWaitingReservation(Long reservationId, Member member) {

        Reservation targetReservation = getReservation(reservationId);

        if (!targetReservation.getMember().equals(member)) {
            throw new NotReservationOwnerException("예약의 주인이 아닙니다.");
        }

        reservationRepository.delete(targetReservation);
    }

    public List<AdminWaitingReservationResponse> getWaitingReservations() {
        return AdminWaitingReservationResponse.from(reservationRepository.findAllByStatus(ReservationStatus.WAITING));
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    private ReservationTime getReservationTime(Long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException(timeId));
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException(themeId));
    }

    private ReservationResponse mapToReservationResponse(Reservation reservation) {
        if (reservation.getStatus().isWaiting()) {
            int order = calculateWaitingOrder(reservation);
            return ReservationResponse.fromWaitingReservation(reservation, order);
        }
        return ReservationResponse.fromConfirmedReservation(reservation);
    }

    private int calculateWaitingOrder(Reservation reservation) {
        return reservationRepository.countReservationsBefore(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getCreatedAt()
        );
    }
}
