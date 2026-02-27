package com.example.spring.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressTest {

    @Nested
    @DisplayName("Address 생성")
    class Creation {

        @Test
        @DisplayName("전체 주소 정보로 Address 생성")
        void createWithFullInfo() {
            Address address = Address.of("06234", "서울특별시 강남구 테헤란로", "123호");

            assertThat(address.getZipCode()).isEqualTo("06234");
            assertThat(address.getAddress()).isEqualTo("서울특별시 강남구 테헤란로");
            assertThat(address.getAddressDetail()).isEqualTo("123호");
        }

        @Test
        @DisplayName("주소와 상세주소만으로 Address 생성")
        void createWithoutZipCode() {
            Address address = Address.of("서울특별시 강남구 테헤란로", "123호");

            assertThat(address.getZipCode()).isNull();
            assertThat(address.getAddress()).isEqualTo("서울특별시 강남구 테헤란로");
            assertThat(address.getAddressDetail()).isEqualTo("123호");
        }

        @Test
        @DisplayName("주소만으로 Address 생성")
        void createWithAddressOnly() {
            Address address = Address.of("서울특별시 강남구 테헤란로");

            assertThat(address.getZipCode()).isNull();
            assertThat(address.getAddress()).isEqualTo("서울특별시 강남구 테헤란로");
            assertThat(address.getAddressDetail()).isNull();
        }

        @Test
        @DisplayName("null 주소로 생성 시 예외 발생")
        void createWithNullAddress() {
            assertThatThrownBy(() -> Address.of("06234", null, "123호"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("주소는 필수입니다");
        }

        @Test
        @DisplayName("빈 주소로 생성 시 예외 발생")
        void createWithEmptyAddress() {
            assertThatThrownBy(() -> Address.of("06234", "  ", "123호"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("주소는 필수입니다");
        }
    }

    @Nested
    @DisplayName("getFullAddress")
    class FullAddress {

        @Test
        @DisplayName("전체 정보가 있는 경우 풀 주소 반환")
        void getFullAddressWithAllInfo() {
            Address address = Address.of("06234", "서울특별시 강남구 테헤란로", "123호");

            assertThat(address.getFullAddress()).isEqualTo("(06234) 서울특별시 강남구 테헤란로 123호");
        }

        @Test
        @DisplayName("우편번호가 없는 경우")
        void getFullAddressWithoutZipCode() {
            Address address = Address.of("서울특별시 강남구 테헤란로", "123호");

            assertThat(address.getFullAddress()).isEqualTo("서울특별시 강남구 테헤란로 123호");
        }

        @Test
        @DisplayName("상세주소가 없는 경우")
        void getFullAddressWithoutDetail() {
            Address address = Address.of("서울특별시 강남구 테헤란로");

            assertThat(address.getFullAddress()).isEqualTo("서울특별시 강남구 테헤란로");
        }
    }

    @Nested
    @DisplayName("Address 수정")
    class Modification {

        @Test
        @DisplayName("상세주소 변경")
        void changeDetail() {
            Address original = Address.of("06234", "서울특별시 강남구 테헤란로", "123호");

            Address changed = original.changeDetail("456호");

            assertThat(changed.getZipCode()).isEqualTo("06234");
            assertThat(changed.getAddress()).isEqualTo("서울특별시 강남구 테헤란로");
            assertThat(changed.getAddressDetail()).isEqualTo("456호");
            // 원본은 변경되지 않음 (불변객체)
            assertThat(original.getAddressDetail()).isEqualTo("123호");
        }

        @Test
        @DisplayName("우편번호 변경")
        void changeZipCode() {
            Address original = Address.of("06234", "서울특별시 강남구 테헤란로", "123호");

            Address changed = original.changeZipCode("12345");

            assertThat(changed.getZipCode()).isEqualTo("12345");
            assertThat(changed.getAddress()).isEqualTo("서울특별시 강남구 테헤란로");
            // 원본은 변경되지 않음
            assertThat(original.getZipCode()).isEqualTo("06234");
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode")
    class Equality {

        @Test
        @DisplayName("같은 값을 가진 Address는 동등함")
        void equalsWithSameValues() {
            Address address1 = Address.of("06234", "서울특별시 강남구 테헤란로", "123호");
            Address address2 = Address.of("06234", "서울특별시 강남구 테헤란로", "123호");

            assertThat(address1).isEqualTo(address2);
            assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 Address는 동등하지 않음")
        void notEqualsWithDifferentValues() {
            Address address1 = Address.of("06234", "서울특별시 강남구 테헤란로", "123호");
            Address address2 = Address.of("06234", "서울특별시 강남구 테헤란로", "456호");

            assertThat(address1).isNotEqualTo(address2);
        }
    }

    @Test
    @DisplayName("toString은 fullAddress를 반환")
    void toStringReturnsFullAddress() {
        Address address = Address.of("06234", "서울특별시 강남구 테헤란로", "123호");

        assertThat(address.toString()).isEqualTo("(06234) 서울특별시 강남구 테헤란로 123호");
    }
}