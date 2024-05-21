package roomescape.service;

import static roomescape.exception.ExceptionType.DUPLICATE_WAITING;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;
import static roomescape.exception.ExceptionType.PERMISSION_DENIED;
import static roomescape.exception.ExceptionType.WAITING_WITHOUT_RESERVATION;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Role;
import roomescape.dto.LoginMemberReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.service.finder.MemberFinder;
import roomescape.service.finder.ReservationFinder;
import roomescape.service.mapper.LoginMemberReservationResponseMapper;
import roomescape.service.mapper.ReservationWaitingResponseMapper;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingRepository waitingRepository;
    private final ReservationFinder reservationFinder;
    private final MemberFinder memberFinder;

    public ReservationWaitingService(ReservationWaitingRepository waitingRepository,
                                     ReservationFinder reservationFinder,
                                     MemberFinder memberFinder) {
        this.waitingRepository = waitingRepository;
        this.reservationFinder = reservationFinder;
        this.memberFinder = memberFinder;
    }

    public ReservationWaitingResponse save(ReservationRequest reservationRequest) {
        Reservation reservation = reservationFinder.findByReservationRequest(reservationRequest,
                () -> new RoomescapeException(WAITING_WITHOUT_RESERVATION));
        Member waitingMember = memberFinder.findById(reservationRequest.memberId());

        validatePastTimeReservation(reservation);
        validateDuplicateWaiting(reservation, waitingMember);

        ReservationWaiting beforeSave = new ReservationWaiting(reservation, waitingMember);
        ReservationWaiting save = waitingRepository.save(beforeSave);

        return ReservationWaitingResponseMapper.toResponse(save, waitingRepository.findByReservation(reservation));
    }

    private void validatePastTimeReservation(Reservation beforeSave) {
        if (beforeSave.isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(PAST_TIME_RESERVATION);
        }
    }

    private void validateDuplicateWaiting(Reservation reservation, Member waitingMember) {
        boolean isDuplicate = waitingRepository.existsByReservationAndWaitingMember(reservation, waitingMember);
        if (isDuplicate) {
            throw new RoomescapeException(DUPLICATE_WAITING);
        }
    }

    public List<ReservationWaitingResponse> findAll() {
        return waitingRepository.findAll().stream()
                .map(ReservationWaitingResponseMapper::toResponseWithoutPriority)
                .toList();
    }

    public List<LoginMemberReservationResponse> findByMemberId(long memberId) {
        List<ReservationWaiting> allByMemberId = waitingRepository.findAllByMemberId(memberId);
        return allByMemberId
                .stream()
                .map(waiting -> ReservationWaitingResponseMapper.toResponse(waiting, allByMemberId))
                .map(LoginMemberReservationResponseMapper::from)
                .toList();
    }

    public void delete(long memberId, long waitingId) {
        if (canDelete(memberId, waitingId)) {
            waitingRepository.delete(waitingId);
        } else {
            throw new RoomescapeException(PERMISSION_DENIED);
        }
    }

    private boolean canDelete(long memberId, long waitingId) {
        Role role = memberFinder.findById(memberId).getRole();
        return Role.ADMIN.equals(role) || waitingRepository.findAllByMemberId(memberId).stream()
                .map(ReservationWaiting::getId)
                .anyMatch(id -> id == waitingId);
    }
}
