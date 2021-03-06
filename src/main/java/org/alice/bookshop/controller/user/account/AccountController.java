package org.alice.bookshop.controller.user.account;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alice.bookshop.model.Order;
import org.alice.bookshop.model.User;
import org.alice.bookshop.service.user.account.AccountService;
import org.alice.bookshop.service.utility.StorageFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller("uaAccountController")
public class AccountController {

	final static int PRIVILEGE_ADMIN = 1;

	@Autowired
	private AccountService accountService;

	@Autowired
	private StorageFileService sfSvc;

	@GetMapping("/login")
	public String login(Model model) {
		return "user/account/login";
	}

	@GetMapping("/login-success")
	public String loginSuccess(HttpSession session) {
		User user = accountService.getUser();
		session.setAttribute("user", user);
		Order cart = accountService.getCart(user);
		session.setAttribute("cart", cart);
		return "redirect:/home";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("user", new User());
		return "user/account/signup";
	}

	@PostMapping("/signup")
	public String signup(RedirectAttributes redirAttr, User user) {
		List<String> msgs = accountService.validateSignUpAccount(user);
		if (msgs.size() == 0) {
			accountService.signup(user);
			redirAttr.addFlashAttribute("user", user);
			return "redirect:/signup-success";
		} else {
			redirAttr.addFlashAttribute("msgs", msgs);
			return "redirect:/signup";
		}
	}

	@GetMapping("signup-success")
	public String signupSuccess(RedirectAttributes redirAttr) {
		return "user/account/signup-success";
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return "redirect:/home";
	}

	@GetMapping("/profiles/{id}")
	public String profile(Model model, @PathVariable int id) {
		model.addAttribute("user", accountService.getUserById(id));
		return "user/account/profile";
	}

	@GetMapping("/profiles/{id}/edit")
	public String editProfile(Model model, @PathVariable int id) {
		model.addAttribute("user", accountService.getUserById(id));
		return "/user/account/edit-profile";
	}

	@PostMapping("/profiles/{id}")
	public String profile(RedirectAttributes redirAttr, User user) {
		List<String> msgs = accountService.validateModifyProfile(user);
		if (msgs.size() == 0) {
			accountService.saveChangeProfile(user);
			redirAttr.addFlashAttribute("msg", "Change profile success");
			return "redirect:/profiles/" + user.getId();
		} else {
			redirAttr.addFlashAttribute("msgs", msgs);
			return "redirect:/profiles/" + user.getId() + "/edit";
		}
	}

	@GetMapping("/profiles/{id}/change-password")
	public String changePassword(Model model, @PathVariable int id) {
		model.addAttribute("user", accountService.getUserById(id));
		return "/user/account/change-password";
	}

	@PostMapping("/profiles/{id}/change-password")
	public String changePassword(RedirectAttributes redirAttr, User user) {
		boolean successd = accountService.validatePassword(user);
		if (successd) {
			accountService.saveChangePassword(user);
			redirAttr.addFlashAttribute("msg", "Change password success!");
			return "redirect:/profiles/" + user.getId();
		} else {
			redirAttr.addFlashAttribute("msg", "Password is not match!");
			return "redirect:/profiles/" + user.getId() + "/change-password";
		}
	}

	@RequestMapping(value = "/profiles/{id}/avatar", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public String changeAvatar(@PathVariable int id, @RequestParam MultipartFile file) {
		User user = accountService.userJpa.getOne(id);
		user.setImgURL("/images/user/" + sfSvc.storageFile(file, "user", user.getUsername()));
		accountService.userJpa.save(user);
		return "redirect:/profiles/" + id;
	}

}
