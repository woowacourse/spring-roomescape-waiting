package roomescape.application.reservation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationStatusResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.role.RoleRepository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.exception.UnAuthorizedException;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    public WaitingService(ReservationRepository reservationRepository,
                          WaitingRepository waitingRepository,
                          MemberRepository memberRepository,
                          RoleRepository roleRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest reservationRequest) {
        Reservation reservation = reservationRepository.getByDateAndTimeIdAndThemeId(reservationRequest.date(),
                reservationRequest.timeId(), reservationRequest.themeId());
        Member member = memberRepository.getById(reservationRequest.memberId());
        validateCreateWaiting(reservationRequest, reservation);
        Waiting waiting = waitingRepository.save(new Waiting(reservation, member));
        reservation.validateCreatedAtAfterReserveTime(waiting.getCreatedAt());
        return ReservationResponse.from(waiting);
    }

    @Transactional
    public List<ReservationResponse> findAll() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public List<ReservationStatusResponse> findAllByMemberId(long memberId) {
        List<Waiting> waitings = waitingRepository.findAllByMemberIdOrderByCreatedAtAsc(memberId);
        return waitings.stream()
                .map(this::makeReservationStatus)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteByIdWhenAuthorization(long memberId, long id) {
        Waiting waiting = waitingRepository.getById(id);
        if (roleRepository.isAdminByMemberId(memberId) || waiting.isOwnedBy(memberId)) {
            deleteById(id);
            return;
        }
        throw new UnAuthorizedException();
    }

    @Transactional
    public void deleteById(Long id) {
        waitingRepository.deleteById(id);
    }

    private void validateCreateWaiting(ReservationRequest reservationRequest, Reservation reservation) {
        validateReservationDuplicate(reservationRequest, reservation);
        validateWaitingDuplicate(reservationRequest, reservation);
    }

    private void validateReservationDuplicate(ReservationRequest reservationRequest, Reservation reservation) {
        if (reservation.isOwnedBy(reservationRequest.memberId())) {
            throw new IllegalArgumentException("동일한 예약은 생성이 불가합니다.");
        }
    }

    private void validateWaitingDuplicate(ReservationRequest reservationRequest, Reservation reservation) {
        if (waitingRepository.existsByReservationIdAndMemberId(reservation.getId(), reservationRequest.memberId())) {
            throw new IllegalArgumentException("동일한 예약 대기는 생성이 불가합니다.");
        }
    }

    private ReservationStatusResponse makeReservationStatus(Waiting waiting) {
        if (waiting.isWaiting()) {
            List<Waiting> sameReservationWaitings =
                    waitingRepository.findByReservationIdOrderByCreatedAtAsc(waiting.getReservation().getId());
            return ReservationStatusResponse.of(waiting, sameReservationWaitings.indexOf(waiting) + 1);
        }
        return ReservationStatusResponse.from(waiting);
    }

    public Optional<Waiting> findFirstByReservationId(long id) {
        return waitingRepository.findFirstByReservationIdOrderByCreatedAtAsc(id);
    }
}
