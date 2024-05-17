package roomescape.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.WaitingWithRank;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.dto.ReservationReadOnly;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.exception.RoomEscapeBusinessException;
import roomescape.service.dto.ReservationBookedResponse;
import roomescape.service.dto.ReservationConditionRequest;
import roomescape.service.dto.ReservationResponse;
import roomescape.service.dto.ReservationSaveRequest;
import roomescape.service.dto.UserReservationResponse;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationResponse saveReservation(ReservationSaveRequest reservationSaveRequest) {
        Member member = findMemberById(reservationSaveRequest.memberId());
        ReservationSlot slot = getSlot(reservationSaveRequest);

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

    private ReservationSlot getSlot(ReservationSaveRequest reservationSaveRequest) {
        // TODO : SlotRepository 만들기
        LocalDate date = reservationSaveRequest.date();
        ReservationTime time = findTimeById(reservationSaveRequest.timeId());
        Theme theme = findThemeById(reservationSaveRequest.themeId());

        return new ReservationSlot(date, time, theme);
    }

    private void validateDuplicateReservation(Member member, Reservation reservation) {
        boolean isReservationExist = reservationRepository.existsBySlotAndMember(reservation.getSlot(), member);
        boolean isWaitingExist = waitingRepository.existsByReservationAndMember(reservation, member);

        if (isReservationExist || isWaitingExist) {
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
                .sorted(Comparator.comparing(ReservationBookedResponse::getDateTime))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserReservationResponse> findAllUserReservation(Long memberId, LocalDate date) {
        Member member = findMemberById(memberId);
        List<Reservation> reservations = reservationRepository.findByMemberAndSlot_DateGreaterThanEqual(member, date);

        List<WaitingWithRank> waitings = waitingRepository.findByMemberAndDateGreaterThanEqualWithRank(member, date);

        return Stream.concat(
                        UserReservationResponse.reservationsToResponseStream(reservations),
                        UserReservationResponse.waitingsToResponseStream(waitings)
                )
                .sorted(Comparator.comparing(UserReservationResponse::date))
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation foundReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약입니다."));

        reservationRepository.delete(foundReservation);
    }

    @Transactional
    public void deleteWaiting(Long id) {
        Waiting foundWaiting = waitingRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약입니다."));

        waitingRepository.delete(foundWaiting);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));
    }

    private ReservationTime findTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약 시간입니다."));
    }

    public Long findMemberIdById(Long id) {
        return waitingRepository.findMemberIdById(id);
    }
}

