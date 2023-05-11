package com.example.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.dao.UserRepository;
import com.example.entities.User;

public class CusomeUserDetailsServiceImplementataion implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User userByUserName = userRepository.getUserByUserName(username);
		if(userByUserName == null)
		{
			throw new UsernameNotFoundException("Could not found user.");
		}
		
		CustomeUserDetails customeUserDetails = new CustomeUserDetails(userByUserName);
		return customeUserDetails;
	}

}
