package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
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
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
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
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(reservationRequest.date(),
                reservationRequest.timeId(), reservationRequest.themeId());
        Member member = memberRepository.getById(reservationRequest.memberId());
        validateCreateWaiting(reservationRequest, reservation);
        Waiting waiting = new Waiting(reservation, member, LocalDateTime.now(clock));
        waitingRepository.save(waiting);
        return ReservationResponse.from(waiting);

    }

    private void validateCreateWaiting(ReservationRequest reservationRequest, Reservation reservation) {
        validateWaitingAfterPresent(reservationRequest);
        validateReservationExist(reservation);
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

    private void validateReservationExist(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("대기를 하지 않고 예약이 가능합니다.");
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

    @Transactional
    public void deleteById(long memberId, long id) {
        Waiting waiting = waitingRepository.getById(id);
        if (roleRepository.isAdminByMemberId(memberId) || waiting.isOwnedBy(memberId)) {
            waitingRepository.deleteById(waiting.getId());
            return;
        }
        throw new UnAuthorizedException();
    }

    public List<ReservationStatusResponse> findAllByMemberId(long memberId) {
        List<Waiting> waitings = waitingRepository.findAllByMemberIdOrderByCreatedAtAsc(memberId);
        return waitings.stream()
                .map(this::makeReservationStatus)
                .collect(Collectors.toList());

    }

    private ReservationStatusResponse makeReservationStatus(Waiting waiting) {
        if (waiting.getWaitingStatus().isWaiting()) {
            List<Waiting> sameReservationWaitings =
                    waitingRepository.findByReservationIdOrderByCreatedAtAsc(waiting.getReservation().getId());
            return ReservationStatusResponse.of(waiting, sameReservationWaitings.indexOf(waiting) + 1);
        }
        return ReservationStatusResponse.from(waiting);
    }
}
