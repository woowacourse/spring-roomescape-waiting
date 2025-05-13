package roomescape.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
import roomescape.theme.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository, final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse create(final ReservationRequest request, final LoginMember loginMember) {
        validateExistsReservationTime(request.timeId());
        validateExistsTheme(request.themeId());
        validateExistsMemberByEmail(loginMember.email());
        validateDuplicateDateTimeAndTheme(request.date(), request.timeId(), request.themeId());
        validatePastDateTime(request.date(), request.timeId());

        final Member member = memberRepository.findByEmail(loginMember.email());
        final Reservation reservation = new Reservation(request.date());
        final Long id = reservationRepository.save(reservation, request.timeId(), request.themeId(), member.getId());
        final Reservation savedReservation = reservationRepository.findById(id);
        return ReservationResponse.from(savedReservation);
    }

    public ReservationResponse createForAdmin(final AdminReservationRequest request) {
        validateExistsReservationTime(request.timeId());
        validateExistsTheme(request.themeId());
        validateExistsMemberById(request.memberId());
        validateDuplicateDateTimeAndTheme(request.date(), request.timeId(), request.themeId());
        validatePastDateTime(request.date(), request.timeId());

        final Reservation reservation = new Reservation(request.date());
        final Long id = reservationRepository.save(reservation, request.timeId(), request.themeId(),
                request.memberId());
        final Reservation savedReservation = reservationRepository.findById(id);
        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> readAll() {
        return reservationRepository.findAll().stream().map(ReservationResponse::from).toList();
    }

    public List<ReservationResponse> readAllByMemberAndThemeAndDateRange(final AdminFilterReservationRequest request) {
        return reservationRepository.findAllByMemberIdAndThemeIdAndDateRange(
                        request.memberId(), request.themeId(),
                        request.from(), request.to()
                ).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteById(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException();
        }
        reservationRepository.delete(id);
    }

    private void validatePastDateTime(final LocalDate date, final Long reservationTimeId) {
        final LocalDate today = LocalDate.now();
        final LocalDate reservationDate = date;
        if (reservationDate.isBefore(today)) {
            throw new ReservationPastDateException();
        }
        if (reservationDate.isEqual(today)) {
            validatePastTime(reservationTimeId);
        }
    }

    private void validatePastTime(final Long reservationTimeId) {
        final ReservationTime reservationTime = reservationTimeRepository.findById(reservationTimeId);
        if (reservationTime.isBefore(LocalTime.now())) {
            throw new ReservationPastTimeException();
        }
    }

    private void validateDuplicateDateTimeAndTheme(final LocalDate date, final Long reservationTimeId,
                                                   final Long themeId) {
        if (reservationRepository.existsByReservationTimeIdAndDateAndThemeId(reservationTimeId, date, themeId)) {
            throw new ReservationConflictException();
        }
    }

    private void validateExistsReservationTime(final Long reservationTimeId) {
        if (!reservationTimeRepository.existsById(reservationTimeId)) {
            throw new ReservationNotExistsTimeException();
        }
    }

    private void validateExistsTheme(final Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new ReservationNotExistsThemeException();
        }
    }

    private void validateExistsMemberByEmail(final String email) {
        if (!memberRepository.existsByEmail(email)) {
            throw new ReservationNotExistsMemberException();
        }
    }

    private void validateExistsMemberById(final Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ReservationNotExistsMemberException();
        }
    }
}
