package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.*;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationResponses;
import roomescape.exception.ReservationFailException;
import roomescape.exception.clienterror.InvalidIdException;
import roomescape.repository.MemberDao;
import roomescape.repository.ReservationDao;
import roomescape.repository.ThemeDao;
import roomescape.repository.TimeDao;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {
    private final TimeDao timeDao;
    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;
    private final MemberDao memberDao;

    public ReservationService(final TimeDao timeDao, final ReservationDao reservationDao, final ThemeDao themeDao, final MemberDao memberDao) {
        this.timeDao = timeDao;
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
        this.memberDao = memberDao;
    }

    public ReservationResponses findEntireReservationList() {
        final List<ReservationResponse> reservationResponses = reservationDao.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return new ReservationResponses(reservationResponses);
    }

    public ReservationResponse create(final ReservationRequest reservationRequest) {
        validateDuplicatedReservation(reservationRequest);
        final TimeSlot timeSlot = getTimeSlot(reservationRequest);
        final Theme theme = getTheme(reservationRequest);
        final Member member = getMember(reservationRequest);
        final ReservationCreateValidator reservationCreateValidator = new ReservationCreateValidator(reservationRequest, timeSlot, theme, member);
        final Reservation newReservation = reservationCreateValidator.create();
        final Long id = reservationDao.create(newReservation);
        return ReservationResponse.from(newReservation.with(id));
    }

    private TimeSlot getTimeSlot(final ReservationRequest reservationRequest) {
        return timeDao.findById(reservationRequest.timeId())
                .orElseThrow(() -> new InvalidIdException("timeId", reservationRequest.timeId()));
    }

    private Theme getTheme(final ReservationRequest reservationRequest) {
        return themeDao.findById(reservationRequest.themeId())
                .orElseThrow(() -> new InvalidIdException("themeId", reservationRequest.themeId()));
    }

    private Member getMember(final ReservationRequest reservationRequest) {
        return memberDao.findById(reservationRequest.memberId())
                .orElseThrow(() -> new InvalidIdException("memberId", reservationRequest.memberId()));
    }

    private void validateDuplicatedReservation(final ReservationRequest reservationRequest) {
        if (reservationDao.isExists(reservationRequest.date(), reservationRequest.timeId(), reservationRequest.themeId())) {
            throw new ReservationFailException("이미 예약이 등록되어 있습니다.");
        }
    }

    public void delete(final Long id) {
        reservationDao.delete(id);
    }

    public ReservationResponses findReservations(final Long themeId, final Long memberId, final LocalDate dateFrom, final LocalDate dateTo) {
        final List<ReservationResponse> reservationResponses = reservationDao.find(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return new ReservationResponses(reservationResponses);
    }
}
