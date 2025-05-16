package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.User;
import roomescape.dto.request.AdminReservationRequestDto;
import roomescape.dto.request.ReservationRequestDto;
import roomescape.dto.request.SearchReservationRequestDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.exception.local.UnauthorizedUserRoleException;

@Service
@Transactional
public class AdminService {

    private final ReservationService reservationService;
    private final UserService userService;

    public AdminService(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    public ReservationResponseDto createReservation(AdminReservationRequestDto adminReservationRequestDto,
            User admin) {
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

    public List<ReservationResponseDto> searchReservations(SearchReservationRequestDto searchReservationRequestDto,
            User admin) {
        return reservationService.findReservationsByUserAndThemeAndFromAndTo(searchReservationRequestDto);
    }

    private static ReservationRequestDto convertAdminReservationRequestDtoToReservationRequestDto(
            AdminReservationRequestDto adminReservationRequestDto) {
        return new ReservationRequestDto(adminReservationRequestDto.date(),
                adminReservationRequestDto.timeId(),
                adminReservationRequestDto.themeId());
    }
}
