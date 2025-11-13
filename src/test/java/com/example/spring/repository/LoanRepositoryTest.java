package com.example.spring.repository;

import com.example.spring.entity.Book;
import com.example.spring.entity.Loan;
import com.example.spring.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LoanRepositoryTest {
    /**
     * save_신규주문_저장성공
     * findById_존재하는주문_주문반환
     * findById_존재하지않는주문_빈Optional반환
     * findAll_주문목록반환
     * findOrderById_존재하는주문_주문직접반환
     * findOrderById_존재하지않는주문_null반환
     */
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void save_신규대여_저장성공() {
        //Given
        Member member = Member.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();
        Member savedMember = memberRepository.save(member);

        Book book = Book.builder()
                .title("스프링 부트 완전 정복")
                .author("김작가")
                .price(new BigDecimal("25000"))
                .isbn("978-89-12345-67-8")
                .available(true)
                .build();
        Book savedBook = bookRepository.save(book);

        Loan newLoan = Loan.builder()
                .book(savedBook)
                .member(savedMember)
                .loanDate(LocalDate.now().atStartOfDay())
                .dueDate(LocalDate.now().plusWeeks(2).atStartOfDay())
                .build();

        // When
        Loan savedLoan = loanRepository.save(newLoan);

        // Then
        assertThat(savedLoan.getId()).isNotNull();
        assertThat(savedLoan.getMember().getId()).isEqualTo(savedMember.getId());
        assertThat(savedLoan.getBook().getId()).isEqualTo(savedBook.getId());
        assertThat(savedLoan.getLoanDate()).isNotNull();
    }

    @Test
    public void findById_존재하는대여_대여반환() {
        //Given
        Member member = Member.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();
        Member savedMember = memberRepository.save(member);

        Book book = Book.builder()
                .title("스프링 부트 완전 정복")
                .author("김작가")
                .price(new BigDecimal("25000"))
                .isbn("978-89-12345-67-8")
                .available(true)
                .build();
        Book savedBook = bookRepository.save(book);

        Loan newloan = Loan.builder()
                .book(savedBook)
                .member(savedMember)
                .loanDate(LocalDate.now().atStartOfDay())
                .dueDate(LocalDate.now().plusWeeks(2).atStartOfDay())
                .build();
        Loan savedLoan = loanRepository.save(newloan);
        Long loanId = savedLoan.getId();

        //When
        Optional<Loan> foundLoan = loanRepository.findById(loanId);

        //Then
        assertThat(foundLoan).isPresent();
        assertThat(foundLoan.get().getId()).isEqualTo(loanId);
        assertThat(foundLoan.get().getMember().getId()).isEqualTo(savedMember.getId());
        assertThat(foundLoan.get().getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(foundLoan.get().getLoanDate()).isEqualTo(savedLoan.getLoanDate());
        assertThat(foundLoan.get().getDueDate()).isEqualTo(savedLoan.getDueDate());
    }

    @Test
    public void findById_존재하지않는대여_빈Optional반환() {
        //Given
        Long loanId = 150L;
        //When
        Optional<Loan> foundLoan = loanRepository.findById(loanId);
        //Then
        assertThat(foundLoan).isEmpty();
    }

    @Test
    public void findAll_대여목록반환() {
        //When
        List<Loan> loans = loanRepository.findAll();
        //Then
        assertThat(loans).isNotEmpty();
        assertThat(loans).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    public void findLoanById_존재하는주문_주문직접반환() {
        //Given
        Loan savedLoan = loanRepository.save(createLoan(
                memberRepository.save(Member.builder().name("이회원").email("lee@example.com").build()),
                bookRepository.save(createBook("책3", "3333"))
        ));
        Long loanId = savedLoan.getId();
        //When
        Loan foundLoan = loanRepository.findByIdOrThrow(loanId);
        //Then
        assertThat(foundLoan).isNotNull();
        assertThat(foundLoan.getId()).isEqualTo(loanId);
        assertThat(foundLoan.getBook().getTitle()).isEqualTo("책3");
    }

    @Test
    public void findLoanById_존재하지않는주문_null반환() {
        //Given
        Long nonExistentId = 999L;
        //When
        Loan foundLoan = loanRepository.findByIdOrThrow(nonExistentId);
        //Then
        assertThat(foundLoan).isNull();
    }

    private Book createBook(String title, String isbn) {
        return Book.builder()
                .title(title)
                .author("김작가")
                .price(new BigDecimal("25000"))
                .isbn(isbn)
                .available(true)
                .build();
    }

    private Loan createLoan(Member member, Book book) {
        return Loan.builder()
                .book(book)
                .member(member)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .build();
    }
}
