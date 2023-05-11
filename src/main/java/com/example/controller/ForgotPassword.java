package com.example.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dao.UserRepository;
import com.example.entities.User;
import com.example.helper.Message;
import com.example.service.EmailService;
import java.util.Random;


@Controller
public class ForgotPassword {
	
	Random random = new Random(1000);

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@RequestMapping("/forgotEmail")
	public String openEmailForm(Model model) {
		model.addAttribute("title", "forgot - Contact Manager");
		return "forgotEmailForm.html";
	}
	
	@RequestMapping(value = "/send-otp", method = RequestMethod.POST)
	public String sendOTP(Model model, @RequestParam("email") String email, HttpSession session) {
		model.addAttribute("title", "forgot - Contact Manager");
		User userByUserName = userRepository.getUserByUserName(email);
		if (userByUserName == null) {
			session.setAttribute("message", "User does not exits with this email !!");
			return "forgotEmailForm";
		} else {
			int otp = random.nextInt(999999);
			String to = email;
			String subject = "OTP From SCM";
			String message = "<div style='border:1px solid #e2e2e2;padding:20px'> <h1><b> OTP = " + otp + "</b></h1></div>";
			//String message = "<h1> OTP = " + otp + "</h1>";
			boolean flag = emailService.sendEmail(subject, message,to);
			if (flag) {
				session.setAttribute("otp", otp);
				session.setAttribute("email", email);
				return "verify_otp";
			} else {
				session.setAttribute("message", "Check your email id !!");
				return "forgotEmailForm";
			}
		}
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession session) {
		Integer myOtp = (int) session.getAttribute("otp");
		if (myOtp.equals(otp)) {
			return "password_change_form";

		} else {
			session.setAttribute("message", "You have entered wrong otp!!");
			return "verify_otp";
		}

	}
	
	@PostMapping("/change-password")
	public String changePassword(HttpSession session, @RequestParam("newPassword") String newPassword,
			@RequestParam("confirmPassword") String confirmPassword) {
		String email = (String) session.getAttribute("email");
		User user = userRepository.getUserByUserName(email);
		if (newPassword.contentEquals(confirmPassword)) {
			user.setUserPassword(bCryptPasswordEncoder.encode(newPassword));
			//user.set(bCryptPasswordEncoder.encode(newPassword));
			userRepository.save(user);
			session.setAttribute("message", new Message("Your Password is Successfully Changed...", "success"));
			return "redirect:/signin?change=password changed successfully...";
		} else {
			session.setAttribute("message", new Message("Please Enter Correct Confirm Password !!", "danger"));
			return "password_change_form";
		}

	}
}
