package com.example.demo.service;

import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Users getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public Users createUser(Users user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already in use: " + user.getEmail());
        }
        if (userRepository.existsByMobile(user.getMobile())) {
            throw new RuntimeException("Mobile already in use: " + user.getMobile());
        }
        return userRepository.save(user);
    }

    public Users updateUser(Long id, Users updatedUser) {
        Users existing = getUserById(id);
        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());
        existing.setMobile(updatedUser.getMobile());
        existing.setPassword_hash(updatedUser.getPassword_hash());
        return userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        getUserById(id); // ensure exists
        userRepository.deleteById(id);
    }
}
