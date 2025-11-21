package com.example.spring.repository;

import com.example.spring.entity.Book;
import com.example.spring.entity.Loan;
import com.example.spring.entity.Member;
import com.example.spring.entity.MembershipType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository loanRepository;

    private Member createAndSaveMember(String name, String email) {
        Member member = Member.builder()
                .name(name)
                .email(email)
                .membershipType(MembershipType.REGULAR)
                .joinDate(LocalDateTime.now())
                .build();
        return entityManager.persistAndFlush(member);
    }

    private Book createAndSaveBook(String title, String author) {
        Book book = Book.builder()
                .title(title)
                .author(author)
                .price(new BigDecimal("20000"))
                .isbn("ISBN" + (System.nanoTime() % 1000000000L))
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();
        return entityManager.persistAndFlush(book);
    }

    @Test
    public void save_신규대출_저장성공() {
        // Given
        Member member = createAndSaveMember("홍길동", "hong@example.com");
        Book book = createAndSaveBook("테스트 도서", "테스트 저자");

        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        // When
        Loan savedLoan = loanRepository.save(loan);
        entityManager.flush();

        // Then
        assertThat(savedLoan.getId()).isNotNull();
        assertThat(savedLoan.getMember()).isEqualTo(member);
        assertThat(savedLoan.getBook()).isEqualTo(book);
        assertThat(savedLoan.getReturnDate()).isNull();
    }

    @Test
    public void findById_존재하는대출_대출반환() {
        // Given
        Member member = createAndSaveMember("김철수", "kim@example.com");
        Book book = createAndSaveBook("자바 프로그래밍", "김저자");

        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();
        Loan savedLoan = entityManager.persistAndFlush(loan);

        // When
        Optional<Loan> foundLoan = loanRepository.findById(savedLoan.getId());

        // Then
        assertThat(foundLoan).isPresent();
        assertThat(foundLoan.get().getMember().getName()).isEqualTo("김철수");
        assertThat(foundLoan.get().getBook().getTitle()).isEqualTo("자바 프로그래밍");
    }

    @Test
    public void findByMemberId_회원별대출조회() {
        // Given
        Member member1 = createAndSaveMember("회원1", "member1@example.com");
        Member member2 = createAndSaveMember("회원2", "member2@example.com");
        Book book1 = createAndSaveBook("도서1", "저자1");
        Book book2 = createAndSaveBook("도서2", "저자2");

        Loan loan1 = Loan.builder()
                .member(member1)
                .book(book1)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        Loan loan2 = Loan.builder()
                .member(member1)
                .book(book2)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        entityManager.persistAndFlush(loan1);
        entityManager.persistAndFlush(loan2);

        // When
        List<Loan> member1Loans = loanRepository.findByMemberId(member1.getId());

        // Then
        assertThat(member1Loans).hasSize(2);
        assertThat(member1Loans).allMatch(loan -> loan.getMember().getId().equals(member1.getId()));
    }

    @Test
    public void findByBookId_도서별대출조회() {
        // Given
        Member member1 = createAndSaveMember("회원1", "member1@example.com");
        Member member2 = createAndSaveMember("회원2", "member2@example.com");
        Book book = createAndSaveBook("인기도서", "유명저자");

        Loan loan1 = Loan.builder()
                .member(member1)
                .book(book)
                .loanDate(LocalDateTime.now().minusWeeks(4))
                .dueDate(LocalDateTime.now().minusWeeks(2))
                .createdDate(LocalDateTime.now())

                .returnDate(LocalDateTime.now().minusWeeks(1))
                .build();

        Loan loan2 = Loan.builder()
                .member(member2)
                .book(book)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        entityManager.persistAndFlush(loan1);
        entityManager.persistAndFlush(loan2);

        // When
        List<Loan> bookLoans = loanRepository.findByBookId(book.getId());

        // Then
        assertThat(bookLoans).hasSize(2);
        assertThat(bookLoans).allMatch(loan -> loan.getBook().getId().equals(book.getId()));
    }

    @Test
    public void findByMemberIdAndReturnDateIsNull_미반납도서조회() {
        // Given
        Member member = createAndSaveMember("대출회원", "borrower@example.com");
        Book book1 = createAndSaveBook("미반납도서1", "저자1");
        Book book2 = createAndSaveBook("반납도서", "저자2");

        Loan unreturned = Loan.builder()
                .member(member)
                .book(book1)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        Loan returned = Loan.builder()
                .member(member)
                .book(book2)
                .loanDate(LocalDateTime.now().minusWeeks(1))
                .dueDate(LocalDateTime.now().plusWeeks(1))
                .createdDate(LocalDateTime.now())

                .returnDate(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(unreturned);
        entityManager.persistAndFlush(returned);

        // When
        List<Loan> unreturnedLoans = loanRepository.findByMemberIdAndReturnDateIsNull(member.getId());

        // Then
        assertThat(unreturnedLoans).hasSize(1);
        assertThat(unreturnedLoans.get(0).getBook().getTitle()).isEqualTo("미반납도서1");
        assertThat(unreturnedLoans.get(0).getReturnDate()).isNull();
    }

    @Test
    public void findOverdueLoans_연체도서조회() {
        // Given
        Member member = createAndSaveMember("연체회원", "overdue@example.com");
        Book book1 = createAndSaveBook("연체도서", "저자1");
        Book book2 = createAndSaveBook("정상도서", "저자2");

        LocalDateTime pastDate = LocalDateTime.now().minusWeeks(1);
        LocalDateTime futureDate = LocalDateTime.now().plusWeeks(1);

        Loan overdueeLoan = Loan.builder()
                .member(member)
                .book(book1)
                .loanDate(LocalDateTime.now().minusWeeks(3))
                .dueDate(pastDate)
                .createdDate(LocalDateTime.now())

                .build();

        Loan normalLoan = Loan.builder()
                .member(member)
                .book(book2)
                .loanDate(LocalDateTime.now())
                .dueDate(futureDate)
                .createdDate(LocalDateTime.now())

                .build();

        entityManager.persistAndFlush(overdueeLoan);
        entityManager.persistAndFlush(normalLoan);

        // When
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDateTime.now());

        // Then
        assertThat(overdueLoans).hasSize(1);
        assertThat(overdueLoans.get(0).getBook().getTitle()).isEqualTo("연체도서");
        assertThat(overdueLoans.get(0).getDueDate()).isBefore(LocalDateTime.now());
    }

    @Test
    public void findByReturnDateIsNull_전체미반납도서조회() {
        // Given
        Member member1 = createAndSaveMember("회원1", "member1@example.com");
        Member member2 = createAndSaveMember("회원2", "member2@example.com");
        Book book1 = createAndSaveBook("미반납도서1", "저자1");
        Book book2 = createAndSaveBook("미반납도서2", "저자2");
        Book book3 = createAndSaveBook("반납도서", "저자3");

        Loan unreturned1 = Loan.builder()
                .member(member1)
                .book(book1)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        Loan unreturned2 = Loan.builder()
                .member(member2)
                .book(book2)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        Loan returned = Loan.builder()
                .member(member1)
                .book(book3)
                .loanDate(LocalDateTime.now().minusWeeks(1))
                .dueDate(LocalDateTime.now().plusWeeks(1))
                .createdDate(LocalDateTime.now())

                .returnDate(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(unreturned1);
        entityManager.persistAndFlush(unreturned2);
        entityManager.persistAndFlush(returned);

        // When
        List<Loan> unreturnedLoans = loanRepository.findByReturnDateIsNull();

        // Then
        assertThat(unreturnedLoans).hasSizeGreaterThanOrEqualTo(2);
        assertThat(unreturnedLoans).allMatch(loan -> loan.getReturnDate() == null);
    }

    @Test
    public void existsByBookIdAndReturnDateIsNull_도서대출가능여부확인() {
        // Given
        Member member = createAndSaveMember("대출회원", "borrower@example.com");
        Book availableBook = createAndSaveBook("대출가능도서", "저자1");
        Book unavailableBook = createAndSaveBook("대출불가도서", "저자2");

        Loan activeLoan = Loan.builder()
                .member(member)
                .book(unavailableBook)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        entityManager.persistAndFlush(activeLoan);

        // When & Then
        assertThat(loanRepository.existsByBookIdAndReturnDateIsNull(unavailableBook.getId())).isTrue();
        assertThat(loanRepository.existsByBookIdAndReturnDateIsNull(availableBook.getId())).isFalse();
    }

    @Test
    public void findByLoanDateBetween_기간별대출통계() {
        // Given
        Member member = createAndSaveMember("통계회원", "stats@example.com");
        Book book1 = createAndSaveBook("도서1", "저자1");
        Book book2 = createAndSaveBook("도서2", "저자2");

        LocalDateTime startDate = LocalDateTime.now().minusWeeks(2);
        LocalDateTime endDate = LocalDateTime.now().minusWeeks(1);
        LocalDateTime withinPeriod = LocalDateTime.now().minusWeeks(1).minusDays(3);
        LocalDateTime outsidePeriod = LocalDateTime.now().minusDays(1);

        Loan loanWithinPeriod = Loan.builder()
                .member(member)
                .book(book1)
                .loanDate(withinPeriod)
                .dueDate(withinPeriod.plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        Loan loanOutsidePeriod = Loan.builder()
                .member(member)
                .book(book2)
                .loanDate(outsidePeriod)
                .dueDate(outsidePeriod.plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        entityManager.persistAndFlush(loanWithinPeriod);
        entityManager.persistAndFlush(loanOutsidePeriod);

        // When
        List<Loan> loansInPeriod = loanRepository.findByLoanDateBetween(startDate, endDate);

        // Then
        assertThat(loansInPeriod).hasSizeGreaterThanOrEqualTo(1);
        assertThat(loansInPeriod).anyMatch(loan -> loan.getBook().getTitle().equals("도서1"));
    }

    @Test
    public void findLoanById_편의메서드_대출직접반환() {
        // Given
        Member member = createAndSaveMember("테스트회원", "test@example.com");
        Book book = createAndSaveBook("테스트도서", "테스트저자");

        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();
        Loan savedLoan = entityManager.persistAndFlush(loan);

        // When
        Loan foundLoan = loanRepository.findLoanById(savedLoan.getId());

        // Then
        assertThat(foundLoan).isNotNull();
        assertThat(foundLoan.getId()).isEqualTo(savedLoan.getId());
    }

    @Test
    public void findLoanById_존재하지않는대출_null반환() {
        // When
        Loan foundLoan = loanRepository.findLoanById(999L);

        // Then
        assertThat(foundLoan).isNull();
    }
}