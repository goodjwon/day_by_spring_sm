package com.example.spring;

import com.example.spring.entity.Book;
import com.example.spring.traditional.repository.TraditionalBookRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTests {

	private static TraditionalBookRepository repository;
	private static Book testBook;

	@BeforeAll
	static void setUp() {
		repository = new TraditionalBookRepository();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void findById_success() {

		Book book = repository.findById(1L);

		assertNotNull(book);
		assertEquals(1L, book.getId());
	}

	@Test
	void findAll_전체조회() {
		List<Book> books = repository.findAll();
		assertNotNull(books);
		assertFalse(books.isEmpty());
	}
}

//todo 테스트를 기동을 하면 일단 데이터를 조회 할 수 있게 설정 바꾸거나 스프링 환경 내에서 테스트를 할수 있게 조정을 해야한다.