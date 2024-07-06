package com.whataburger.whataburgerproject.repository;

import com.whataburger.whataburgerproject.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {
    @PersistenceContext
    private EntityManager em;

    public void save(User user) {
        em.persist(user);
    }

    public User findUser(Long id) {
        return em.find(User.class, id);
    }

    public List<User> findAllUser() {
        return em.createQuery("select u from User u", User.class)
                .getResultList();
    }
    
    public List<User> findUserByName(String name) {
        return em.createQuery("select u from User u where u.name = :name", User.class)
                .setParameter("name", name)
                .getResultList();
    }
}
