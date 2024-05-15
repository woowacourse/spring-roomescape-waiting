package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.controller.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.controller.dto.request.ReservationSaveRequest;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

@Service
public class AdminReservationService {

    private final ReservationService reservationService;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public AdminReservationService(final ReservationService reservationService, final MemberRepository memberRepository,
                                   final ReservationRepository reservationRepository) {
        this.reservationService = reservationService;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponse save(final AdminReservationSaveRequest reservationSaveRequest) {
        Member member = findMemberById(reservationSaveRequest.memberId());

        return reservationService.save(new ReservationSaveRequest(
                reservationSaveRequest.themeId(),
                reservationSaveRequest.date(),
                reservationSaveRequest.timeId()
        ), member);
    }

    private Member findMemberById(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 회원 번호를 입력하였습니다."));
    }

    public List<ReservationResponse> getByFilter(
            final Long memberId, final Long themeId,
            final LocalDate dateFrom, final LocalDate dateTo
    ) {
        List<Reservation> reservations = new ArrayList<>();
        if (memberId != null) {
            reservations.addAll(reservationRepository.findByMemberId(memberId));
        }
        if (themeId != null) {
            reservations.addAll(reservationRepository.findByThemeId(themeId));
        }
        reservations.addAll(findByDateFromAndDateTo(dateFrom, dateTo));

        return filterResults(reservations, memberId, themeId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private List<Reservation> findByDateFromAndDateTo(final LocalDate dateFrom,
                                                      final LocalDate dateTo) {
        return reservationService.getAllReservations().stream()
                .filter(reservation -> isReservationAfterDateFrom(dateFrom, reservation)
                        && isReservationBeforeDateTo(dateTo, reservation)
                ).toList();
    }

    private boolean isReservationAfterDateFrom(final LocalDate dateFrom, final Reservation reservation) {
        return dateFrom == null || reservation.getDate().isAfter(dateFrom);
    }

    private boolean isReservationBeforeDateTo(final LocalDate dateTo, final Reservation reservation) {
        return dateTo == null || reservation.getDate().isBefore(dateTo);
    }

    private List<Reservation> filterResults(
            final List<Reservation> reservations,
            final Long memberId, final Long themeId,
            final LocalDate dateFrom, final LocalDate dateTo
    ) {
        return reservations.stream()
                .filter(reservation ->
                        (memberId == null || reservation.getMember().getId().equals(memberId))
                )
                .filter(reservation ->
                        (themeId == null || reservation.getTheme().getId().equals(themeId))
                ).filter(reservation ->
                        (dateFrom == null ||
                                (reservation.getDate().isEqual(dateFrom) || reservation.getDate().isAfter(dateFrom)))
                )
                .filter(reservation ->
                        (dateTo == null ||
                                (reservation.getDate().isEqual(dateTo) || reservation.getDate().isBefore(dateTo)))
                )
                .distinct()
                .toList();
    }
}
