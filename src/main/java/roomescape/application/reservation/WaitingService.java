package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
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
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.role.RoleRepository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.exception.UnAuthorizedException;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final RoleRepository roleRepository;
    private final Clock clock;

    public WaitingService(ReservationRepository reservationRepository, WaitingRepository waitingRepository,
                          MemberRepository memberRepository, ReservationTimeRepository reservationTimeRepository,
                          RoleRepository roleRepository, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.roleRepository = roleRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest reservationRequest) {
        Reservation reservation = reservationRepository.getByDateAndTimeIdAndThemeId(reservationRequest.date(),
                reservationRequest.timeId(), reservationRequest.themeId());
        Member member = memberRepository.getById(reservationRequest.memberId());
        validateCreateWaiting(reservationRequest, reservation);
        Waiting waiting = new Waiting(reservation, member, LocalDateTime.now(clock));
        waitingRepository.save(waiting);
        return ReservationResponse.from(waiting);
    }

    public List<ReservationResponse> findAll() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(ReservationResponse::from)
                .toList();
    }

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
        validateWaitingAfterPresent(reservationRequest);
        validateReservationDuplicate(reservationRequest, reservation);
        validateWaitingDuplicate(reservationRequest, reservation);
    }

    private void validateWaitingAfterPresent(ReservationRequest reservationRequest) {
        ReservationTime time = reservationTimeRepository.getById(reservationRequest.timeId());
        LocalDateTime reservedDateTime = LocalDateTime.of(reservationRequest.date(), time.getStartAt());
        if (reservedDateTime.isBefore(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("현재 시간보다 과거로 예약할 수 없습니다.");
        }
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
