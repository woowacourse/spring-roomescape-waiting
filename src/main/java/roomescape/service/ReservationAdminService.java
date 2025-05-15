package roomescape.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.dto.request.ReservationAdminRegisterDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationAdminService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;

    public ReservationAdminService(ReservationRepository reservationRepository,
                                   ThemeRepository themeRepository,
                                   ReservationTimeRepository reservationTimeRepository,
                                   MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
    }

    public void saveReservation(ReservationAdminRegisterDto registerDto) {
        Member member = findMemberById(registerDto.memberId());
        ReservationTime reservationTime = findReservationTimeById(registerDto.timeId());
        Theme theme = findThemeById(registerDto.themeId());

        Reservation reservation = new Reservation(registerDto.date(), reservationTime, theme, member, LocalDate.now());

        reservationRepository.save(reservation);
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 테마는 존재하지 않습니다."));
    }

    private ReservationTime findReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 예약 시각은 존재하지 않습니다."));
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 회원은 존재하지 않습니다."));
    }
}
