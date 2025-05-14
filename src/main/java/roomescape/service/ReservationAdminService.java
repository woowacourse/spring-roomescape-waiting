package roomescape.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.dao.MemberDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dto.request.ReservationAdminRegisterDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

@Service
public class ReservationAdminService {

    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;
    private final ReservationTimeDao reservationTimeDao;
    private final MemberDao memberDao;

    public ReservationAdminService(ReservationDao reservationDao, ThemeDao themeDao,
                                   ReservationTimeDao reservationTimeDao,
                                   MemberDao memberDao) {
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
        this.reservationTimeDao = reservationTimeDao;
        this.memberDao = memberDao;
    }

    public void saveReservation(ReservationAdminRegisterDto registerDto) {
        Member member = findMemberById(registerDto.memberId());
        ReservationTime reservationTime = findReservationTimeById(registerDto.timeId());
        Theme theme = findThemeById(registerDto.themeId());

        Reservation reservation = new Reservation(registerDto.date(), reservationTime, theme, member, LocalDate.now());

        reservationDao.saveReservation(reservation);
    }

    private Theme findThemeById(final Long id) {
        return themeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 테마는 존재하지 않습니다."));
    }

    private ReservationTime findReservationTimeById(final Long id) {
        return reservationTimeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 예약 시각은 존재하지 않습니다."));
    }

    private Member findMemberById(final Long id) {
        return memberDao.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 회원은 존재하지 않습니다."));
    }
}
