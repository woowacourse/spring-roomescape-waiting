package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.waiting.WaitingRequest;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.util.DateUtil;

@Service
public class WaitingService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(
            MemberRepository memberRepository,
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository
    ) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public Long addWaiting(WaitingRequest waitingRequest) {
        Member member = findMember(waitingRequest.memberId());
        Reservation reservation = findReservation(waitingRequest.reservationId());
        validateOwner(member, reservation);
        validateUnPassedDate(reservation.getDate(), reservation.getTime().getStartAt());

        Waiting waiting = new Waiting(member, reservation);
        waitingRepository.save(waiting);

        return waiting.getId();
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 사용자 정보 입니다.",
                        new Throwable("member_id : " + memberId)
                ));
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약 정보 입니다.",
                        new Throwable("reservation_id : " + id)
                ));
    }

    private void validateOwner(Member member, Reservation reservation) {
        if (reservation.isOwner(member)) {
            throw new IllegalArgumentException();
        }
    }

    private void validateUnPassedDate(LocalDate date, LocalTime time) {
        if (DateUtil.isPastDateTime(date, time)) {
            throw new IllegalArgumentException(
                    "[ERROR] 지나간 날짜와 시간은 예약이 불가능합니다.",
                    new Throwable("생성 예약 시간 : " + date + " " + time)
            );
        }
    }
}
