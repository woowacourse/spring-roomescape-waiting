package roomescape.service;

import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION;
import static roomescape.exception.ExceptionType.PERMISSION_DENIED;
import static roomescape.service.mapper.ReservationResponseMapper.toResponse;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.LoginMemberReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.service.finder.MemberFinder;
import roomescape.service.finder.ReservationFinder;
import roomescape.service.mapper.LoginMemberReservationResponseMapper;
import roomescape.service.mapper.ReservationResponseMapper;

@Service
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository waitingRepository;
    private final ReservationFinder reservationFinder;
    private final MemberFinder memberFinder;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationWaitingRepository waitingRepository,
                              ReservationFinder reservationFinder, MemberFinder memberFinder) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.reservationFinder = reservationFinder;
        this.memberFinder = memberFinder;
    }

    public ReservationResponse save(ReservationRequest reservationRequest) {
        Reservation beforeSave = reservationFinder.createWhenNotExists(reservationRequest);

        beforeSave.validatePastTimeReservation();

        Reservation saved = reservationRepository.save(beforeSave);
        return toResponse(saved);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponseMapper::toResponse)
                .toList();
    }

    public List<ReservationResponse> findByMemberAndThemeBetweenDates(long memberId, long themeId, LocalDate start,
                                                                      LocalDate end) {
        return reservationRepository.findByMemberAndThemeBetweenDates(memberId, themeId, start, end)
                .stream()
                .map(ReservationResponseMapper::toResponse)
                .toList();
    }

    public List<LoginMemberReservationResponse> findByMemberId(long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(LoginMemberReservationResponseMapper::toResponse)
                .toList();
    }

    public void cancel(long requestMemberId, long reservationId) {
        if (!canCancel(requestMemberId, reservationId)) {
            throw new RoomescapeException(PERMISSION_DENIED);
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION));

        waitingRepository.findTopWaitingByReservation(reservation)
                .ifPresentOrElse(waiting -> updateReservationAndDeleteTopWaiting(reservation, waiting),
                        () -> deleteReservation(reservationId));
    }

    private boolean canCancel(long requestMemberId, long reservationId) {
        Member requestMember = memberFinder.findById(requestMemberId);
        if (requestMember.isAdmin()) {
            return true;
        }
        return isMembersReservation(requestMemberId, reservationId);
    }

    private boolean isMembersReservation(long memberId, long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(Reservation::getReservationMember)
                .map(member -> member.hasIdOf(memberId))
                .orElse(false);
    }

    private void updateReservationAndDeleteTopWaiting(Reservation reservation, ReservationWaiting waiting) {
        Member waitingMember = waiting.getWaitingMember();
        reservation.updateReservationMember(waitingMember);
        waitingRepository.delete(waiting.getId());
    }

    private void deleteReservation(long reservationId) {
        reservationRepository.delete(reservationId);
    }
}
