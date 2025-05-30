package roomescape.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.NotFoundException;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationAdminRegisterDto;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@RequiredArgsConstructor
@Service
public class ReservationAdminService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveReservation(final ReservationAdminRegisterDto registerDto) {
        final Member member = findMemberById(registerDto.memberId());
        final ReservationTime reservationTime = findReservationTimeById(registerDto.timeId());
        final Theme theme = findThemeById(registerDto.themeId());

        final Reservation reservation = new Reservation(
                registerDto.date(),
                reservationTime,
                theme,
                member,
                LocalDate.now()
        );

        reservationRepository.save(reservation);
    }

    private Theme findThemeById(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 테마는 존재하지 않습니다."));
    }

    private ReservationTime findReservationTimeById(final Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 예약 시각은 존재하지 않습니다."));
    }

    private Member findMemberById(final Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 회원은 존재하지 않습니다."));
    }
}
