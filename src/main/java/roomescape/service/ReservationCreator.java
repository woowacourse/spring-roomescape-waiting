package roomescape.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.StoreDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.dto.request.AdminReservationRequestDto;
import roomescape.dto.request.ReservationRequestDto;

@Component
public class ReservationCreator {
    private final ReservationDao reservationDao;
    private final TimeDao timeDao;
    private final ThemeDao themeDao;
    private final StoreDao storeDao;

    public ReservationCreator(
            ReservationDao reservationDao,
            TimeDao timeDao,
            ThemeDao themeDao,
            StoreDao storeDao
    ) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
        this.storeDao = storeDao;
    }

    public Reservation createByUser(Member member, ReservationRequestDto request, LocalDateTime now) {
        Time time = findTime(request.timeId());
        Theme theme = findTheme(request.themeId());
        Store store = findStore(request.storeId());
        Slot slot = new Slot(request.date(), time, theme, store);
        validateAvailable(slot);

        return Reservation.createByUser(member, request.date(), time, theme, store, now);
    }

    public Reservation createByAdmin(Member member, AdminReservationRequestDto request) {
        Time time = findTime(request.timeId());
        Theme theme = findTheme(request.themeId());
        Store store = findStore(request.storeId());
        Slot slot = new Slot(request.date(), time, theme, store);
        validateAvailable(slot);

        return Reservation.createByAdmin(member, request.date(), time, theme, store);
    }

    private void validateAvailable(Slot slot) {
        if (reservationDao.existsBySlotForUpdate(slot)) {
            throw new DuplicateEntityException("이미 존재하는 예약이 있습니다.");
        }
    }

    private Time findTime(Long id) {
        return timeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
    }

    private Theme findTheme(Long id) {
        return themeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마입니다."));
    }

    private Store findStore(Long id) {
        return storeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 매장입니다."));
    }
}
