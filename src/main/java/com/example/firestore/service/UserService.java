package com.example.firestore.service;

import com.example.firestore.model.User;
import com.example.firestore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service class for User business logic.
 * Provides high-level operations and business rules for user management.
 */
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    public UserService() {
        this.userRepository = new UserRepository();
        logger.info("UserService initialized");
    }
    
    // Constructor for testing with dependency injection
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger.info("UserService initialized with injected repository");
    }
    
    // ============ CREATE OPERATIONS ============
    
    /**
     * Create a new user with validation and duplicate email check
     */
    public User createUser(String name, String email, Integer age) {
        try {
            logger.info("Creating user: name={}, email={}, age={}", name, email, age);
            
            // Business validation
            validateUserInput(name, email, age);
            
            // Check for duplicate email
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("User with email '" + email + "' already exists");
            }
            
            User user = new User(name, email, age);
            User createdUser = userRepository.createUser(user);
            
            logger.info("User created successfully with ID: {}", createdUser.getIdAsString());
            return createdUser;
            
        } catch (Exception e) {
            logger.error("Failed to create user: name={}, email={}, age={}", name, email, age, e);
            throw e;
        }
    }
    
    /**
     * Create multiple users with validation
     */
    public List<User> createUsers(List<User> users) {
        try {
            logger.info("Creating {} users", users.size());
            
            if (users == null || users.isEmpty()) {
                throw new IllegalArgumentException("Users list cannot be null or empty");
            }
            
            // Validate all users and check for duplicates
            for (User user : users) {
                validateUserInput(user.getName(), user.getEmail(), user.getAge());
                
                if (userRepository.existsByEmail(user.getEmail())) {
                    throw new IllegalArgumentException("User with email '" + user.getEmail() + "' already exists");
                }
            }
            
            List<User> createdUsers = userRepository.createUsers(users);
            logger.info("Successfully created {} users", createdUsers.size());
            
            return createdUsers;
            
        } catch (Exception e) {
            logger.error("Failed to create users", e);
            throw e;
        }
    }
    
    // ============ READ OPERATIONS ============
    
    /**
     * Get user by ID
     */
    public Optional<User> getUserById(String id) {
        try {
            logger.debug("Getting user by ID: {}", id);
            
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            
            return userRepository.findById(id);
            
        } catch (Exception e) {
            logger.error("Failed to get user by ID: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        try {
            logger.debug("Getting user by email: {}", email);
            
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }
            
            return userRepository.findByEmail(email);
            
        } catch (Exception e) {
            logger.error("Failed to get user by email: {}", email, e);
            throw e;
        }
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        try {
            logger.debug("Getting all users");
            List<User> users = userRepository.findAll();
            logger.info("Retrieved {} users", users.size());
            return users;
            
        } catch (Exception e) {
            logger.error("Failed to get all users", e);
            throw e;
        }
    }
    
    /**
     * Get users by age range with business validation
     */
    public List<User> getUsersByAgeRange(int minAge, int maxAge) {
        try {
            logger.debug("Getting users by age range: {}-{}", minAge, maxAge);
            
            if (minAge < 0 || maxAge < 0) {
                throw new IllegalArgumentException("Age cannot be negative");
            }
            
            if (minAge > maxAge) {
                throw new IllegalArgumentException("Minimum age cannot be greater than maximum age");
            }
            
            if (maxAge > 150) {
                throw new IllegalArgumentException("Maximum age seems unrealistic (over 150)");
            }
            
            List<User> users = userRepository.findByAgeRange(minAge, maxAge);
            logger.info("Found {} users in age range {}-{}", users.size(), minAge, maxAge);
            
            return users;
            
        } catch (Exception e) {
            logger.error("Failed to get users by age range: {}-{}", minAge, maxAge, e);
            throw e;
        }
    }
    
    /**
     * Search users by name
     */
    public List<User> searchUsersByName(String namePattern) {
        try {
            logger.debug("Searching users by name pattern: {}", namePattern);
            
            if (namePattern == null || namePattern.trim().isEmpty()) {
                throw new IllegalArgumentException("Name pattern cannot be null or empty");
            }
            
            if (namePattern.length() < 2) {
                throw new IllegalArgumentException("Name pattern must be at least 2 characters long");
            }
            
            List<User> users = userRepository.findByNameContaining(namePattern);
            logger.info("Found {} users matching name pattern: {}", users.size(), namePattern);
            
            return users;
            
        } catch (Exception e) {
            logger.error("Failed to search users by name: {}", namePattern, e);
            throw e;
        }
    }
    
    /**
     * Get user statistics
     */
    public UserStatistics getUserStatistics() {
        try {
            logger.debug("Getting user statistics");
            
            long totalUsers = userRepository.countUsers();
            List<User> allUsers = userRepository.findAll();
            
            UserStatistics stats = new UserStatistics();
            stats.setTotalUsers(totalUsers);
            
            if (!allUsers.isEmpty()) {
                // Calculate age statistics
                double avgAge = allUsers.stream().mapToInt(User::getAge).average().orElse(0.0);
                int minAge = allUsers.stream().mapToInt(User::getAge).min().orElse(0);
                int maxAge = allUsers.stream().mapToInt(User::getAge).max().orElse(0);
                
                stats.setAverageAge(avgAge);
                stats.setMinAge(minAge);
                stats.setMaxAge(maxAge);
            }
            
            logger.info("User statistics: total={}, avgAge={}, minAge={}, maxAge={}", 
                       stats.getTotalUsers(), stats.getAverageAge(), stats.getMinAge(), stats.getMaxAge());
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Failed to get user statistics", e);
            throw e;
        }
    }
    
    // ============ UPDATE OPERATIONS ============
    
    /**
     * Update user with business validation
     */
    public boolean updateUser(String id, String name, String email, Integer age) {
        try {
            logger.info("Updating user: id={}, name={}, email={}, age={}", id, name, email, age);
            
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            
            // Check if user exists
            Optional<User> existingUser = userRepository.findById(id);
            if (!existingUser.isPresent()) {
                throw new IllegalArgumentException("User with ID '" + id + "' not found");
            }
            
            // Validate new data
            validateUserInput(name, email, age);
            
            // Check for duplicate email (excluding current user)
            Optional<User> userWithEmail = userRepository.findByEmail(email);
            if (userWithEmail.isPresent() && !userWithEmail.get().getIdAsString().equals(id)) {
                throw new IllegalArgumentException("Another user with email '" + email + "' already exists");
            }
            
            User updatedUser = new User(name, email, age);
            boolean success = userRepository.updateUser(id, updatedUser);
            
            if (success) {
                logger.info("User updated successfully: {}", id);
            } else {
                logger.warn("User update failed: {}", id);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to update user: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Update user email with validation
     */
    public boolean updateUserEmail(String id, String newEmail) {
        try {
            logger.info("Updating user email: id={}, newEmail={}", id, newEmail);
            
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            
            validateEmail(newEmail);
            
            // Check if user exists
            if (!userRepository.existsById(id)) {
                throw new IllegalArgumentException("User with ID '" + id + "' not found");
            }
            
            // Check for duplicate email
            Optional<User> userWithEmail = userRepository.findByEmail(newEmail);
            if (userWithEmail.isPresent() && !userWithEmail.get().getIdAsString().equals(id)) {
                throw new IllegalArgumentException("Another user with email '" + newEmail + "' already exists");
            }
            
            boolean success = userRepository.updateUserEmail(id, newEmail);
            
            if (success) {
                logger.info("User email updated successfully: {}", id);
            } else {
                logger.warn("User email update failed: {}", id);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to update user email: {}", id, e);
            throw e;
        }
    }
    
    // ============ DELETE OPERATIONS ============
    
    /**
     * Delete user by ID
     */
    public boolean deleteUser(String id) {
        try {
            logger.info("Deleting user: {}", id);
            
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            
            // Check if user exists
            if (!userRepository.existsById(id)) {
                throw new IllegalArgumentException("User with ID '" + id + "' not found");
            }
            
            boolean success = userRepository.deleteUser(id);
            
            if (success) {
                logger.info("User deleted successfully: {}", id);
            } else {
                logger.warn("User deletion failed: {}", id);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to delete user: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Delete users by age range with confirmation
     */
    public long deleteUsersByAgeRange(int minAge, int maxAge, boolean confirm) {
        try {
            logger.info("Deleting users by age range: {}-{}, confirmed={}", minAge, maxAge, confirm);
            
            if (!confirm) {
                throw new IllegalArgumentException("Deletion not confirmed. This operation is irreversible.");
            }
            
            if (minAge < 0 || maxAge < 0 || minAge > maxAge) {
                throw new IllegalArgumentException("Invalid age range");
            }
            
            // Count how many users will be affected
            List<User> usersToDelete = userRepository.findByAgeRange(minAge, maxAge);
            logger.warn("About to delete {} users in age range {}-{}", usersToDelete.size(), minAge, maxAge);
            
            long deletedCount = userRepository.deleteUsersByAgeRange(minAge, maxAge);
            logger.info("Deleted {} users in age range {}-{}", deletedCount, minAge, maxAge);
            
            return deletedCount;
            
        } catch (Exception e) {
            logger.error("Failed to delete users by age range: {}-{}", minAge, maxAge, e);
            throw e;
        }
    }
    
    // ============ UTILITY METHODS ============
    
    /**
     * Check if user exists
     */
    public boolean userExists(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return false;
            }
            
            return userRepository.existsById(id);
            
        } catch (Exception e) {
            logger.error("Failed to check if user exists: {}", id, e);
            return false;
        }
    }
    
    /**
     * Check if email is already taken
     */
    public boolean emailExists(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            
            return userRepository.existsByEmail(email);
            
        } catch (Exception e) {
            logger.error("Failed to check if email exists: {}", email, e);
            return false;
        }
    }
    
    // ============ VALIDATION METHODS ============
    
    /**
     * Validate user input data
     */
    private void validateUserInput(String name, String email, Integer age) {
        validateName(name);
        validateEmail(email);
        validateAge(age);
    }
    
    /**
     * Validate name
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        if (name.length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters long");
        }
        
        if (name.length() > 100) {
            throw new IllegalArgumentException("Name cannot be longer than 100 characters");
        }
        
        // Check for valid characters (letters, spaces, hyphens, apostrophes)
        if (!name.matches("^[a-zA-Z\\s\\-']+$")) {
            throw new IllegalArgumentException("Name can only contain letters, spaces, hyphens, and apostrophes");
        }
    }
    
    /**
     * Validate email
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        if (email.length() > 254) {
            throw new IllegalArgumentException("Email cannot be longer than 254 characters");
        }
        
        // Basic email validation
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    /**
     * Validate age
     */
    private void validateAge(Integer age) {
        if (age == null) {
            throw new IllegalArgumentException("Age cannot be null");
        }
        
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        
        if (age > 150) {
            throw new IllegalArgumentException("Age cannot be greater than 150");
        }
    }
    
    // ============ INNER CLASSES ============
    
    /**
     * Class to hold user statistics
     */
    public static class UserStatistics {
        private long totalUsers;
        private double averageAge;
        private int minAge;
        private int maxAge;
        
        // Getters and setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        
        public double getAverageAge() { return averageAge; }
        public void setAverageAge(double averageAge) { this.averageAge = averageAge; }
        
        public int getMinAge() { return minAge; }
        public void setMinAge(int minAge) { this.minAge = minAge; }
        
        public int getMaxAge() { return maxAge; }
        public void setMaxAge(int maxAge) { this.maxAge = maxAge; }
        
        @Override
        public String toString() {
            return String.format("UserStatistics{totalUsers=%d, averageAge=%.1f, minAge=%d, maxAge=%d}", 
                               totalUsers, averageAge, minAge, maxAge);
        }
    }
}
