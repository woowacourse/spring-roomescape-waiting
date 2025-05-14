package roomescape.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsMemberException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsThemeException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsTimeException;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;
import roomescape.exception.custom.reason.reservation.ReservationPastTimeException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.dto.AdminFilterReservationRequest;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;


    public ReservationResponse create(final ReservationRequest request, final LoginMember loginMember) {
        final ReservationTime reservationTime = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberByEmail(loginMember.email());

        validateDuplicateDateTimeAndTheme(request.date(), reservationTime, theme);
        validatePastDateTime(request.date(), reservationTime);

        final Reservation notSavedReservation = new Reservation(request.date(), member, reservationTime, theme, ReservationStatus.PENDING);
        final Reservation savedReservation = reservationRepository.save(notSavedReservation);
        return ReservationResponse.from(savedReservation);
    }

    public ReservationResponse createForAdmin(final AdminReservationRequest request) {
        final ReservationTime reservationTime = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberById(request.memberId());

        validateDuplicateDateTimeAndTheme(request.date(), reservationTime, theme);
        validatePastDateTime(request.date(), reservationTime);

        final Reservation notSavedReservation = new Reservation(request.date(), member, reservationTime, theme, ReservationStatus.PENDING);
        final Reservation savedReservation = reservationRepository.save(notSavedReservation);
        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> readAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readAllByMemberAndThemeAndDateRange(final AdminFilterReservationRequest request) {
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberById(request.memberId());

        return reservationRepository.findAllByMemberAndThemeAndDateBetween(
                        member, theme,
                        request.from(), request.to()
                ).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteById(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException();
        }

        reservationRepository.deleteById(id);
    }

    private void validatePastDateTime(final LocalDate date, ReservationTime reservationTime) {
        final LocalDate today = LocalDate.now();
        final LocalDate reservationDate = date;
        if (reservationDate.isBefore(today)) {
            throw new ReservationPastDateException();
        }
        if (reservationDate.isEqual(today)) {
            validatePastTime(reservationTime);
        }
    }

    private void validatePastTime(final ReservationTime reservationTime) {
        if (reservationTime.isBefore(LocalTime.now())) {
            throw new ReservationPastTimeException();
        }
    }

    private void validateDuplicateDateTimeAndTheme(final LocalDate date, final ReservationTime reservationTime,
                                                   final Theme theme) {
        if (reservationRepository.existsByReservationTimeAndDateAndTheme(reservationTime, date, theme)) {
            throw new ReservationConflictException();
        }
    }

    private Theme getThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ReservationNotExistsThemeException());
    }

    private ReservationTime getReservationTimeById(final Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new ReservationNotExistsTimeException());
    }

    private Member getMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ReservationNotExistsMemberException());
    }

    private Member getMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ReservationNotExistsMemberException());
    }
}
