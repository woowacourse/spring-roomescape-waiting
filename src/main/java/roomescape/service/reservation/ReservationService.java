package roomescape.service.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationitem.ReservationItem;
import roomescape.dto.response.MyPageReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.WaitingReservationResponse;
import roomescape.service.member.MemberService;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationItemService reservationItemService;
    private final MemberService memberService;

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAllReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getFilteredReservations(final Long memberId,
                                                             final Long themeId,
                                                             final LocalDate dateFrom,
                                                             final LocalDate dateTo) {
        final List<Reservation> reservations = reservationRepository.findByMemberIdAndThemeIdAndDateFromAndDateTo(
                memberId,
                themeId,
                dateFrom,
                dateTo
        );
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<WaitingReservationResponse> getAllWaitingReservations() {
        List<Reservation> waitingReservations = reservationRepository.findByReservationStatusOrderByIdDesc(ReservationStatus.PENDING);
        return waitingReservations.stream()
                .map(WaitingReservationResponse::from)
                .toList();
    }

    public List<MyPageReservationResponse> getReservationsByMemberId(Long memberId) {
        final Member member = memberService.getMemberById(memberId);
        List<Reservation> myReservations = reservationRepository.findByMemberId(member.getId());
        return myReservations.stream()
                .map(reservation -> {
                            final int priority = calculatePriority(reservation);
                            return MyPageReservationResponse.from(reservation, priority);
                        }
                )
                .toList();
    }

    private int calculatePriority(Reservation reservation) {
        Long reservationItemId = reservation.getReservationItem().getId();
        Long currentReservationId = reservation.getId();

        return (int) reservationRepository.countByReservationItemIdAndIdLessThan(
                reservationItemId, currentReservationId
        );
    }

    @Transactional
    public void denyPendingReservation(Long reservationId) {
        Reservation waitingReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약입니다."));

        waitingReservation.deny();
    }

    @Transactional
    public void removeReservation(Long reservationId) {
        Reservation targetReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약입니다."));

        if (targetReservation.getReservationStatus() == ReservationStatus.PENDING) {
            deleteReservationOnly(targetReservation);
        } else if (targetReservation.getReservationStatus() == ReservationStatus.ACCEPTED) {
            handleAcceptedReservationRemoval(targetReservation);
        }
    }

    private void handleAcceptedReservationRemoval(Reservation targetReservation) {
        ReservationItem reservationItem = targetReservation.getReservationItem();

        reservationRepository.findFirstByReservationItemAndReservationStatusOrderByIdAsc(
                reservationItem, ReservationStatus.PENDING
        ).ifPresentOrElse(
                nextReservation -> {
                    nextReservation.changeStatusToAccepted();
                    reservationRepository.save(nextReservation);
                    deleteReservationOnly(targetReservation);
                },
                () -> deleteReservationWithItem(targetReservation, reservationItem)
        );
    }

    private void deleteReservationOnly(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
    }

    private void deleteReservationWithItem(Reservation reservation, ReservationItem reservationItem) {
        reservationRepository.deleteById(reservation.getId());
        reservationItemService.deleteReservationItem(reservationItem);
    }
}
