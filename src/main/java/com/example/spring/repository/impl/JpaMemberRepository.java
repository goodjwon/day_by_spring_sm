package com.example.spring.repository.impl;

import com.example.spring.entity.Member;
import com.example.spring.entity.MembershipType;
import com.example.spring.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class JpaMemberRepository implements MemberRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return em.createQuery("SELECT m FROM Member m ORDER BY m.joinDate DESC", Member.class)
                .getResultList();
    }

    @Override
    public Member save(Member member) {
        if (member.getId() == null) {
            em.persist(member);
            return member;
        } else {
            return em.merge(member);
        }
    }

    @Override
    public void deleteById(Long id) {
        Member member = em.find(Member.class, id);
        if (member != null) {
            em.remove(member);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {
        TypedQuery<Member> query = em.createQuery(
                "SELECT m FROM Member m WHERE m.email = :email", Member.class);
        query.setParameter("email", email);

        List<Member> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> findByMembershipType(MembershipType membershipType) {
        return em.createQuery(
                        "SELECT m FROM Member m WHERE m.membershipType = :membershipType ORDER BY m.joinDate DESC",
                        Member.class)
                .setParameter("membershipType", membershipType)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> findByNameContaining(String name) {
        return em.createQuery(
                        "SELECT m FROM Member m WHERE m.name LIKE :name ORDER BY m.name",
                        Member.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        Long count = em.createQuery(
                        "SELECT COUNT(m) FROM Member m WHERE m.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> findByMembershipTypeAndNameContaining(MembershipType membershipType, String name) {
        return em.createQuery(
                        "SELECT m FROM Member m WHERE m.membershipType = :membershipType AND m.name LIKE :name ORDER BY m.name",
                        Member.class)
                .setParameter("membershipType", membershipType)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> findByJoinDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return em.createQuery(
                        "SELECT m FROM Member m WHERE m.joinDate BETWEEN :startDate AND :endDate ORDER BY m.joinDate DESC",
                        Member.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }
}