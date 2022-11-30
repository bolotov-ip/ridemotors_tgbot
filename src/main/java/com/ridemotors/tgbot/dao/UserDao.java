package com.ridemotors.tgbot.dao;

import com.ridemotors.tgbot.model.Product;
import com.ridemotors.tgbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserDao extends JpaRepository<User, Long> {
    public List<User> findByRoleContainingIgnoreCase(String role);

    public User findByUserName(String userName);
}
