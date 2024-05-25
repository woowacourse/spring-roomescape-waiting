package roomescape.service;

import static roomescape.exception.ExceptionType.DUPLICATE_WAITING;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.PERMISSION_DENIED;
import static roomescape.exception.ExceptionType.WAITING_WITHOUT_RESERVATION;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.LoginMemberReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.service.finder.ReservationFinder;
import roomescape.service.mapper.LoginMemberReservationResponseMapper;
import roomescape.service.mapper.ReservationWaitingResponseMapper;

@Service
@Transactional
public class ReservationWaitingService {
    private final ReservationWaitingRepository waitingRepository;
    private final ReservationFinder reservationFinder;
    private final MemberRepository memberRepository;

    public ReservationWaitingService(ReservationWaitingRepository waitingRepository,
                                     ReservationFinder reservationFinder,
                                     MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationFinder = reservationFinder;
        this.memberRepository = memberRepository;
    }

    public ReservationWaitingResponse save(ReservationRequest reservationRequest) {
        Reservation reservation = reservationFinder.findByReservationRequest(reservationRequest,
                () -> new RoomescapeException(WAITING_WITHOUT_RESERVATION));
        Member waitingMember = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));

        reservation.validatePastTimeReservation();
        validateDuplicateWaiting(reservation, waitingMember);

        ReservationWaiting beforeSave = new ReservationWaiting(reservation, waitingMember);
        ReservationWaiting saved = waitingRepository.save(beforeSave);

        int priority = saved.calculatePriority(waitingRepository.findByReservation(reservation));
        return ReservationWaitingResponseMapper.toResponse(saved, priority);
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
        return allByMemberId.stream()
                .map(waiting -> {
                    int priority = waiting.calculatePriority(allByMemberId);
                    return ReservationWaitingResponseMapper.toResponse(waiting, priority);
                })
                .map(LoginMemberReservationResponseMapper::from)
                .toList();
    }

    public void delete(long memberId, long waitingId) {
        if (!canDelete(memberId, waitingId)) {
            throw new RoomescapeException(PERMISSION_DENIED);
        }
        waitingRepository.delete(waitingId);
    }

    private boolean canDelete(long memberId, long waitingId) {
        return isAdmin(memberId) || isMembersWaiting(memberId, waitingId);
    }

    private boolean isAdmin(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER))
                .isAdmin();
    }

    private boolean isMembersWaiting(long memberId, long waitingId) {
        return waitingRepository.findAllByMemberId(memberId).stream()
                .map(ReservationWaiting::getId)
                .anyMatch(id -> id == waitingId);
    }
}
