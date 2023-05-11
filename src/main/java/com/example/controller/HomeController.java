package com.example.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dao.UserRepository;
import com.example.entities.User;
import com.example.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String homePage(Model model) {
		model.addAttribute("title", "Home - Smart contact management");
		return "homePage";
	}

	@RequestMapping("/about")
	public String aboutPage(Model model) {
		model.addAttribute("title", "About - Smart contact management");
		return "aboutPage";
	}

	@RequestMapping("/signup")
	public String signupPage(Model model) {
		model.addAttribute("title", "registration - Smart contact management");
		model.addAttribute("user", new User());
		return "signUpPage";
	}

	@RequestMapping(value = "/registrationProcess", method = RequestMethod.POST)
	public String userRegistration(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreed", defaultValue = "false") boolean agreed, Model model, HttpSession session) {
		try {
			if (!agreed) {
				System.out.println("You have not agreed terms and condition");
				throw new Exception("You have not agreed terms and condition");
			}
			if (result.hasErrors()) {
				System.out.println(result.toString());
				model.addAttribute("user", user);
				return "signUpPage";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("Brijesh.png");
			user.setUserPassword(bCryptPasswordEncoder.encode(user.getUserPassword()));

			System.out.println("user" + user);
			System.out.println("agreed" + agreed);

			User saveUser = this.userRepository.save(user);
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully registered", "alert-success"));
			return "signUpPage";

		} catch (Exception exception) {
			// TODO: handle exception
			exception.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message",
					new Message("Something went wrong !! " + exception.getMessage(), "alert-danger"));
			return "signUpPage";
		}
		
	}
	
	@RequestMapping("/Login")
	public String customeLogin(Model model)
	{
		model.addAttribute("title", "Login - Smart contact management");
		return "loginPage";
	}
}
