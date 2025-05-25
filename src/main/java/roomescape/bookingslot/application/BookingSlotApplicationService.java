package roomescape.bookingslot.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.bookingslot.domain.service.BookingSlotDomainService;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.domain.Member;
import roomescape.member.domain.service.MemberDomainService;
import roomescape.bookingslot.domain.BookingSlot;
import roomescape.bookingslot.presentation.dto.response.MyReservationResponse;
import roomescape.bookingslot.presentation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.service.ReservationTimeDomainService;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.service.ThemeDomainService;
import roomescape.reservation.domain.service.ReservationDomainService;

@Service
public class BookingSlotApplicationService {

    private final BookingSlotDomainService bookingSlotDomainService;
    private final ReservationTimeDomainService reservationTimeDomainService;
    private final ThemeDomainService themeDomainService;
    private final MemberDomainService memberDomainService;
    private final ReservationDomainService reservationDomainService;

    public BookingSlotApplicationService(final BookingSlotDomainService bookingSlotDomainService,
                                         final ReservationTimeDomainService reservationTimeDomainService,
                                         final ThemeDomainService themeDomainService,
                                         final MemberDomainService memberDomainService,
                                         final ReservationDomainService slotReservationDomainService) {
        this.bookingSlotDomainService = bookingSlotDomainService;
        this.reservationTimeDomainService = reservationTimeDomainService;
        this.themeDomainService = themeDomainService;
        this.memberDomainService = memberDomainService;
        this.reservationDomainService = slotReservationDomainService;
    }

    public List<ReservationResponse> findReservations(final Long themeId, final Long memberId,
                                                      final LocalDate startDate,
                                                      final LocalDate endDate) {
        List<BookingSlot> filteredReservations = bookingSlotDomainService.findFilteredReservations(themeId, memberId,
                startDate, endDate);
        return filteredReservations
                .stream()
                .map(reservation -> {
                    ReservationTime time = reservation.getTime();
                    Theme theme = reservation.getTheme();
                    Member member = reservation.findReservedMember();
                    return ReservationResponse.of(reservation, time, theme, member);
                })
                .toList();
    }

    public void delete(Long id) {
        bookingSlotDomainService.delete(id);
    }

    public ReservationResponse create(final LocalDate date, final Long timeId, final Long themeId, final Long memberId,
                                      final LocalDateTime now) {
        bookingSlotDomainService.checkIfReservationDoesNotExists(date, timeId, themeId);
        ReservationTime time = reservationTimeDomainService.findReservationTime(timeId);
        Theme theme = themeDomainService.findTheme(themeId);
        Member member = memberDomainService.getMember(memberId);

        BookingSlot newBookingSlot = bookingSlotDomainService.save(member, date, time, theme, now);
        return ReservationResponse.of(newBookingSlot, time, theme, member);
    }

    public List<MyReservationResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationDomainService.findMyReservations(memberInfo);
    }
}
