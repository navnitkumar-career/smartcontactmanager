package com.example.dao;

import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entities.Contact;
import com.example.entities.User;

public interface ContactRepository extends JpaRepository<Contact,Integer> {
	
	@Query("from Contact as contact where contact.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param("userId") int userId ,Pageable  pageable);
	//@Query("from Contact as contact where contact.user.id =:userId")
	
	//public List<Contact> findByNameContainingAndUser(String contactName,User user);

}
