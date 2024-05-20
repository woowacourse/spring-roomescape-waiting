package roomescape.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.dto.ReservationReadOnly;
import roomescape.domain.reservation.dto.WaitingWithRank;
import roomescape.domain.reservation.slot.ReservationSlot;
import roomescape.exception.RoomEscapeBusinessException;
import roomescape.service.dto.ReservationBookedResponse;
import roomescape.service.dto.ReservationConditionRequest;
import roomescape.service.dto.ReservationResponse;
import roomescape.service.dto.ReservationSaveRequest;
import roomescape.service.dto.UserReservationResponse;
import roomescape.service.dto.WaitingResponse;

@Service
public class ReservationService {

    private final ReservationSlotService reservationSlotService;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationSlotService reservationSlotService,
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository,
            MemberRepository memberRepository
    ) {
        this.reservationSlotService = reservationSlotService;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationResponse saveReservation(ReservationSaveRequest reservationSaveRequest) {
        Member member = findMemberById(reservationSaveRequest.memberId());
        ReservationSlot slot = reservationSlotService.findSlot(reservationSaveRequest.toSlotRequest());

        Optional<Reservation> reservation = reservationRepository.findBySlot(slot);
        if (reservation.isPresent()) {
            Reservation foundReservation = reservation.get();

            validateDuplicateReservation(member, foundReservation);

            Waiting waiting = new Waiting(member, foundReservation);
            waitingRepository.save(waiting);
            return ReservationResponse.createByWaiting(waiting);
        }

        Reservation newReservation = new Reservation(member, slot);
        reservationRepository.save(newReservation);
        return ReservationResponse.createByReservation(newReservation);
    }

    private void validateDuplicateReservation(Member member, Reservation reservation) {
        boolean isWaitingExist = waitingRepository.existsByReservationAndMember(reservation, member);

        if (reservation.isMember(member) || isWaitingExist) {
            throw new RoomEscapeBusinessException("중복된 예약을 할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationBookedResponse> findReservationsByCondition(
            ReservationConditionRequest reservationConditionRequest) {
        List<ReservationReadOnly> reservations = reservationRepository.findByConditions(
                reservationConditionRequest.dateFrom(),
                reservationConditionRequest.dateTo(),
                reservationConditionRequest.themeId(),
                reservationConditionRequest.memberId()
        );

        return reservations.stream()
                .map(ReservationBookedResponse::from)
                .sorted(Comparator.comparing(ReservationBookedResponse::dateTime))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserReservationResponse> findAllReservationAndWaiting(Long memberId, LocalDate date) {
        Member member = findMemberById(memberId);
        List<Reservation> reservations = reservationRepository.findByMemberAndSlot_DateGreaterThanEqual(member, date);

        List<WaitingWithRank> waitings = waitingRepository.findWaitingRankByMemberAndDateAfter(member, date);

        return Stream.concat(
                        UserReservationResponse.reservationsToResponseStream(reservations),
                        UserReservationResponse.waitingsToResponseStream(waitings)
                )
                .sorted(Comparator.comparing(UserReservationResponse::dateTime))
                .toList();
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약입니다."));

        Optional<Waiting> firstWaiting = waitingRepository.findFirstByReservation(reservation, Sort.by("id"));

        if (firstWaiting.isPresent()) {
            Waiting waiting = firstWaiting.get();
            reservation.updateMember(waiting.getMember());
            waitingRepository.delete(waiting);
            return;
        }

        reservationRepository.delete(reservation);
    }

    @Transactional
    public void cancelWaiting(Long id) {
        Waiting foundWaiting = waitingRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약 대기입니다."));

        waitingRepository.delete(foundWaiting);
    }

    @Transactional(readOnly = true)
    public Long findMemberIdByWaitingId(Long waitingId) {
        return waitingRepository.findMemberIdById(waitingId);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public List<WaitingResponse> findAllWaiting() {
        return waitingRepository.findAllReadOnly().stream()
                .map(WaitingResponse::from)
                .toList();
    }
}

