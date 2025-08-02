package com.example.spring.repository;

import com.example.spring.entity.Member;
import com.example.spring.entity.MembershipType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(com.example.spring.repository.impl.JpaMemberRepository.class)
public class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void save_신규회원_저장성공() {
        // Given
        Member newMember = Member.builder()
                .name("홍길동")
                .email("hong@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();

        // When
        Member savedMember = memberRepository.save(newMember);
        entityManager.flush();

        // Then
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getName()).isEqualTo("홍길동");
        assertThat(savedMember.getEmail()).isEqualTo("hong@example.com");
        assertThat(savedMember.getMembershipType()).isEqualTo(MembershipType.REGULAR);
    }

    @Test
    public void findById_존재하는회원_회원반환() {
        // Given
        Member member = Member.builder()
                .name("김철수")
                .email("kim@example.com")
                .membershipType(MembershipType.PREMIUM)
                .joinDate(LocalDateTime.now())
                .build();
        Member savedMember = entityManager.persistAndFlush(member);

        // When
        Optional<Member> foundMember = memberRepository.findById(savedMember.getId());

        // Then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("김철수");
        assertThat(foundMember.get().getEmail()).isEqualTo("kim@example.com");
    }

    @Test
    public void findById_존재하지않는회원_빈Optional반환() {
        // When
        Optional<Member> foundMember = memberRepository.findById(999L);

        // Then
        assertThat(foundMember).isEmpty();
    }

    @Test
    public void findByEmail_존재하는이메일_회원반환() {
        // Given
        Member member = Member.builder()
                .name("박영희")
                .email("park@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(member);

        // When
        Optional<Member> foundMember = memberRepository.findByEmail("park@example.com");

        // Then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("박영희");
    }

    @Test
    public void findByEmail_존재하지않는이메일_빈Optional반환() {
        // When
        Optional<Member> foundMember = memberRepository.findByEmail("notexist@example.com");

        // Then
        assertThat(foundMember).isEmpty();
    }

    @Test
    public void findByMembershipType_멤버십타입별조회() {
        // Given
        Member regularMember = Member.builder()
                .name("일반회원")
                .email("regular@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();

        Member premiumMember = Member.builder()
                .name("프리미엄회원")
                .email("premium@example.com")
                .membershipType(MembershipType.PREMIUM)
                .joinDate(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(regularMember);
        entityManager.persistAndFlush(premiumMember);

        // When
        List<Member> regularMembers = memberRepository.findByMembershipType(MembershipType.REGULAR);
        List<Member> premiumMembers = memberRepository.findByMembershipType(MembershipType.PREMIUM);

        // Then
        assertThat(regularMembers).isNotEmpty();
        assertThat(premiumMembers).isNotEmpty();
        assertThat(regularMembers).anyMatch(m -> m.getName().equals("일반회원"));
        assertThat(premiumMembers).anyMatch(m -> m.getName().equals("프리미엄회원"));
    }

    @Test
    public void findByNameContaining_이름검색() {
        // Given
        Member member1 = Member.builder()
                .name("김철수")
                .email("kim1@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();

        Member member2 = Member.builder()
                .name("김영희")
                .email("kim2@example.com")
                .membershipType(MembershipType.PREMIUM)
                .joinDate(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(member1);
        entityManager.persistAndFlush(member2);

        // When
        List<Member> foundMembers = memberRepository.findByNameContaining("김");

        // Then
        assertThat(foundMembers).hasSizeGreaterThanOrEqualTo(2);
        assertThat(foundMembers).anyMatch(m -> m.getName().equals("김철수"));
        assertThat(foundMembers).anyMatch(m -> m.getName().equals("김영희"));
    }

    @Test
    public void existsByEmail_이메일존재확인() {
        // Given
        Member member = Member.builder()
                .name("이순신")
                .email("lee@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(member);

        // When & Then
        assertThat(memberRepository.existsByEmail("lee@example.com")).isTrue();
        assertThat(memberRepository.existsByEmail("notexist@example.com")).isFalse();
    }

    @Test
    public void findAll_회원목록조회() {
        // Given
        Member member1 = Member.builder()
                .name("회원1")
                .email("member1@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();

        Member member2 = Member.builder()
                .name("회원2")
                .email("member2@example.com")
                .membershipType(MembershipType.PREMIUM)
                .joinDate(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(member1);
        entityManager.persistAndFlush(member2);

        // When
        List<Member> members = memberRepository.findAll();

        // Then
        assertThat(members).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    public void deleteById_회원삭제() {
        // Given
        Member member = Member.builder()
                .name("삭제할회원")
                .email("delete@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
        Member savedMember = entityManager.persistAndFlush(member);
        Long memberId = savedMember.getId();

        // When
        memberRepository.deleteById(memberId);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Member> deletedMember = memberRepository.findById(memberId);
        assertThat(deletedMember).isEmpty();
    }

    @Test
    public void findMemberById_편의메서드_회원직접반환() {
        // Given
        Member member = Member.builder()
                .name("테스트회원")
                .email("test@example.com")
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
        Member savedMember = entityManager.persistAndFlush(member);

        // When
        Member foundMember = memberRepository.findMemberById(savedMember.getId());

        // Then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getName()).isEqualTo("테스트회원");
    }

    @Test
    public void findMemberById_존재하지않는회원_null반환() {
        // When
        Member foundMember = memberRepository.findMemberById(999L);

        // Then
        assertThat(foundMember).isNull();
    }
}