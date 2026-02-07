package com.example.spring.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소를 나타내는 Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Address {

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "address_detail", length = 200)
    private String addressDetail;

    private Address(String zipCode, String address, String addressDetail) {
        validate(address);
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    public static Address of(String zipCode, String address, String addressDetail) {
        return new Address(zipCode, address, addressDetail);
    }

    public static Address of(String address, String addressDetail) {
        return new Address(null, address, addressDetail);
    }

    public static Address of(String address) {
        return new Address(null, address, null);
    }

    private void validate(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소는 필수입니다");
        }
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (zipCode != null && !zipCode.isBlank()) {
            sb.append("(").append(zipCode).append(") ");
        }
        sb.append(address);
        if (addressDetail != null && !addressDetail.isBlank()) {
            sb.append(" ").append(addressDetail);
        }
        return sb.toString();
    }

    public Address changeDetail(String newAddressDetail) {
        return new Address(this.zipCode, this.address, newAddressDetail);
    }

    public Address changeZipCode(String newZipCode) {
        return new Address(newZipCode, this.address, this.addressDetail);
    }

    @Override
    public String toString() {
        return getFullAddress();
    }
}
