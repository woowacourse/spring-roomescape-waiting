package roomescape.business.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.business.domain.PlayTime;
import roomescape.business.domain.Reservation;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.dao.PlayTimeDao;
import roomescape.persistence.dao.ReservationDao;
import roomescape.presentation.dto.PlayTimeRequest;
import roomescape.presentation.dto.PlayTimeResponse;
import roomescape.presentation.dto.ReservationAvailableTimeResponse;

@Service
public class PlayTimeService {

    private final PlayTimeDao playTimeDao;
    private final ReservationDao reservationDao;

    public PlayTimeService(final PlayTimeDao playTimeDao, final ReservationDao reservationDao) {
        this.playTimeDao = playTimeDao;
        this.reservationDao = reservationDao;
    }

    public PlayTimeResponse insert(final PlayTimeRequest playTimeRequest) {
        validateStartAtIsNotDuplicate(playTimeRequest.startAt());
        final PlayTime playTime = playTimeRequest.toDomain();
        final PlayTime insertPlayTime = playTimeDao.insert(playTime);
        return PlayTimeResponse.from(insertPlayTime);
    }

    private void validateStartAtIsNotDuplicate(final LocalTime startAt) {
        if (playTimeDao.existsByStartAt(startAt)) {
            throw new DuplicateException("추가 하려는 시간이 이미 존재합니다.");
        }
    }

    public List<PlayTimeResponse> findAll() {
        return playTimeDao.findAll()
                .stream()
                .map(PlayTimeResponse::from)
                .toList();
    }

    public PlayTimeResponse findById(final Long id) {
        final PlayTime playTime = playTimeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 방탈출 시간을 찾을 수 없습니다. 방탈출 id: %d".formatted(id)));
        return PlayTimeResponse.from(playTime);
    }

    public void deleteById(final Long id) {
        if (!playTimeDao.deleteById(id)) {
            throw new NotFoundException("해당하는 방탈출 시간을 찾을 수 없습니다. 방탈출 id: %d".formatted(id));
        }
    }

    public List<ReservationAvailableTimeResponse> findAvailableTimes(final LocalDate date, final Long themeId) {
        final List<Reservation> reservations = reservationDao.findByDateAndThemeId(date, themeId);
        final List<PlayTime> playTimes = playTimeDao.findAll();
        return playTimes.stream()
                .map(playTime -> {
                    boolean isAlreadyBooked = containPlayTime(reservations, playTime);
                    return new ReservationAvailableTimeResponse(playTime, isAlreadyBooked);
                })
                .collect(Collectors.toList());
    }

    private boolean containPlayTime(final List<Reservation> reservations, final PlayTime playTime) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.isSamePlayTime(playTime));
    }
}
