package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.controller.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.controller.dto.response.AdminReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public AdminReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public AdminReservationResponse save(final AdminReservationSaveRequest reservationSaveRequest) {
        Member member = findMemberById(reservationSaveRequest);
        ReservationTime reservationTime = findReservationTimeById(reservationSaveRequest);
        Theme theme = findThemeById(reservationSaveRequest);

        Reservation reservation = reservationRepository.save(
                reservationSaveRequest.toEntity(member, reservationTime, theme, Status.RESERVATION)
        );
        return AdminReservationResponse.from(reservation);
    }

    private ReservationTime findReservationTimeById(final AdminReservationSaveRequest reservationSaveRequest) {
        return reservationTimeRepository.findById(reservationSaveRequest.timeId())
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 예약 가능 시간 번호를 입력하였습니다."));
    }

    private Theme findThemeById(final AdminReservationSaveRequest reservationSaveRequest) {
        return themeRepository.findById(reservationSaveRequest.themeId())
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 테마 번호를 입력하였습니다."));
    }

    private Member findMemberById(final AdminReservationSaveRequest reservationSaveRequest) {
        return memberRepository.findById(reservationSaveRequest.memberId())
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 회원 번호를 입력하였습니다."));
    }

    public List<AdminReservationResponse> getByFilter(
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
                .map(AdminReservationResponse::from)
                .toList();
    }

    private List<Reservation> findByDateFromAndDateTo(final LocalDate dateFrom,
                                                      final LocalDate dateTo) {
        return getAllReservations().stream()
                .filter(reservation -> (dateFrom == null || reservation.getDate().isAfter(dateFrom))
                        && (dateTo == null || reservation.getDate().isBefore(dateTo)))
                .toList();
    }

    public List<Reservation> getAllReservations() {
        return StreamSupport.stream(reservationRepository.findAll().spliterator(), false)
                .toList();
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
