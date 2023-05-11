package com.example.controller;

import io.github.pixee.security.Filenames;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


import com.example.dao.ContactRepository;
import com.example.dao.MyOrderRepository;
import com.example.dao.UserRepository;
import com.example.entities.Contact;
import com.example.entities.MyOrder;
import com.example.entities.User;
import com.example.helper.Message;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;


@org.springframework.stereotype.Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private MyOrderRepository myOrderRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@ModelAttribute
	public void addCommonUser(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("username is : " + userName);

		User userByUserName = this.userRepository.getUserByUserName(userName);
		System.out.println(userByUserName);
		model.addAttribute("user", userByUserName);

	}

	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "User Dashboard");
		return "normal/userDashboard";
	}

	@GetMapping("/addContact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/addContact";
	}

	@PostMapping("/contactProcess")
	public String contactProcess(@ModelAttribute Contact contact, @RequestParam("image") MultipartFile file,
			Principal principal, HttpSession session) {
		// System.out.println(contact);
		try {
			String name = principal.getName();
			User userByUserName = this.userRepository.getUserByUserName(name);

			if (file.isEmpty()) {
				System.out.println("File is Empty");
				contact.setImage("3.jpeg");
			} else {
				contact.setImage(Filenames.toSimpleFileName(file.getOriginalFilename()));
				File UPLOAD_DIR = new ClassPathResource("static/images").getFile();
				Path filePath = Paths
						.get(UPLOAD_DIR.getAbsolutePath() + File.pathSeparator + Filenames.toSimpleFileName(file.getOriginalFilename()));
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("file upload successfully");

			}
			userByUserName.getContacts().add(contact);
			contact.setUser(userByUserName);
			this.userRepository.save(userByUserName);

			session.setAttribute("message", new Message("your contact added", "success"));

		} catch (Exception exception) {
			System.out.println("Exception is : " + exception.getMessage());
			exception.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong", "danger"));

		}

//		model.addAttribute("title","Add Contact");
//		model.addAttribute("contact", new Contact());
		return "normal/addContact";
	}

	@GetMapping("/showContact/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show Contacts");
//		String userName = principal.getName();
//		User userByUserName = this.userRepository.getUserByUserName(userName);
//		List<Contact> contacts = userByUserName.getContacts();

		String userName = principal.getName();
		User userByUserName = this.userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 2);
		Page<Contact> findContactsByUser = this.contactRepository.findContactsByUser(userByUserName.getId(), pageable);
		model.addAttribute("contacts", findContactsByUser);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", findContactsByUser.getTotalPages());
		return "normal/showContact";
	}

	@RequestMapping("/{contactId}/showContactDetails")
	public String showContactDetails(@PathVariable("contactId") Integer cId, Model model, Principal principal) {
		System.out.println("");
		Optional<Contact> findById = this.contactRepository.findById(cId);
		Contact contact = findById.get();

		String userName = principal.getName();
		User userByUserName = this.userRepository.getUserByUserName(userName);
		if (userByUserName.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getContactName());
		}

		return "normal/showContactDetails";
	}

	@GetMapping("/deleteContact/{contactId}")
	public String deleteContact(@PathVariable("contactId") Integer cId, Model model, HttpSession session,
			Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		// contact.setUser(null);
		// this.contactRepository.delete(contact);
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);

		System.out.println("DELETED");
		session.setAttribute("message", new Message("Contact Deleted successfully", "success"));
		return "redirect:/user/showContact/0";
	}

	@RequestMapping("/updateContactForm/{contactId}")
	public String updateForm(@PathVariable("contactId") Integer cId, Model model) {
		model.addAttribute("title", "Update Contact");
		Contact findByIdContact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact", findByIdContact);
		return "normal/updateContactForm";
	}

	@RequestMapping(value = "/contactUpdateProcess", method = RequestMethod.POST)
	public String contactUpdateProcess(@ModelAttribute Contact contact, @RequestParam("image") MultipartFile file,
			Model model, HttpSession session, Principal principal) {
		try {
			Contact oldContactDetails = this.contactRepository.findById(contact.getContactId()).get();
			if (!file.isEmpty()) {

				// delete old file
				File deleteFile = new ClassPathResource("static/images/").getFile();
				File deleteOldFile = new File(deleteFile, oldContactDetails.getImage());
				deleteOldFile.delete();

				// update new file
				File UPLOAD_DIR = new ClassPathResource("static/images/").getFile();
				Path filePath = Paths
						.get(UPLOAD_DIR.getAbsolutePath() + File.pathSeparator + Filenames.toSimpleFileName(file.getOriginalFilename()));
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(Filenames.toSimpleFileName(file.getOriginalFilename()));
			} else {
				contact.setImage(oldContactDetails.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("your contact upadated", "success"));

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return "redirect:/user/" + contact.getContactId() + "/showContactDetails";
	}

	@GetMapping("/profile")
	public String profilePage(Model model) {
		model.addAttribute("title", "Profile Page");
		return "normal/profilePage";
	}

	@GetMapping("/setting")
	public String openSettingForm(Model model) {
		model.addAttribute("title", "Settings ");
		return "normal/settings";
	}

	@PostMapping("/changePasswordProcess")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Model model, HttpSession session, Principal principal) {
		System.out.println("OLD Password " + oldPassword);
		System.out.println("NEW Password " + newPassword);

		String userName = principal.getName();

		User userByUserName = this.userRepository.getUserByUserName(userName);
		System.out.println("username is : " + userByUserName);

		if (this.bCryptPasswordEncoder.matches(oldPassword, userByUserName.getUserPassword())) {
			userByUserName.setUserPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(userByUserName);
			session.setAttribute("message", new Message("your Password upadated", "success"));
		} else {
			session.setAttribute("message", new Message("wrong old password", "error"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {

		int amt = Integer.parseInt(data.get("amount").toString());
		RazorpayClient client = new RazorpayClient("rzp_test_t5e5RZ3qae8f4V", "p5TPQX54YYqcKWpM7kGEhFZd");
		//new RazorpayClient(null, null)
		JSONObject ob = new JSONObject();
		ob.put("amount", amt * 100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_235425");
		Order order = client.Orders.create(ob);
		
		//Order order = client.orders.create(ob);
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(amt);
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setReceipt(order.get("receipt"));
		myOrder.setUser(userRepository.getUserByUserName(principal.getName()));
		myOrderRepository.save(myOrder);
		return order.toString();
	}
	
	@PostMapping("/update_order")
	@ResponseBody
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {
		MyOrder myOrder = myOrderRepository.fingByOrderId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		myOrderRepository.save(myOrder);
		return ResponseEntity.ok(Map.of("msg", "updated"));
	}

}
