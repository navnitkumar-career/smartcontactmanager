package com.example.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	@Query("select user from User user where user.userEmail = :userEmail ")
	public User getUserByUserName(@Param("userEmail") String email);
}
