package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.ReservationWaitingRepository;
import roomescape.service.request.ReservationWaitingAppRequest;
import roomescape.service.response.ReservationWaitingAppResponse;
import roomescape.service.response.ReservationWaitingAppResponseWithRank;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository,
                                     MemberRepository memberRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationWaitingAppResponse> findAll() {
        return reservationWaitingRepository.findAll().stream()
                .map(ReservationWaitingAppResponse::from)
                .toList();
    }

    public List<ReservationWaitingAppResponseWithRank> findAllByMemberId(Long memberId) {
        return reservationWaitingRepository.findAllByMemberId(memberId).stream()
                .map(reservationWaiting -> ReservationWaitingAppResponseWithRank.of(reservationWaiting,
                        getRank(reservationWaiting)))
                .toList();
    }

    @Transactional
    public ReservationWaitingAppResponse save(ReservationWaitingAppRequest request) {
        Member member = findMember(request.memberId());
        Reservation reservation = findReservation(request.date(), request.timeId(), request.themeId());
        validateDuplication(reservation.getId(), member.getId());
        ReservationWaiting newReservationWaiting = ReservationWaiting.create(
                member,
                reservation,
                getPriority(reservation.getId())
        );
        ReservationWaiting savedreservationWaiting = reservationWaitingRepository.save(newReservationWaiting);

        return ReservationWaitingAppResponse.from(savedreservationWaiting);
    }

    private long getPriority(Long reservationId) {
        return reservationWaitingRepository.countByReservationId(reservationId) + 1;
    }

    private void validateDuplication(Long reservationId, Long memberId) {
        if (reservationWaitingRepository.existsByReservationIdAndMemberId(reservationId, memberId)) {
            throw new IllegalStateException("중복 예약 대기는 불가능합니다.");
        }
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원 정보를 찾지 못했습니다."));
    }

    private Reservation findReservation(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(new ReservationDate(date.toString()), timeId,
                        themeId)
                .orElseThrow(() -> new NoSuchElementException("해당 예약이 없습니다. 예약해주세요."));
    }

    @Transactional
    public void deleteBy(Long id) {
        reservationWaitingRepository.deleteById(id);
    }

    private long getRank(ReservationWaiting target) {
        return reservationWaitingRepository.getRankByReservationAndPriority(
                target.getReservation().getId(),
                target.getPriority());
    }
}
