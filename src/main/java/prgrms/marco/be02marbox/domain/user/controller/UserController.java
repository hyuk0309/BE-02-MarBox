package prgrms.marco.be02marbox.domain.user.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import prgrms.marco.be02marbox.domain.user.dto.RequestSignInUser;
import prgrms.marco.be02marbox.domain.user.dto.RequestSignUpUser;
import prgrms.marco.be02marbox.domain.user.dto.ResponseSignUpUser;
import prgrms.marco.be02marbox.domain.user.jwt.JwtAuthentication;
import prgrms.marco.be02marbox.domain.user.jwt.JwtAuthenticationToken;
import prgrms.marco.be02marbox.domain.user.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;

	public UserController(UserService userService, AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
	}

	@PostMapping("/sign-up")
	public ResponseEntity<ResponseSignUpUser> signUp(
		@Validated @RequestBody RequestSignUpUser userSignUpReq) {

		Long userId = userService.create(
			userSignUpReq.email(),
			userSignUpReq.password(),
			userSignUpReq.name(),
			userSignUpReq.role());

		return ResponseEntity
			.created(URI.create("/users/sign-in"))
			.body(new ResponseSignUpUser(userId));
	}

	@PostMapping("/sign-in")
	public ResponseEntity<Void> signIn(
		@Validated @RequestBody RequestSignInUser requestSignInUser) {
		JwtAuthenticationToken authToken = new JwtAuthenticationToken(
			requestSignInUser.email(), requestSignInUser.password());
		Authentication resultToken = authenticationManager.authenticate(authToken);
		JwtAuthentication principal = (JwtAuthentication)resultToken.getPrincipal();
		return ResponseEntity
			.noContent()
			.header("token", principal.getToken())
			.build();
	}
}
