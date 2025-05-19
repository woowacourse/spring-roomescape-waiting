package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.MyPageReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationThemeRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ReservationThemeRepository reservationThemeRepository,
                              final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationThemeRepository = reservationThemeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse addReservationWithMemberId(final ReservationRequest request, final long memberId) {
        final long timeId = request.timeId();
        final long themeId = request.themeId();
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 사용자 입니다."));
        final ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약 시간 입니다."));
        final ReservationTheme theme = reservationThemeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 테마 입니다."));
        validateDuplicateReservation(request.date(), timeId, themeId);
        final Reservation reservation = new Reservation(member, request.date(), time, theme);
        Reservation saved = reservationRepository.saveWithMember(reservation);
        return ReservationResponse.fromV2(saved);
    }

    public ReservationResponse addReservationForAdmin(final AdminReservationRequest request) {
        final long timeId = request.timeId();
        final long themeId = request.themeId();
        final Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 사용자 입니다."));
        final ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약 시간 입니다."));
        final ReservationTheme theme = reservationThemeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 테마 입니다."));
        final LocalDate date = request.date();
        validateDuplicateReservation(date, timeId, themeId);
        final Reservation reservation = Reservation.builder()
                .member(member)
                .date(date)
                .time(time)
                .theme(theme)
                .build();
        Reservation saved = reservationRepository.saveWithMember(reservation);
        return ReservationResponse.fromV2(saved);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAllReservationsV2().stream()
                .map(ReservationResponse::fromV2)
                .toList();
    }

    public List<ReservationResponse> getFilteredReservations(final Long memberId, final Long themeId,
                                                             final LocalDate dateFrom, final LocalDate dateTo) {
        final List<Reservation> reservations = reservationRepository.findByMemberIdAndThemeIdAndDateFromAndDateTo(
                memberId, themeId, dateFrom, dateTo);
        return reservations.stream()
                .map(ReservationResponse::fromV2)
                .toList();
    }

    private void validateDuplicateReservation(final LocalDate localDate, final long timeId, final long themeId) {
        if (reservationRepository.existByDateAndTimeIdAndThemeId(localDate, timeId, themeId)) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 예약 입니다.");
        }
    }

    public List<MyPageReservationResponse> getReservationsByMemberId(Long memberId) {
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 사용자 입니다."));
        List<Reservation> myReservations = reservationRepository.findByMemberId(member.getId());
        return myReservations.stream()
                .map(MyPageReservationResponse::from)
                .toList();
    }

    public void removeReservation(Long reservationId) {
        final Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));
        reservationRepository.deleteById(reservation.getId());
    }
}
