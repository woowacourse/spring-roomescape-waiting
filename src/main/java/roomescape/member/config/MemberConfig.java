package roomescape.member.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.member.dao.MemberDaoImpl;
import roomescape.member.dao.MemberDao;

@Configuration
public class MemberConfig {

    @Bean
    public MemberDao memberDao(@Autowired MemberDaoImpl memberDao) {
        return memberDao;
    }
}
