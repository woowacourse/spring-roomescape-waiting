package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.fixture.MemberFixture;
import roomescape.util.DatabaseCleaner;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MemberRepositoryTest {
    @Autowired
    MemberRepository sut;
    @Autowired
    DatabaseCleaner databaseCleaner;
    @BeforeEach
    void setup(){
        databaseCleaner.initialize();
    }

    @Test
    void create(){
        final var result = sut.save(MemberFixture.getDomain());
        assertThat(result).isNotNull();
    }
    @Test
    void findById() {
        final var id = sut.save(MemberFixture.getDomain())
                .getId();
        final var result = sut.findById(id);
        assertThat(result).contains(MemberFixture.getDomain());
    }
    @Test
    void delete(){
        final var member = sut.save(MemberFixture.getDomain());
        sut.delete(member);
        final var result = sut.findById(member.getId());
        assertThat(result).isNotPresent();
    }
    @Test
    void getAll(){
        sut.save(MemberFixture.getDomain());
        sut.save(MemberFixture.getDomain("alphaka@gmail.com"));

        final var result = sut.findAll();
        assertThat(result).hasSize(2);
    }
    @Test
    void findByEmail(){
        sut.save(MemberFixture.getDomain("alphaka@gmail.com"));

        final var result = sut.findByEmailAddress("alphaka@gmail.com");
        assertThat(result).contains(MemberFixture.getDomain("alphaka@gmail.com"));
    }
    @Test
    void existsByEmailAddress(){
        sut.save(MemberFixture.getDomain("alphaka@gmail.com"));
        final var result = sut.existsByEmailAddress("alphaka@gmail.com");
        assertThat(result).isTrue();
    }
}
