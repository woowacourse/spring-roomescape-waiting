package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationV2;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.ReservationRequestV2;
import roomescape.dto.ReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationThemeRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationServiceV2 {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final MemberRepository memberRepository;

    public ReservationServiceV2(final ReservationRepository reservationRepository,
                                final ReservationTimeRepository reservationTimeRepository,
                                final ReservationThemeRepository reservationThemeRepository,
                                final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationThemeRepository = reservationThemeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse addReservationWithMemberId(final ReservationRequestV2 request, final long memberId) {
        long timeId = request.timeId();
        final long themeId = request.themeId();

        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 사용자 입니다."));
        final ReservationTime time = reservationTimeRepository.findById(timeId).orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약 시간 입니다."));
        final ReservationTheme theme = reservationThemeRepository.findById(themeId).orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 테마 입니다."));
        final ReservationV2 reservationV2 = new ReservationV2(member, request.date(), time, theme);
        ReservationV2 saved = reservationRepository.saveWithMember(reservationV2);
        return ReservationResponse.fromV2(saved);
    }

    public ReservationResponse addReservationForAdmin(final AdminReservationRequest request) {
        long timeId = request.timeId();
        final long themeId = request.themeId();

        final Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 사용자 입니다."));
        final ReservationTime time = reservationTimeRepository.findById(timeId).orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약 시간 입니다."));
        final ReservationTheme theme = reservationThemeRepository.findById(themeId).orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 테마 입니다."));
        final ReservationV2 reservationV2 = new ReservationV2(member, request.date(), time, theme);
        ReservationV2 saved = reservationRepository.saveWithMember(reservationV2);
        return ReservationResponse.fromV2(saved);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAllReservationsV2().stream()
                .map(ReservationResponse::fromV2)
                .toList();
    }

    public List<ReservationResponse> getFilteredReservations(final Long memberId, final Long themeId,
                                                       final LocalDate dateFrom, final LocalDate dateTo) {
        final List<ReservationV2> reservations = reservationRepository.findByMemberIdAndThemeIdAndDateFromAndDateTo(
                memberId, themeId, dateFrom, dateTo);
        return reservations.stream()
                .map(ReservationResponse::fromV2)
                .toList();
    }
}
