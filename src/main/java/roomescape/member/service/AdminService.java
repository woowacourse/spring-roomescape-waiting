package roomescape.member.service;

import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.dto.request.CreateReservationByAdminRequest;
import roomescape.member.dto.response.CreateReservationResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class AdminService {
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public AdminService(final MemberRepository memberRepository,
                        final ReservationTimeRepository reservationTimeRepository,
                        final ThemeRepository themeRepository,
                        final ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public CreateReservationResponse createReservation(final CreateReservationByAdminRequest createReservationByAdminRequest) {
        Member member = memberRepository.getById(createReservationByAdminRequest.memberId());
        Theme theme = themeRepository.getById(createReservationByAdminRequest.themeId());
        ReservationTime reservationTime = reservationTimeRepository.getById(createReservationByAdminRequest.timeId());

        Reservation reservation = reservationRepository.save(
                new Reservation(member, createReservationByAdminRequest.date(), reservationTime, theme));
        return CreateReservationResponse.from(reservation);
    }
}
