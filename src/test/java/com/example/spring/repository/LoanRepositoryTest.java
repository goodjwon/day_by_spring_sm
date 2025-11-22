package com.example.spring.repository;

import com.example.spring.entity.Book;
import com.example.spring.entity.Loan;
import com.example.spring.entity.LoanStatus;
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
        long uniqueNumber = System.nanoTime() % 10000000000L;
        String isbn13 = String.format("978%010d", uniqueNumber);

        Book book = Book.builder()
                .title(title)
                .author(author)
                .price(new BigDecimal("20000"))
                .isbn(isbn13)
                .available(true)
                .createdDate(LocalDateTime.now())
                .build();
        return entityManager.persistAndFlush(book);
    }

    private Loan createLoan(Member member, Book book) {
        return Loan.builder()
                .member(member)
                .book(book)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Test
    public void save_신규대출_저장성공() {
        // Given
        Member member = createAndSaveMember("홍길동", "hong@example.com");
        Book book = createAndSaveBook("테스트 도서", "테스트 저자");

        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .status(LoanStatus.ACTIVE)
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
                .status(LoanStatus.ACTIVE)
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
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        Loan loan2 = Loan.builder()
                .member(member1)
                .book(book2)
                .status(LoanStatus.ACTIVE)
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
                .status(LoanStatus.RETURNED)
                .loanDate(LocalDateTime.now().minusWeeks(4))
                .dueDate(LocalDateTime.now().minusWeeks(2))
                .createdDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().minusWeeks(1))
                .build();

        Loan loan2 = Loan.builder()
                .member(member2)
                .book(book)
                .status(LoanStatus.ACTIVE)
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
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())
                .build();

        Loan returned = Loan.builder()
                .member(member)
                .book(book2)
                .status(LoanStatus.RETURNED)
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
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now().minusWeeks(3))
                .dueDate(pastDate)
                .createdDate(LocalDateTime.now())

                .build();

        Loan normalLoan = Loan.builder()
                .member(member)
                .book(book2)
                .status(LoanStatus.ACTIVE)
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
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        Loan unreturned2 = Loan.builder()
                .member(member2)
                .book(book2)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        Loan returned = Loan.builder()
                .member(member1)
                .book(book3)
                .status(LoanStatus.ACTIVE)
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
                .status(LoanStatus.ACTIVE)
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
                .status(LoanStatus.ACTIVE)
                .loanDate(withinPeriod)
                .dueDate(withinPeriod.plusWeeks(2))
                .createdDate(LocalDateTime.now())

                .build();

        Loan loanOutsidePeriod = Loan.builder()
                .member(member)
                .book(book2)
                .status(LoanStatus.ACTIVE)
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
                .status(LoanStatus.ACTIVE)
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

    // ========== JOIN 메소드 테스트 ==========

    @Test
    public void findByMemberName_회원이름으로조회() {
        // Given
        Member member = createAndSaveMember("김철수", "kim@test.com");
        Book book = createAndSaveBook("자바의 정석", "남궁성");

        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .createdDate(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(loan);

        // When
        List<Loan> loans = loanRepository.findByMemberName("김철수");

        // Then
        assertThat(loans).hasSize(1);
        assertThat(loans.get(0).getMember().getName()).isEqualTo("김철수");
    }

    @Test
    public void findByBookTitle_도서제목으로조회() {
        // Given
        Member member = createAndSaveMember("이영희", "lee@test.com");
        Book book = createAndSaveBook("스프링 부트 핵심 가이드", "장정우");

        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .createdDate(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(loan);

        // When
        List<Loan> loans = loanRepository.findByBookTitle("스프링 부트 핵심 가이드");

        // Then
        assertThat(loans).hasSize(1);
        assertThat(loans.get(0).getBook().getTitle()).isEqualTo("스프링 부트 핵심 가이드");
    }

    @Test
    public void findOverdueLoansByMemberEmail_회원이메일로연체대여조회() {
        // Given
        Member member = createAndSaveMember("박민수", "park@test.com");
        Book book = createAndSaveBook("클린 코드", "로버트 마틴");

        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(5))
                .createdDate(LocalDateTime.now())

                .build();
        loan.updateStatus(); // OVERDUE 상태로 변경
        entityManager.persistAndFlush(loan);

        // When
        List<Loan> overdueLoans = loanRepository.findOverdueLoansByMemberEmail("park@test.com");

        // Then
        assertThat(overdueLoans).hasSize(1);
        assertThat(overdueLoans.get(0).getMember().getEmail()).isEqualTo("park@test.com");
    }

    @Test
    public void findAllWithMemberAndBook_FetchJoin으로조회() {
        // Given
        Member member1 = createAndSaveMember("홍길동", "hong@test.com");
        Member member2 = createAndSaveMember("강감찬", "kang@test.com");
        Book book1 = createAndSaveBook("이펙티브 자바", "조슈아 블로크");
        Book book2 = createAndSaveBook("토비의 스프링", "이일민");

        Loan loan1 = Loan.builder()
                .member(member1)
                .book(book1)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .createdDate(LocalDateTime.now())

                .build();

        Loan loan2 = Loan.builder()
                .member(member2)
                .book(book2)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .createdDate(LocalDateTime.now())

                .build();

        entityManager.persistAndFlush(loan1);
        entityManager.persistAndFlush(loan2);
        entityManager.clear(); // 영속성 컨텍스트 초기화

        // When
        List<Loan> loans = loanRepository.findAllWithMemberAndBook();

        // Then
        assertThat(loans).hasSize(2);
        // Fetch Join으로 인해 Member와 Book이 이미 로딩되어 있음
        assertThat(loans.get(0).getMember()).isNotNull();
        assertThat(loans.get(0).getBook()).isNotNull();
    }

    @Test
    public void findMembersByBookTitle_특정도서를대여한회원목록() {
        // Given
        Member member1 = createAndSaveMember("회원A", "a@test.com");
        Member member2 = createAndSaveMember("회원B", "b@test.com");
        Book book = createAndSaveBook("인기도서", "저자");

        Loan loan1 = Loan.builder()
                .member(member1)
                .book(book)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now().minusDays(30))
                .dueDate(LocalDateTime.now().minusDays(16))
                .returnDate(LocalDateTime.now().minusDays(15))
                .createdDate(LocalDateTime.now())

                .build();

        Loan loan2 = Loan.builder()
                .member(member2)
                .book(book)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .createdDate(LocalDateTime.now())

                .build();

        entityManager.persistAndFlush(loan1);
        entityManager.persistAndFlush(loan2);

        // When
        List<Member> members = loanRepository.findMembersByBookTitle("인기도서");

        // Then
        assertThat(members).hasSize(2);
        assertThat(members).extracting(Member::getName)
                .containsExactlyInAnyOrder("회원A", "회원B");
    }

    @Test
    public void findCurrentlyBorrowedBooks_회원의대여중인도서목록() {
        // Given
        Member member = createAndSaveMember("독서왕", "reader@test.com");
        Book book1 = createAndSaveBook("대여중 도서1", "저자1");
        Book book2 = createAndSaveBook("대여중 도서2", "저자2");
        Book book3 = createAndSaveBook("반납완료 도서", "저자3");

        // 대여 중인 도서들
        Loan activeLoan1 = Loan.builder()
                .member(member)
                .book(book1)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDateTime.now().plusDays(9))
                .createdDate(LocalDateTime.now())

                .build();

        Loan activeLoan2 = Loan.builder()
                .member(member)
                .book(book2)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now().minusDays(3))
                .dueDate(LocalDateTime.now().plusDays(11))
                .createdDate(LocalDateTime.now())

                .build();

        // 반납 완료된 도서
        Loan returnedLoan = Loan.builder()
                .member(member)
                .book(book3)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(6))
                .returnDate(LocalDateTime.now().minusDays(5))
                .createdDate(LocalDateTime.now())

                .build();

        entityManager.persistAndFlush(activeLoan1);
        entityManager.persistAndFlush(activeLoan2);
        entityManager.persistAndFlush(returnedLoan);

        // When
        List<Book> borrowedBooks = loanRepository.findCurrentlyBorrowedBooks(member.getId());

        // Then
        assertThat(borrowedBooks).hasSize(2);
        assertThat(borrowedBooks).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("대여중 도서1", "대여중 도서2");
    }

    @Test
    public void findOverdueLoansWithMember_연체대여와회원정보조회() {
        // Given
        Member member1 = createAndSaveMember("연체자1", "overdue1@test.com");
        Member member2 = createAndSaveMember("연체자2", "overdue2@test.com");
        Book book1 = createAndSaveBook("연체도서1", "저자");
        Book book2 = createAndSaveBook("연체도서2", "저자");

        Loan overdueLoan1 = Loan.builder()
                .member(member1)
                .book(book1)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now().minusDays(25))
                .dueDate(LocalDateTime.now().minusDays(11))
                .createdDate(LocalDateTime.now())

                .build();
        overdueLoan1.updateStatus(); // OVERDUE

        Loan overdueLoan2 = Loan.builder()
                .member(member2)
                .book(book2)
                .status(LoanStatus.ACTIVE)
                .loanDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(6))
                .createdDate(LocalDateTime.now())

                .build();
        overdueLoan2.updateStatus(); // OVERDUE

        entityManager.persistAndFlush(overdueLoan1);
        entityManager.persistAndFlush(overdueLoan2);
        entityManager.clear(); // 영속성 컨텍스트 초기화

        // When
        List<Loan> overdueLoans = loanRepository.findOverdueLoansWithMember();

        // Then
        assertThat(overdueLoans).hasSize(2);
        // Fetch Join으로 Member 정보도 함께 로딩됨
        assertThat(overdueLoans.get(0).getMember()).isNotNull();
        // dueDate 오름차순 정렬 확인
        assertThat(overdueLoans.get(0).getDueDate())
                .isBeforeOrEqualTo(overdueLoans.get(1).getDueDate());
    }
}