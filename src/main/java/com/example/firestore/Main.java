package com.example.firestore;

import com.example.firestore.config.FirestoreConfig;
import com.example.firestore.model.User;
import com.example.firestore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Main class demonstrating CRUD operations with Firestore using MongoDB API.
 * This class provides a comprehensive demonstration of all functionality.
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final UserService userService = new UserService();
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        logger.info("Starting Java Firestore CRUD Application");
        
        try {
            // Test connection first
            if (!testConnection()) {
                logger.error("Failed to connect to Firestore. Please check your configuration.");
                return;
            }
            
            // Run demonstration
            runCrudDemo();
            
            // Interactive mode (optional)
            if (askUserChoice("Would you like to run interactive mode?")) {
                runInteractiveMode();
            }
            
        } catch (Exception e) {
            logger.error("Application error", e);
        } finally {
            cleanup();
        }
        
        logger.info("Application finished");
    }
    
    /**
     * Test the connection to Firestore
     */
    private static boolean testConnection() {
        try {
            System.out.println("\n=== Testing Firestore Connection ===");
            FirestoreConfig config = FirestoreConfig.getInstance();
            boolean connected = config.testConnection();
            
            if (connected) {
                System.out.println("✅ Successfully connected to Firestore!");
                logger.info("Firestore connection test successful");
            } else {
                System.out.println("❌ Failed to connect to Firestore");
                logger.error("Firestore connection test failed");
            }
            
            return connected;
            
        } catch (Exception e) {
            System.out.println("❌ Connection test failed: " + e.getMessage());
            logger.error("Connection test error", e);
            return false;
        }
    }
    
    /**
     * Run comprehensive CRUD demonstration
     */
    private static void runCrudDemo() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("    FIRESTORE CRUD OPERATIONS DEMO");
        System.out.println("=".repeat(50));
        
        try {
            // Clean slate - remove existing demo users
            cleanupDemoUsers();
            
            // CREATE operations
            demonstrateCreateOperations();
            
            // READ operations
            demonstrateReadOperations();
            
            // UPDATE operations
            demonstrateUpdateOperations();
            
            // DELETE operations
            demonstrateDeleteOperations();
            
            // Statistics
            demonstrateStatistics();
            
        } catch (Exception e) {
            logger.error("Error during CRUD demonstration", e);
            System.out.println("❌ Demo failed: " + e.getMessage());
        }
    }
    
    /**
     * Demonstrate CREATE operations
     */
    private static void demonstrateCreateOperations() {
        System.out.println("\n--- CREATE OPERATIONS ---");
        
        try {
            // Create individual users
            System.out.println("Creating individual users...");
            
            User user1 = userService.createUser("John Doe", "john.doe@example.com", 30);
            System.out.println("✅ Created: " + user1);
            
            User user2 = userService.createUser("Jane Smith", "jane.smith@example.com", 25);
            System.out.println("✅ Created: " + user2);
            
            User user3 = userService.createUser("Bob Johnson", "bob.johnson@example.com", 35);
            System.out.println("✅ Created: " + user3);
            
            // Create multiple users at once
            System.out.println("\nCreating multiple users in batch...");
            List<User> batchUsers = Arrays.asList(
                new User("Alice Wilson", "alice.wilson@example.com", 28),
                new User("Charlie Brown", "charlie.brown@example.com", 42),
                new User("Diana Prince", "diana.prince@example.com", 29)
            );
            
            List<User> createdUsers = userService.createUsers(batchUsers);
            System.out.println("✅ Batch created " + createdUsers.size() + " users");
            
            // Demonstrate validation error
            System.out.println("\nTesting validation (this should fail)...");
            try {
                userService.createUser("", "invalid-email", -5);
            } catch (IllegalArgumentException e) {
                System.out.println("✅ Validation working: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Create operation failed: " + e.getMessage());
            logger.error("Create operation error", e);
        }
    }
    
    /**
     * Demonstrate READ operations
     */
    private static void demonstrateReadOperations() {
        System.out.println("\n--- READ OPERATIONS ---");
        
        try {
            // Get all users
            System.out.println("Retrieving all users...");
            List<User> allUsers = userService.getAllUsers();
            System.out.println("✅ Found " + allUsers.size() + " users:");
            allUsers.forEach(user -> System.out.println("   " + user));
            
            if (!allUsers.isEmpty()) {
                // Get user by ID
                User firstUser = allUsers.get(0);
                String userId = firstUser.getIdAsString();
                System.out.println("\nFinding user by ID: " + userId);
                Optional<User> foundUser = userService.getUserById(userId);
                if (foundUser.isPresent()) {
                    System.out.println("✅ Found: " + foundUser.get());
                } else {
                    System.out.println("❌ User not found");
                }
                
                // Get user by email
                String email = firstUser.getEmail();
                System.out.println("\nFinding user by email: " + email);
                Optional<User> userByEmail = userService.getUserByEmail(email);
                if (userByEmail.isPresent()) {
                    System.out.println("✅ Found: " + userByEmail.get());
                } else {
                    System.out.println("❌ User not found");
                }
            }
            
            // Search by age range
            System.out.println("\nFinding users aged 25-35...");
            List<User> usersInRange = userService.getUsersByAgeRange(25, 35);
            System.out.println("✅ Found " + usersInRange.size() + " users in age range:");
            usersInRange.forEach(user -> System.out.println("   " + user.getName() + " (age " + user.getAge() + ")"));
            
            // Search by name pattern
            System.out.println("\nSearching for users with 'Jo' in name...");
            List<User> usersWithJo = userService.searchUsersByName("Jo");
            System.out.println("✅ Found " + usersWithJo.size() + " users:");
            usersWithJo.forEach(user -> System.out.println("   " + user.getName()));
            
        } catch (Exception e) {
            System.out.println("❌ Read operation failed: " + e.getMessage());
            logger.error("Read operation error", e);
        }
    }
    
    /**
     * Demonstrate UPDATE operations
     */
    private static void demonstrateUpdateOperations() {
        System.out.println("\n--- UPDATE OPERATIONS ---");
        
        try {
            // Get a user to update
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No users available for update demo");
                return;
            }
            
            User userToUpdate = users.get(0);
            String userId = userToUpdate.getIdAsString();
            
            System.out.println("Updating user: " + userToUpdate.getName());
            System.out.println("Before: " + userToUpdate);
            
            // Update full user information
            boolean updateSuccess = userService.updateUser(
                userId, 
                "John Updated Doe", 
                "john.updated@example.com", 
                31
            );
            
            if (updateSuccess) {
                System.out.println("✅ User updated successfully");
                
                // Retrieve updated user to show changes
                Optional<User> updatedUser = userService.getUserById(userId);
                if (updatedUser.isPresent()) {
                    System.out.println("After: " + updatedUser.get());
                }
            } else {
                System.out.println("❌ User update failed");
            }
            
            // Update only email
            System.out.println("\nUpdating only email...");
            boolean emailUpdateSuccess = userService.updateUserEmail(userId, "john.newest@example.com");
            if (emailUpdateSuccess) {
                System.out.println("✅ Email updated successfully");
            } else {
                System.out.println("❌ Email update failed");
            }
            
            // Test duplicate email validation
            if (users.size() > 1) {
                System.out.println("\nTesting duplicate email validation...");
                try {
                    userService.updateUserEmail(userId, users.get(1).getEmail());
                } catch (IllegalArgumentException e) {
                    System.out.println("✅ Duplicate email validation working: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Update operation failed: " + e.getMessage());
            logger.error("Update operation error", e);
        }
    }
    
    /**
     * Demonstrate DELETE operations
     */
    private static void demonstrateDeleteOperations() {
        System.out.println("\n--- DELETE OPERATIONS ---");
        
        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No users available for delete demo");
                return;
            }
            
            // Delete individual user
            User userToDelete = users.get(users.size() - 1); // Delete last user
            String userId = userToDelete.getIdAsString();
            
            System.out.println("Deleting user: " + userToDelete.getName() + " (ID: " + userId + ")");
            boolean deleteSuccess = userService.deleteUser(userId);
            
            if (deleteSuccess) {
                System.out.println("✅ User deleted successfully");
                
                // Verify deletion
                boolean exists = userService.userExists(userId);
                System.out.println("User still exists? " + exists);
            } else {
                System.out.println("❌ User deletion failed");
            }
            
            // Demonstrate delete by age range (only if user confirms)
            System.out.println("\nDemo: Delete users by age range (this will be skipped for safety)");
            System.out.println("In a real scenario, you could delete users aged 40+ like this:");
            System.out.println("long deletedCount = userService.deleteUsersByAgeRange(40, 150, true);");
            
        } catch (Exception e) {
            System.out.println("❌ Delete operation failed: " + e.getMessage());
            logger.error("Delete operation error", e);
        }
    }
    
    /**
     * Demonstrate statistics functionality
     */
    private static void demonstrateStatistics() {
        System.out.println("\n--- USER STATISTICS ---");
        
        try {
            UserService.UserStatistics stats = userService.getUserStatistics();
            System.out.println("✅ " + stats);
            
        } catch (Exception e) {
            System.out.println("❌ Statistics failed: " + e.getMessage());
            logger.error("Statistics error", e);
        }
    }
    
    /**
     * Interactive mode for manual testing
     */
    private static void runInteractiveMode() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("           INTERACTIVE MODE");
        System.out.println("=".repeat(50));
        
        while (true) {
            System.out.println("\nChoose an operation:");
            System.out.println("1. Create User");
            System.out.println("2. Find User by ID");
            System.out.println("3. Find User by Email");
            System.out.println("4. List All Users");
            System.out.println("5. Update User");
            System.out.println("6. Delete User");
            System.out.println("7. Show Statistics");
            System.out.println("0. Exit");
            
            System.out.print("Enter choice (0-7): ");
            String choice = scanner.nextLine().trim();
            
            try {
                switch (choice) {
                    case "1": interactiveCreateUser(); break;
                    case "2": interactiveFindById(); break;
                    case "3": interactiveFindByEmail(); break;
                    case "4": interactiveListAll(); break;
                    case "5": interactiveUpdateUser(); break;
                    case "6": interactiveDeleteUser(); break;
                    case "7": interactiveShowStats(); break;
                    case "0": 
                        System.out.println("Exiting interactive mode...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("❌ Operation failed: " + e.getMessage());
            }
        }
    }
    
    // Interactive mode helper methods
    private static void interactiveCreateUser() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Enter age: ");
        String ageStr = scanner.nextLine().trim();
        
        try {
            int age = Integer.parseInt(ageStr);
            User user = userService.createUser(name, email, age);
            System.out.println("✅ User created: " + user);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid age format");
        }
    }
    
    private static void interactiveFindById() {
        System.out.print("Enter user ID: ");
        String id = scanner.nextLine().trim();
        
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            System.out.println("✅ Found: " + user.get());
        } else {
            System.out.println("❌ User not found");
        }
    }
    
    private static void interactiveFindByEmail() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            System.out.println("✅ Found: " + user.get());
        } else {
            System.out.println("❌ User not found");
        }
    }
    
    private static void interactiveListAll() {
        List<User> users = userService.getAllUsers();
        System.out.println("✅ Found " + users.size() + " users:");
        users.forEach(user -> System.out.println("   " + user));
    }
    
    private static void interactiveUpdateUser() {
        System.out.print("Enter user ID to update: ");
        String id = scanner.nextLine().trim();
        
        Optional<User> existingUser = userService.getUserById(id);
        if (!existingUser.isPresent()) {
            System.out.println("❌ User not found");
            return;
        }
        
        System.out.println("Current user: " + existingUser.get());
        
        System.out.print("Enter new name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter new email: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Enter new age: ");
        String ageStr = scanner.nextLine().trim();
        
        try {
            int age = Integer.parseInt(ageStr);
            boolean success = userService.updateUser(id, name, email, age);
            if (success) {
                System.out.println("✅ User updated successfully");
            } else {
                System.out.println("❌ Update failed");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid age format");
        }
    }
    
    private static void interactiveDeleteUser() {
        System.out.print("Enter user ID to delete: ");
        String id = scanner.nextLine().trim();
        
        Optional<User> user = userService.getUserById(id);
        if (!user.isPresent()) {
            System.out.println("❌ User not found");
            return;
        }
        
        System.out.println("User to delete: " + user.get());
        System.out.print("Are you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("yes")) {
            boolean success = userService.deleteUser(id);
            if (success) {
                System.out.println("✅ User deleted successfully");
            } else {
                System.out.println("❌ Delete failed");
            }
        } else {
            System.out.println("Delete cancelled");
        }
    }
    
    private static void interactiveShowStats() {
        UserService.UserStatistics stats = userService.getUserStatistics();
        System.out.println("✅ " + stats);
    }
    
    // Utility methods
    private static boolean askUserChoice(String question) {
        System.out.print(question + " (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }
    
    private static void cleanupDemoUsers() {
        try {
            // This method could clean up previous demo data if needed
            // For now, we'll just log that we're starting fresh
            logger.info("Starting CRUD demonstration with fresh data");
        } catch (Exception e) {
            logger.warn("Error during cleanup", e);
        }
    }
    
    private static void cleanup() {
        try {
            // Close scanner
            scanner.close();
            
            // Shutdown Firestore configuration
            FirestoreConfig.shutdown();
            
            logger.info("Application cleanup completed");
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
        }
    }
}
