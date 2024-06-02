package roomescape.service;

import static roomescape.exception.RoomescapeExceptionCode.CANNOT_WAIT_FOR_MY_RESERVATION;
import static roomescape.exception.RoomescapeExceptionCode.MEMBER_NOT_FOUND;
import static roomescape.exception.RoomescapeExceptionCode.RESERVATION_NOT_FOUND;
import static roomescape.exception.RoomescapeExceptionCode.WAITING_DUPLICATED;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.MyReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationWaitingService(
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationRepository reservationRepository,
            MemberRepository memberRepository
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationWaitingResponse createWaiting(ReservationRequest request) {
        Member member = getMemberById(request.memberId());
        Reservation reservation = getReservationByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                request.themeId());
        validateDifferentMember(member, reservation.getMember());
        validateWaitingNotExists(member, reservation);
        ReservationWaiting waiting = reservationWaitingRepository.save(new ReservationWaiting(member, reservation));
        return ReservationWaitingResponse.from(waiting);
    }

    public List<MyReservationResponse> findWaitingsByMemberId(long memberId) {
        Member member = getMemberById(memberId);
        List<ReservationWaiting> waitings = reservationWaitingRepository.findAllByMember(member);
        return waitings.stream()
                .map(waiting -> MyReservationResponse.of(waiting, waiting.getRank(waitings)))
                .toList();
    }

    public List<ReservationWaitingResponse> findWaitings() {
        return reservationWaitingRepository.findAll()
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    public boolean existsByMemberAndReservation(Member member, Reservation reservation) {
        return reservationWaitingRepository.existsByMemberAndReservation(member, reservation);
    }

    public void deleteById(Long id) {
        reservationWaitingRepository.deleteById(id);
    }

    private Member getMemberById(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(MEMBER_NOT_FOUND));
    }

    private Reservation getReservationByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findByDateAndReservationTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_NOT_FOUND));
    }

    private void validateDifferentMember(Member member1, Member member2) {
        if (Objects.equals(member1, member2)) {
            throw new RoomescapeException(CANNOT_WAIT_FOR_MY_RESERVATION);
        }
    }

    private void validateWaitingNotExists(Member member, Reservation reservation) {
        if (existsByMemberAndReservation(member, reservation)) {
            throw new RoomescapeException(WAITING_DUPLICATED);
        }
    }
}
