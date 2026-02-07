package com.example.spring.repository;

import com.example.spring.domain.vo.ISBN;
import com.example.spring.domain.vo.Money;
import com.example.spring.entity.*;
import org.junit.jupiter.api.DisplayName;
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
                .price(Money.of(new BigDecimal("20000")))
                .isbn(ISBN.of("ISBN" + (System.nanoTime() % 1000000000L)))
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

    // ========== JOIN 관련 테스트 ==========

    @Test
    public void findByMemberName_회원이름으로대출조회() {
        // Given
        Member member1 = createAndSaveMember("김철수", "kim@example.com");
        Member member2 = createAndSaveMember("박영희", "park@example.com");
        Book book1 = createAndSaveBook("자바 프로그래밍", "저자1");
        Book book2 = createAndSaveBook("스프링 부트", "저자2");

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
        List<Loan> loans = loanRepository.findByMemberName("김철수");

        // Then
        assertThat(loans).hasSize(2);
        assertThat(loans).allMatch(loan -> loan.getMember().getName().equals("김철수"));
    }

    @Test
    public void findByBookTitle_도서제목으로대출조회() {
        // Given
        Member member1 = createAndSaveMember("회원1", "member1@example.com");
        Member member2 = createAndSaveMember("회원2", "member2@example.com");
        Book popularBook = createAndSaveBook("인기도서", "유명저자");

        Loan loan1 = Loan.builder()
                .member(member1)
                .book(popularBook)
                .loanDate(LocalDateTime.now().minusWeeks(4))
                .dueDate(LocalDateTime.now().minusWeeks(2))
                .createdDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().minusWeeks(1))
                .build();

        Loan loan2 = Loan.builder()
                .member(member2)
                .book(popularBook)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(loan1);
        entityManager.persistAndFlush(loan2);

        // When
        List<Loan> loans = loanRepository.findByBookTitle("인기도서");

        // Then
        assertThat(loans).hasSize(2);
        assertThat(loans).allMatch(loan -> loan.getBook().getTitle().equals("인기도서"));
    }

    @Test
    public void findAllWithMemberAndBook_FetchJoin으로N플러스1문제해결() {
        // Given
        Member member = createAndSaveMember("테스트회원", "test@example.com");
        Book book1 = createAndSaveBook("도서1", "저자1");
        Book book2 = createAndSaveBook("도서2", "저자2");

        Loan loan1 = Loan.builder()
                .member(member)
                .book(book1)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        Loan loan2 = Loan.builder()
                .member(member)
                .book(book2)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(loan1);
        entityManager.persistAndFlush(loan2);
        entityManager.clear(); // 영속성 컨텍스트 초기화

        // When
        List<Loan> loans = loanRepository.findAllWithMemberAndBook();

        // Then
        assertThat(loans).hasSizeGreaterThanOrEqualTo(2);
        // Fetch Join으로 이미 로드되어 있어야 함
        loans.forEach(loan -> {
            assertThat(loan.getMember()).isNotNull();
            assertThat(loan.getBook()).isNotNull();
        });
    }

    @Test
    public void findMembersByBookTitle_특정도서를대여한회원목록조회() {
        // Given
        Member member1 = createAndSaveMember("회원1", "member1@example.com");
        Member member2 = createAndSaveMember("회원2", "member2@example.com");
        Member member3 = createAndSaveMember("회원3", "member3@example.com");
        Book popularBook = createAndSaveBook("베스트셀러", "유명저자");
        Book otherBook = createAndSaveBook("다른책", "다른저자");

        Loan loan1 = Loan.builder()
                .member(member1)
                .book(popularBook)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        Loan loan2 = Loan.builder()
                .member(member2)
                .book(popularBook)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        Loan loan3 = Loan.builder()
                .member(member3)
                .book(otherBook)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(loan1);
        entityManager.persistAndFlush(loan2);
        entityManager.persistAndFlush(loan3);

        // When
        List<Member> members = loanRepository.findMembersByBookTitle("베스트셀러");

        // Then
        assertThat(members).hasSize(2);
        assertThat(members).extracting(Member::getName)
                .containsExactlyInAnyOrder("회원1", "회원2");
    }

    @Test
    public void findCurrentlyBorrowedBooks_특정회원의현재대여중인도서목록() {
        // Given
        Member member = createAndSaveMember("대출회원", "borrower@example.com");
        Book book1 = createAndSaveBook("현재대여중1", "저자1");
        Book book2 = createAndSaveBook("현재대여중2", "저자2");
        Book book3 = createAndSaveBook("반납완료", "저자3");

        Loan currentLoan1 = Loan.builder()
                .member(member)
                .book(book1)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        Loan currentLoan2 = Loan.builder()
                .member(member)
                .book(book2)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        Loan returnedLoan = Loan.builder()
                .member(member)
                .book(book3)
                .loanDate(LocalDateTime.now().minusWeeks(3))
                .dueDate(LocalDateTime.now().minusWeeks(1))
                .createdDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().minusWeeks(1))
                .build();

        entityManager.persistAndFlush(currentLoan1);
        entityManager.persistAndFlush(currentLoan2);
        entityManager.persistAndFlush(returnedLoan);

        // When
        List<Book> borrowedBooks = loanRepository.findCurrentlyBorrowedBooks(member.getId());

        // Then
        assertThat(borrowedBooks).hasSize(2);
        assertThat(borrowedBooks).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("현재대여중1", "현재대여중2");
    }

    @Test
    public void countOverdueLoans_연체대여수조회() {
        // Given
        Member member = createAndSaveMember("연체회원", "overdue@example.com");
        Book book1 = createAndSaveBook("연체도서1", "저자1");
        Book book2 = createAndSaveBook("연체도서2", "저자2");
        Book book3 = createAndSaveBook("정상도서", "저자3");

        Loan overdueLoan1 = Loan.builder()
                .member(member)
                .book(book1)
                .loanDate(LocalDateTime.now().minusWeeks(4))
                .dueDate(LocalDateTime.now().minusWeeks(1))
                .createdDate(LocalDateTime.now())
                .build();

        Loan overdueLoan2 = Loan.builder()
                .member(member)
                .book(book2)
                .loanDate(LocalDateTime.now().minusWeeks(3))
                .dueDate(LocalDateTime.now().minusDays(2))
                .createdDate(LocalDateTime.now())
                .build();

        Loan normalLoan = Loan.builder()
                .member(member)
                .book(book3)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(overdueLoan1);
        entityManager.persistAndFlush(overdueLoan2);
        entityManager.persistAndFlush(normalLoan);

        // When
        long overdueCount = loanRepository.countOverdueLoans();

        // Then
        assertThat(overdueCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("existsByMemberAndStatus: 특정 회원의 특정 대출 상태 존재 여부 확인")
    void existsByMemberAndStatus() {
        Member member = createAndSaveMember("강감찬", "kang@example.com");
        Book book = createAndSaveBook("귀주대첩", "역사학자");

        //Given
        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .status(LoanStatus.OVERDUE)
                .createdDate(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(loan);

        //When
        boolean hasOverdue = loanRepository.existsByMemberAndStatus(member, LoanStatus.OVERDUE);
        //Then
        assertThat(hasOverdue).isTrue();


    }
}