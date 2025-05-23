package roomescape.admin.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.domain.dto.AdminReservationRequestDto;
import roomescape.admin.domain.dto.SearchReservationRequestDto;
import roomescape.member.exception.UnauthorizedMemberException;
import roomescape.reservation.domain.dto.ReservationRequestDto;
import roomescape.reservation.domain.dto.ReservationResponseDto;
import roomescape.reservation.service.ReservationService;
import roomescape.user.domain.User;
import roomescape.user.service.UserService;
import roomescape.waiting.domain.dto.WaitingResponseDto;
import roomescape.waiting.service.WaitingService;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final ReservationService reservationService;
    private final UserService userService;
    private final WaitingService waitingService;

    public AdminService(ReservationService reservationService, UserService userService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.userService = userService;
        this.waitingService = waitingService;
    }

    @Transactional
    public ReservationResponseDto createReservation(AdminReservationRequestDto adminReservationRequestDto) {
        User member = getUser(adminReservationRequestDto.memberId());
        ReservationRequestDto reservationRequestDto = convertAdminReservationRequestDtoToReservationRequestDto(
                adminReservationRequestDto);
        return reservationService.add(reservationRequestDto, member);
    }

    private User getUser(Long memberId) {
        User member = userService.findByIdOrThrow(memberId);
        if (!member.isMember()) {
            throw new UnauthorizedMemberException();
        }
        return member;
    }

    public List<ReservationResponseDto> searchReservations(SearchReservationRequestDto searchReservationRequestDto) {
        return reservationService.findReservationsByUserAndThemeAndFromAndTo(searchReservationRequestDto);
    }

    public List<WaitingResponseDto> findAllWaitings() {
        return waitingService.findAll();
    }

    private static ReservationRequestDto convertAdminReservationRequestDtoToReservationRequestDto(
            AdminReservationRequestDto adminReservationRequestDto) {
        return new ReservationRequestDto(adminReservationRequestDto.date(),
                adminReservationRequestDto.timeId(),
                adminReservationRequestDto.themeId());
    }

    @Transactional
    public void deleteWaitingById(Long waitingId) {
        waitingService.delete(waitingId);
    }
}
