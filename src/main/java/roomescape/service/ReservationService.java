package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.response.MyPageReservationResponse;
import roomescape.dto.response.ReservationResponse;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
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

        validateDuplicateReservation(date, timeId, themeId);
        final Reservation reservation = Reservation.builder()
                .member(member)
                .date(date)
                .time(time)
                .theme(theme)
                .build();
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
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

    private void validateDuplicateReservation(final LocalDate localDate, final long timeId, final long themeId) {
        if (reservationRepository.existByDateAndTimeIdAndThemeId(localDate, timeId, themeId)) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 예약 입니다.");
        }
    }

    public List<MyPageReservationResponse> getReservationsByMemberId(Long memberId) {
        final Member member = memberService.getMemberById(memberId);
        List<Reservation> myReservations = reservationRepository.findByMemberId(member.getId());
        return myReservations.stream()
                .map(MyPageReservationResponse::from)
                .toList();
    }

    public void removeReservation(Long reservationId) {
        final Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약입니다."));
        reservationRepository.deleteById(reservation.getId());
    }
}
