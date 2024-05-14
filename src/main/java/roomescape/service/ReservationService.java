package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.repository.MemberDao;
import roomescape.repository.ReservationDao;
import roomescape.repository.ThemeDao;
import roomescape.repository.TimeDao;

@Service
public class ReservationService {
    private final MemberDao memberDao;
    private final TimeDao timeDao;
    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;

    public ReservationService(final MemberDao memberDao, final TimeDao timeDao,
                              final ReservationDao reservationDao, final ThemeDao themeDao) {
        this.memberDao = memberDao;
        this.timeDao = timeDao;
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
    }

    public List<ReservationResponse> findEntireReservationList() {
        return reservationDao.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findDistinctReservations(final Long memberId, final Long themeId,
                                                              final String dateFrom, final String dateTo) {
        return reservationDao.findAllByMemberAndDateAndTheme(memberId, themeId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse create(final ReservationRequest reservationRequest) {
        Member member = memberDao.findById(reservationRequest.memberId());
        TimeSlot timeSlot = checkTimeSlot(reservationRequest);
        Theme theme = checkTheme(reservationRequest);

        validate(reservationRequest.date(), timeSlot, theme);

        Long reservationId = reservationDao.create(reservationRequest);
        Reservation reservation = reservationRequest.toEntity(reservationId, member, timeSlot, theme);

        return ReservationResponse.from(reservation);
    }

    public void delete(final Long id) {
        reservationDao.delete(id);
    }

    private TimeSlot checkTimeSlot(final ReservationRequest reservationRequest) {
        TimeSlot timeSlot;
        try {
            timeSlot = timeDao.findById(reservationRequest.timeId());
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("[ERROR] 존재하지 않는 예약 시간입니다");
        }

        return timeSlot;
    }

    private Theme checkTheme(final ReservationRequest reservationRequest) {
        Theme theme;
        try {
            theme = themeDao.findById(reservationRequest.themeId());
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("[ERROR] 존재하지 않는 테마 입니다");
        }

        return theme;
    }

    private void validate(final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        validateReservation(date, timeSlot);
        validateDuplicatedReservation(date, timeSlot.getId(), theme.getId());
    }

    private void validateDuplicatedReservation(final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationDao.isExists(date, timeId, themeId)) {
            throw new IllegalArgumentException("[ERROR] 예약이 종료되었습니다");
        }
    }

    private void validateReservation(final LocalDate date, final TimeSlot time) {
        if (time == null || (time.isTimeBeforeNow() && !date.isAfter(LocalDate.now()))) {
            throw new IllegalArgumentException("[ERROR] 지나간 날짜와 시간으로 예약할 수 없습니다");
        }
    }
}
