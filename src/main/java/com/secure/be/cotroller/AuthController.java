package com.secure.be.cotroller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.secure.be.dto.ERole;
import com.secure.be.dto.Role;
import com.secure.be.dto.User;
import com.secure.be.repository.RoleRepository;
import com.secure.be.repository.UserRepository;
import com.secure.be.security.payload.JwtResponse;
import com.secure.be.security.payload.MessageResponse;
import com.secure.be.security.service.JwtUtils;
import com.secure.be.security.service.UserDetailsImpl;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@ApiParam(value = "Enter your Username", required = true)
	@RequestParam("userName") String userName,
    @ApiParam(value = "Enter your password", required = true)
    @RequestParam(value = "password", required = true) String password) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(userName, password));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();		
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(new JwtResponse(jwt, 
												 userDetails.getId(), 
												 userDetails.getUsername(), 
												 userDetails.getEmail(), 
												 roles));
	}

	@ApiOperation(value = "Register new User with valid credentials")
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@ApiParam(value = "Username should be unique", required = true)
			@RequestParam("userName") String userName,
            @ApiParam(value = "The password should contains 1. Atlease one uppercase and one lowercase \n 2.should contain one number", required = true)
            @RequestParam(value = "password", required = true) String password,
            @ApiParam(value = "Please Enter your valid email id", required = true)
			@RequestParam("email") String email,
            @ApiParam(value = "Role for which you want to register . for e.g. Admin,User,Moderator etc.,", allowableValues = "ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR", required = true)
            @RequestParam(value = "role", required = true) String role) throws Exception {
		if (userRepository.existsByUsername(userName)) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(email)) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		User user = new User(userName, 
							 email,
							 encoder.encode(password));

		Role roles=null;

		if (role != null) {
			switch (role) {
			case "ROLE_ADMIN":
				roles = roleRepository.findByName(ERole.ROLE_ADMIN)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
				
				break;
			case "ROLE_MODERATOR":
				roles = roleRepository.findByName(ERole.ROLE_MODERATOR)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
				

				break;
			case "ROLE_USER":
				roles = roleRepository.findByName(ERole.ROLE_USER)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
				break;
			}
		} else {
			throw new Exception("Specified Role not found");
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
}