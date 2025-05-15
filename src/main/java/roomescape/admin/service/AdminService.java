package roomescape.admin.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.domain.dto.AdminReservationRequestDto;
import roomescape.admin.domain.dto.SearchReservationRequestDto;
import roomescape.member.exception.UnauthorizedUserRoleException;
import roomescape.reservation.domain.dto.ReservationRequestDto;
import roomescape.reservation.domain.dto.ReservationResponseDto;
import roomescape.reservation.service.ReservationService;
import roomescape.user.domain.User;
import roomescape.user.service.UserService;

@Service
@Transactional
public class AdminService {

    private final ReservationService reservationService;
    private final UserService userService;

    public AdminService(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    public ReservationResponseDto createReservation(AdminReservationRequestDto adminReservationRequestDto) {
        User member = getUser(adminReservationRequestDto.memberId());
        ReservationRequestDto reservationRequestDto = convertAdminReservationRequestDtoToReservationRequestDto(
                adminReservationRequestDto);
        return reservationService.add(reservationRequestDto, member);
    }

    private User getUser(Long memberId) {
        User member = userService.findByIdOrThrow(memberId);
        if (!member.isMember()) {
            throw new UnauthorizedUserRoleException();
        }
        return member;
    }

    public List<ReservationResponseDto> searchReservations(SearchReservationRequestDto searchReservationRequestDto) {
        return reservationService.findReservationsByUserAndThemeAndFromAndTo(searchReservationRequestDto);
    }

    private static ReservationRequestDto convertAdminReservationRequestDtoToReservationRequestDto(
            AdminReservationRequestDto adminReservationRequestDto) {
        return new ReservationRequestDto(adminReservationRequestDto.date(),
                adminReservationRequestDto.timeId(),
                adminReservationRequestDto.themeId());
    }
}
