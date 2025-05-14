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
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.admin.AdminReservationRequest;
import roomescape.reservation.dto.admin.AdminReservationSearchRequest;
import roomescape.reservation.dto.user.UserReservationRequest;
import roomescape.reservationTime.dao.ReservationTimeDao;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.dto.admin.ReservationTimeResponse;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final MemberDao memberDao;

    public ReservationService(
            ReservationDao reservationDao,
            ReservationTimeDao reservationTimeDao,
            ThemeDao themeDao,
            MemberDao memberDao
    ) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.memberDao = memberDao;
    }

    public List<ReservationResponse> findAll() {
        return reservationDao.findAll().stream()
                .map(reservation -> new ReservationResponse(
                        reservation.getId(),
                        reservation.getMember(),
                        reservation.getTheme(),
                        reservation.getDate(),
                        ReservationTimeResponse.from(reservation.getTime()))
                )
                .toList();
    }

    public List<ReservationResponse> findAllByMemberId(final Long memberId) {
        return reservationDao.findAllByMemberId(memberId).stream()
                .map(reservation -> new ReservationResponse(
                        reservation.getId(),
                        reservation.getMember(),
                        reservation.getTheme(),
                        reservation.getDate(),
                        ReservationTimeResponse.from(reservation.getTime()))
                )
                .toList();
    }

    public List<ReservationResponse> findAllByMemberAndThemeAndDate(
            final AdminReservationSearchRequest adminReservationSearchRequest
    ) {
        Long memberId = adminReservationSearchRequest.memberId();
        Long themeId = adminReservationSearchRequest.themeId();
        LocalDate dateFrom = adminReservationSearchRequest.dateFrom();
        LocalDate dateTo = adminReservationSearchRequest.dateTo();

        return reservationDao.findAllByMemberAndThemeAndDate(memberId, themeId, dateFrom, dateTo)
                .stream().map(reservation -> new ReservationResponse(
                        reservation.getId(),
                        reservation.getMember(),
                        reservation.getTheme(),
                        reservation.getDate(),
                        ReservationTimeResponse.from(reservation.getTime()))
                )
                .toList();
    }

    public ReservationResponse add(final Long memberId, final UserReservationRequest userReservationRequest) {
        Member memberResult = searchMember(memberId);
        ReservationTime reservationTimeResult = searchReservationTime(userReservationRequest.timeId());
        validateTime(userReservationRequest.date(), reservationTimeResult);
        validateAvailability(userReservationRequest.date(), reservationTimeResult);
        Theme themeResult = searchTheme(userReservationRequest.themeId());

        Reservation newReservation = new Reservation(
                memberResult,
                userReservationRequest.date(),
                reservationTimeResult,
                themeResult
        );
        Reservation savedReservation = reservationDao.add(newReservation);

        return new ReservationResponse(
                savedReservation.getId(),
                savedReservation.getMember(),
                savedReservation.getTheme(),
                savedReservation.getDate(),
                ReservationTimeResponse.from(savedReservation.getTime())
        );
    }

    public ReservationResponse addByAdmin(final AdminReservationRequest adminReservationRequest) {
        Member memberResult = searchMember(adminReservationRequest.memberId());
        ReservationTime reservationTimeResult = searchReservationTime(adminReservationRequest.timeId());
        validateTime(adminReservationRequest.date(), reservationTimeResult);
        validateAvailability(adminReservationRequest.date(), reservationTimeResult);
        Theme themeResult = searchTheme(adminReservationRequest.themeId());

        Reservation newReservation = new Reservation(
                memberResult,
                adminReservationRequest.date(),
                reservationTimeResult,
                themeResult
        );
        Reservation savedReservation = reservationDao.add(newReservation);

        return new ReservationResponse(
                savedReservation.getId(),
                savedReservation.getMember(),
                savedReservation.getTheme(),
                savedReservation.getDate(),
                ReservationTimeResponse.from(savedReservation.getTime())
        );
    }

    private void validateTime(final LocalDate reservationDate, final ReservationTime reservationTimeResult) {
        if (reservationDate.isEqual(LocalDate.now())
                && reservationTimeResult.getStartAt().isBefore(LocalTime.now())) {
            throw new InvalidTimeException(ReservationExceptionMessage.TIME_BEFORE_NOW.getMessage());
        }
    }

    private void validateAvailability(
            final LocalDate reservationDate,
            final ReservationTime reservationTimeResult
    ) {
        boolean isDuplicate = reservationDao.existsByDateAndTimeId(reservationDate, reservationTimeResult.getId());

        if (isDuplicate) {
            throw new DuplicateException(ReservationExceptionMessage.DUPLICATE_RESERVATION.getMessage());
        }
    }

    public void deleteById(final Long id) {
        searchReservation(id);
        reservationDao.deleteById(id);
    }

    private Reservation searchReservation(final Long id) {
        return reservationDao.findById(id)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_RESERVATION_ID.getMessage()));
    }

    private Member searchMember(Long memberId) {
        return memberDao.findById(memberId)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_MEMBER_ID.getMessage()));
    }

    private ReservationTime searchReservationTime(final Long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_TIME_ID.getMessage()));
    }

    private Theme searchTheme(final Long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_THEME_ID.getMessage()));
    }
}
