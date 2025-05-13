package roomescape.business.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.business.domain.Member;
import roomescape.business.domain.PlayTime;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidDateAndTimeException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.dao.MemberDao;
import roomescape.persistence.dao.PlayTimeDao;
import roomescape.persistence.dao.ReservationDao;
import roomescape.persistence.dao.ThemeDao;
import roomescape.presentation.dto.ReservationResponse;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final MemberDao memberDao;
    private final PlayTimeDao playTimeDao;
    private final ThemeDao themeDao;

    public ReservationService(final ReservationDao reservationDao, final MemberDao memberDao,
                              final PlayTimeDao playTimeDao, final ThemeDao themeDao
    ) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
        this.playTimeDao = playTimeDao;
        this.themeDao = themeDao;
    }

    public ReservationResponse insert(final LocalDate date, final Long memberId, final Long timeId,
                                      final Long themeId
    ) {
        validateMemberIdExists(memberId);
        validateTimeIdExists(timeId);
        validateThemeIdExists(themeId);
        validateIsDuplicate(date, timeId, themeId);
        final PlayTime playTime = playTimeDao.findById(timeId).get();
        validateDateAndTimeIsFuture(date, playTime.getStartAt());

        final Theme theme = themeDao.findById(themeId).get();
        final Member member = memberDao.findById(memberId).get();
        final Reservation reservation = new Reservation(date, member, playTime, theme);
        final Reservation savedReservation = reservationDao.insert(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateMemberIdExists(final Long memberId) {
        if (!memberDao.existsById(memberId)) {
            throw new NotFoundException("해당하는 사용자를 찾을 수 없습니다. 사용자 id: %d".formatted(memberId));
        }
    }

    private void validateTimeIdExists(final Long timeId) {
        if (!playTimeDao.existsById(timeId)) {
            throw new NotFoundException("해당하는 방탈출 예약 시간을 찾을 수 없습니다. 방탈출 id: %d".formatted(timeId));
        }
    }

    private void validateThemeIdExists(final Long themeId) {
        if (!themeDao.existsById(themeId)) {
            throw new NotFoundException("해당하는 테마를 찾을 수 없습니다. 테마 id: %d".formatted(themeId));
        }
    }

    private void validateIsDuplicate(final LocalDate date, final Long playTimeId, final Long themeId) {
        if (reservationDao.existsByDateAndTimeIdAndThemeId(date, playTimeId, themeId)) {
            throw new DuplicateException("추가 하려는 예약과 같은 날짜, 시간, 테마의 예약이 이미 존재합니다.");
        }
    }

    private void validateDateAndTimeIsFuture(final LocalDate date, final LocalTime time) {
        final LocalDateTime now = LocalDateTime.now();

        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time);
        if (reservationDateTime.isBefore(now)) {
            throw new InvalidDateAndTimeException("방탈출 예약 날짜와 시간이 현재보다 과거일 수 없습니다.");
        }
    }

    public List<ReservationResponse> findAll() {
        return reservationDao.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllFilter(final Long memberId, final Long themeId, final LocalDate startDate,
                                                   final LocalDate endDate) {
        return reservationDao.findAllByFilter(memberId, themeId, startDate, endDate)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteById(final Long id) {
        if (!reservationDao.deleteById(id)) {
            throw new NotFoundException("해당하는 방탈출 예약을 찾을 수 없습니다. 방탈출 id: %d".formatted(id));
        }
    }
}
