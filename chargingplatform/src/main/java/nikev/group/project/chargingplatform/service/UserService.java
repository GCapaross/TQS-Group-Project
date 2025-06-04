package nikev.group.project.chargingplatform.service;

import java.util.Optional;
import nikev.group.project.chargingplatform.DTOs.RegisterRequestDTO;
import nikev.group.project.chargingplatform.model.Role;
import nikev.group.project.chargingplatform.model.User;
import nikev.group.project.chargingplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User registerUser(RegisterRequestDTO registerRequest) {
        Optional<User> existingUser = userRepository.findByEmail(
            registerRequest.getEmail()
        );
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(registerRequest.getPassword());

        if (registerRequest.getAccountType().equals("user")) {
            System.out.println("Setting user role to USER");
            newUser.setRole(Role.USER);
        } else if (registerRequest.getAccountType().equals("operator")) {
            System.out.println("Setting user role to OPERATOR");
            newUser.setRole(Role.OPERATOR);
        }
        System.out.println("Saving new user: " + newUser.getEmail());
        return userRepository.save(newUser);
    }

    public User login(String email, String password) {
        User user = userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    public Long getUserIdByUsername(String username) {
        return userRepository
            .findByUsername(username)
            .map(User::getId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
