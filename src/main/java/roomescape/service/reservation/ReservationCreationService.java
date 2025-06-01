package roomescape.service.reservation;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationitem.ReservationItem;
import roomescape.domain.reservationitem.ReservationTheme;
import roomescape.domain.reservationitem.ReservationTime;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.member.MemberService;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationCreationService {

    private final ReservationRepository reservationRepository;
    private final ReservationItemService reservationItemService;
    private final MemberService memberService;
    private final ReservationThemeService reservationThemeService;
    private final ReservationTimeService reservationTimeService;

    public ReservationResponse addReservation(final CreateReservationRequest request) {
        return createReservation(request, ReservationStatus.ACCEPTED, false);
    }

    public ReservationResponse addPendingReservation(final CreateReservationRequest request) {
        return createReservation(request, ReservationStatus.PENDING, true);
    }

    private ReservationResponse createReservation(
            final CreateReservationRequest request,
            final ReservationStatus status,
            final boolean requiresExistingReservation) {

        final Member member = memberService.getMemberById(request.memberId());
        final ReservationTime time = reservationTimeService.getReservationTimeById(request.timeId());
        final ReservationTheme theme = reservationThemeService.getThemeById(request.themeId());
        final LocalDate date = request.date();

        validateReservationAvailability(date, time, theme, requiresExistingReservation);

        final ReservationItem reservationItem = reservationItemService.createReservationItemIfNotExist(date, time, theme);

        validateDuplicateReservation(member, reservationItem);

        final Reservation saved = reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .reservationItem(reservationItem)
                        .reservationStatus(status)
                        .build()
        );
        return ReservationResponse.from(saved);
    }

    private void validateReservationAvailability(
            final LocalDate date,
            final ReservationTime time,
            final ReservationTheme theme,
            final boolean requiresExistingReservation) {

        final boolean reservationExists = reservationItemService.isExistReservationItem(date, time, theme);

        if (requiresExistingReservation && !reservationExists) {
            throw new IllegalArgumentException("[ERROR] 대기 예약은 기존 예약이 있을 때만 가능합니다.");
        }

        if (!requiresExistingReservation && reservationExists) {
            throw new IllegalArgumentException("[ERROR] 이미 예약된 시간입니다.");
        }
    }

    private void validateDuplicateReservation(final Member member, final ReservationItem reservationItem) {
        if (reservationRepository.existsByMemberAndReservationItem(member, reservationItem)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약을 등록하였습니다.");
        }
    }
}
