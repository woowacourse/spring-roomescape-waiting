package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.NotFoundException;
import roomescape.dao.MemberDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationRegisterDto;
import roomescape.dto.request.ReservationSearchDto;
import roomescape.dto.response.MemberResponseDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.dto.response.ThemeResponseDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final MemberDao memberDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao,
                              MemberDao memberDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.memberDao = memberDao;
    }

    public ReservationResponseDto saveReservation(ReservationRegisterDto reservationRegisterDto,
                                                  LoginMember loginMember) {
        Reservation reservation = createReservation(reservationRegisterDto, loginMember);
        assertReservationIsNotDuplicated(reservation);

        Long reservationId = reservationDao.saveReservation(reservation);

        ReservationTime time = reservation.getTime();
        Theme theme = reservation.getTheme();
        return new ReservationResponseDto(
                reservationId,
                new MemberResponseDto(reservation.getMember()),
                reservation.getDate(),
                new ReservationTimeResponseDto(time.getId(), time.getStartAt()),
                new ThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail())
        );
    }

    public List<ReservationResponseDto> getAllReservations(final ReservationSearchDto reservationSearchDto) {
        return reservationDao.findAll(reservationSearchDto).stream()
                .map(ReservationResponseDto::from)
                .toList();
    }

    public void cancelReservation(Long id) {
        reservationDao.deleteById(id);
    }

    private Reservation createReservation(ReservationRegisterDto reservationRegisterDto, LoginMember loginMember) {
        ReservationTime foundTime = reservationTimeDao.findById(reservationRegisterDto.timeId())
                .orElseThrow(() -> new NotFoundException("id 에 해당하는 예약 시각이 존재하지 않습니다."));

        Theme foundTheme = themeDao.findById(reservationRegisterDto.themeId())
                .orElseThrow(() -> new NotFoundException("id 에 해당하는 테마가 존재하지 않습니다."));

        Member member = findMemberById(loginMember.id());
        return reservationRegisterDto.convertToReservation(foundTime, foundTheme, member);
    }

    private void assertReservationIsNotDuplicated(Reservation reservation) {
        reservationDao.findByDateAndTime(reservation)
                .ifPresent(foundReservation -> {
                    throw new DuplicatedException("이미 예약이 존재합니다.");
                });
    }

    private Member findMemberById(final Long id) {
        return memberDao.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자에 대한 예약 요청입니다."));
    }
}
