package roomescape.service.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationitem.ReservationItem;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationitem.ReservationTheme;
import roomescape.domain.reservationitem.ReservationTime;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.response.MyPageReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.member.MemberService;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationItemService reservationItemService;
    private final MemberService memberService;
    private final ReservationThemeService reservationThemeService;
    private final ReservationTimeService reservationTimeService;

    public ReservationResponse addReservation(final CreateReservationRequest request) {
        final long timeId = request.timeId();
        final long themeId = request.themeId();
        final LocalDate date = request.date();

        final Member member = memberService.getMemberById(request.memberId());
        final ReservationTime time = reservationTimeService.getReservationTimeById(timeId);
        final ReservationTheme theme = reservationThemeService.getThemeById(themeId);

        final ReservationStatus reservationStatus = checkReservationStatus(date, time, theme);
        final ReservationItem reservationItem = reservationItemService.createReservationItemIfNotExist(
                date, time, theme
        );
        if (reservationRepository.existsByMemberAndReservationItem(member, reservationItem)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약을 등록하였습니다.");
        }

        final Reservation saved = reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .reservationItem(reservationItem)
                        .reservationStatus(reservationStatus)
                        .build()
        );
        return ReservationResponse.from(saved);
    }

    private ReservationStatus checkReservationStatus(LocalDate date, ReservationTime time, ReservationTheme theme) {
        if (reservationItemService.isExistReservationItem(date, time, theme)) {
            return ReservationStatus.PENDING;
        }
        return ReservationStatus.ACCEPTED;
    }

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
