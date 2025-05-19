package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.InvalidTimeException;
import roomescape.common.exception.message.IdExceptionMessage;
import roomescape.common.exception.message.ReservationExceptionMessage;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.admin.AdminReservationRequest;
import roomescape.reservation.dto.admin.AdminReservationSearchRequest;
import roomescape.reservation.dto.user.UserReservationRequest;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository timeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findAllByMemberId(final Long memberId) {
        return reservationRepository.findAllByMemberId(memberId).stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllByMemberAndThemeAndDate(
            final AdminReservationSearchRequest request
    ) {
        Long memberId = request.memberId();
        Long themeId = request.themeId();
        LocalDate dateFrom = request.dateFrom();
        LocalDate dateTo = request.dateTo();

        return reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(memberId, themeId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse addByUser(final Long memberId, final UserReservationRequest request) {
        return addReservation(memberId, request.date(), request.timeId(), request.themeId());
    }

    public ReservationResponse addByAdmin(final AdminReservationRequest request) {
        return addReservation(request.memberId(), request.date(), request.timeId(), request.themeId());
    }

    private ReservationResponse addReservation(Long memberId, LocalDate date, Long timeId, Long themeId) {
        Member member = searchMember(memberId);
        ReservationTime reservationTime = searchReservationTime(timeId);
        validateRequest(date, reservationTime);
        Theme theme = searchTheme(themeId);

        Reservation newReservation = new Reservation(member, date, reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(newReservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateRequest(final LocalDate reservationDate, final ReservationTime reservationTime) {
        validateFutureTime(reservationDate, reservationTime);
        validateDuplicateReservation(reservationDate, reservationTime);
    }

    private void validateFutureTime(final LocalDate reservationDate, final ReservationTime reservationTime) {
        if (isToday(reservationDate) && isPastTime(reservationTime)) {
            throw new InvalidTimeException(ReservationExceptionMessage.TIME_BEFORE_NOW.getMessage());
        }
    }

    private boolean isToday(final LocalDate reservationDate) {
        return reservationDate.equals(LocalDate.now());
    }

    private boolean isPastTime(final ReservationTime reservationTime) {
        return reservationTime.getStartAt().isBefore(LocalTime.now());
    }

    private void validateDuplicateReservation(
            final LocalDate reservationDate,
            final ReservationTime reservationTime
    ) {
        boolean isDuplicate = reservationRepository.existsByDateAndTimeId(
                reservationDate,
                reservationTime.getId()
        );
        if (isDuplicate) {
            throw new DuplicateException(ReservationExceptionMessage.DUPLICATE_RESERVATION.getMessage());
        }
    }

    public void deleteById(final Long id) {
        searchReservation(id);
        reservationRepository.deleteById(id);
    }

    private void searchReservation(final Long id) {
        reservationRepository.findById(id)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_RESERVATION_ID.getMessage()));
    }

    private Member searchMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_MEMBER_ID.getMessage()));
    }

    private ReservationTime searchReservationTime(final Long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_TIME_ID.getMessage()));
    }

    private Theme searchTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_THEME_ID.getMessage()));
    }
}
