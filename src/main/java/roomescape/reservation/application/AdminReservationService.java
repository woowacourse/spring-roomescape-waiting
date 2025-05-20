package roomescape.reservation.application;

import static roomescape.auth.domain.AuthRole.ADMIN;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthRole;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.request.ReservationsByFilterRequest;
import roomescape.reservation.ui.dto.response.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
@RequiredArgsConstructor
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationResponse create(
            final CreateReservationRequest request
    ) {
        return ReservationResponse.from(
                createReservation(
                        request.date(),
                        request.timeId(),
                        request.themeId(),
                        request.memberId(),
                        request.status()
                ));
    }

    private Reservation createReservation(
            final LocalDate date,
            final Long timeId,
            final Long themeId,
            final Long memberId,
            final ReservationStatus status
    ) {
        final ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 시간이 존재하지 않습니다."));
        validateNoDuplicateReservation(date, timeId, themeId);

        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 테마가 존재하지 않습니다."));
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        final Reservation reservation = new Reservation(date, reservationTime, theme, member, status);

        return reservationRepository.save(reservation);
    }

    private void validateNoDuplicateReservation(final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new AlreadyExistException("해당 날짜와 시간에 이미 해당 테마에 대한 예약이 있습니다.");
        }
    }

    public void deleteAsAdmin(final Long reservationId, final AuthRole authRole) {
        if (authRole != ADMIN) {
            throw new AuthorizationException("관리자만 삭제할 권한이 있습니다.");
        }

        if (!reservationRepository.existsById(reservationId)) {
            // 이미 취소되어 예약이 존재하지 않음을 구분하는 것이 UX에 좋을 것으로 예상됨
            throw new ResourceNotFoundException("해당 예약을 찾을 수 없습니다.");
        }
        reservationRepository.deleteById(reservationId);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllByFilter(final ReservationsByFilterRequest request) {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        return reservationRepository.findAllByThemeIdAndMemberIdAndDateRange(
                        request.themeId(), request.memberId(), request.dateFrom(), request.dateTo()
                )
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
