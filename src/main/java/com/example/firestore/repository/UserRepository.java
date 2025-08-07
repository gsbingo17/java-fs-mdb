package com.example.firestore.repository;

import com.example.firestore.config.FirestoreConfig;
import com.example.firestore.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

/**
 * Repository class for User CRUD operations using MongoDB driver with Firestore.
 * Provides data access layer with comprehensive CRUD functionality.
 */
public class UserRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    
    private final MongoCollection<User> userCollection;
    private final FirestoreConfig firestoreConfig;
    
    public UserRepository() {
        this.firestoreConfig = FirestoreConfig.getInstance();
        MongoDatabase database = firestoreConfig.getDatabase();
        String collectionName = firestoreConfig.getProperty("app.collection.name", "users");
        this.userCollection = database.getCollection(collectionName, User.class);
        
        logger.info("UserRepository initialized with collection: {}", collectionName);
    }
    
    // ============ CREATE OPERATIONS ============
    
    /**
     * Insert a single user into the collection
     */
    public User createUser(User user) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            
            if (!user.isValid()) {
                throw new IllegalArgumentException("User data is not valid");
            }
            
            user.updateTimestamp();
            InsertOneResult result = userCollection.insertOne(user);
            
            if (result.wasAcknowledged() && result.getInsertedId() != null) {
                user.setId(result.getInsertedId().asObjectId().getValue());
                logger.info("User created successfully with ID: {}", user.getIdAsString());
                return user;
            } else {
                throw new RuntimeException("Failed to create user");
            }
            
        } catch (Exception e) {
            logger.error("Error creating user: {}", user, e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    /**
     * Insert multiple users in bulk
     */
    public List<User> createUsers(List<User> users) {
        try {
            if (users == null || users.isEmpty()) {
                throw new IllegalArgumentException("Users list cannot be null or empty");
            }
            
            // Validate all users
            for (User user : users) {
                if (!user.isValid()) {
                    throw new IllegalArgumentException("Invalid user data: " + user);
                }
                user.updateTimestamp();
            }
            
            InsertManyResult result = userCollection.insertMany(users);
            
            if (result.wasAcknowledged()) {
                logger.info("Successfully created {} users", result.getInsertedIds().size());
                return users;
            } else {
                throw new RuntimeException("Failed to create users");
            }
            
        } catch (Exception e) {
            logger.error("Error creating users", e);
            throw new RuntimeException("Failed to create users", e);
        }
    }
    
    // ============ READ OPERATIONS ============
    
    /**
     * Find user by ID
     */
    public Optional<User> findById(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return Optional.empty();
            }
            
            ObjectId objectId = new ObjectId(id);
            User user = userCollection.find(eq("_id", objectId)).first();
            
            if (user != null) {
                logger.debug("Found user with ID: {}", id);
                return Optional.of(user);
            } else {
                logger.debug("No user found with ID: {}", id);
                return Optional.empty();
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid ObjectId format: {}", id);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", id, e);
            throw new RuntimeException("Failed to find user by ID", e);
        }
    }
    
    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Optional.empty();
            }
            
            User user = userCollection.find(eq("email", email)).first();
            
            if (user != null) {
                logger.debug("Found user with email: {}", email);
                return Optional.of(user);
            } else {
                logger.debug("No user found with email: {}", email);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new RuntimeException("Failed to find user by email", e);
        }
    }
    
    /**
     * Find all users
     */
    public List<User> findAll() {
        try {
            List<User> users = new ArrayList<>();
            userCollection.find().into(users);
            
            logger.debug("Found {} users", users.size());
            return users;
            
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new RuntimeException("Failed to find all users", e);
        }
    }
    
    /**
     * Find users by age range
     */
    public List<User> findByAgeRange(int minAge, int maxAge) {
        try {
            if (minAge < 0 || maxAge < 0 || minAge > maxAge) {
                throw new IllegalArgumentException("Invalid age range");
            }
            
            List<User> users = new ArrayList<>();
            userCollection.find(and(gte("age", minAge), lte("age", maxAge))).into(users);
            
            logger.debug("Found {} users in age range {}-{}", users.size(), minAge, maxAge);
            return users;
            
        } catch (Exception e) {
            logger.error("Error finding users by age range: {}-{}", minAge, maxAge, e);
            throw new RuntimeException("Failed to find users by age range", e);
        }
    }
    
    /**
     * Find users by name (case-insensitive partial match)
     */
    public List<User> findByNameContaining(String namePattern) {
        try {
            if (namePattern == null || namePattern.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            List<User> users = new ArrayList<>();
            // Using regex for case-insensitive partial matching
            userCollection.find(regex("name", ".*" + namePattern + ".*", "i")).into(users);
            
            logger.debug("Found {} users with name containing: {}", users.size(), namePattern);
            return users;
            
        } catch (Exception e) {
            logger.error("Error finding users by name pattern: {}", namePattern, e);
            throw new RuntimeException("Failed to find users by name pattern", e);
        }
    }
    
    /**
     * Count total number of users
     */
    public long countUsers() {
        try {
            long count = userCollection.countDocuments();
            logger.debug("Total user count: {}", count);
            return count;
            
        } catch (Exception e) {
            logger.error("Error counting users", e);
            throw new RuntimeException("Failed to count users", e);
        }
    }
    
    // ============ UPDATE OPERATIONS ============
    
    /**
     * Update user by ID
     */
    public boolean updateUser(String id, User updatedUser) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            
            if (updatedUser == null || !updatedUser.isValid()) {
                throw new IllegalArgumentException("Updated user data is not valid");
            }
            
            ObjectId objectId = new ObjectId(id);
            updatedUser.updateTimestamp();
            
            Bson updates = combine(
                set("name", updatedUser.getName()),
                set("email", updatedUser.getEmail()),
                set("age", updatedUser.getAge()),
                set("updated_at", updatedUser.getUpdatedAt())
            );
            
            UpdateResult result = userCollection.updateOne(eq("_id", objectId), updates);
            
            boolean success = result.wasAcknowledged() && result.getModifiedCount() > 0;
            if (success) {
                logger.info("User updated successfully with ID: {}", id);
            } else {
                logger.warn("No user found to update with ID: {}", id);
            }
            
            return success;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid ObjectId format: {}", id);
            return false;
        } catch (Exception e) {
            logger.error("Error updating user with ID: {}", id, e);
            throw new RuntimeException("Failed to update user", e);
        }
    }
    
    /**
     * Update user's email by ID
     */
    public boolean updateUserEmail(String id, String newEmail) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            
            if (newEmail == null || newEmail.trim().isEmpty() || !isValidEmail(newEmail)) {
                throw new IllegalArgumentException("Invalid email address");
            }
            
            ObjectId objectId = new ObjectId(id);
            
            Bson updates = combine(
                set("email", newEmail),
                set("updated_at", java.time.LocalDateTime.now())
            );
            
            UpdateResult result = userCollection.updateOne(eq("_id", objectId), updates);
            
            boolean success = result.wasAcknowledged() && result.getModifiedCount() > 0;
            if (success) {
                logger.info("User email updated successfully for ID: {}", id);
            } else {
                logger.warn("No user found to update email for ID: {}", id);
            }
            
            return success;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid ObjectId format: {}", id);
            return false;
        } catch (Exception e) {
            logger.error("Error updating user email for ID: {}", id, e);
            throw new RuntimeException("Failed to update user email", e);
        }
    }
    
    // ============ DELETE OPERATIONS ============
    
    /**
     * Delete user by ID
     */
    public boolean deleteUser(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            
            ObjectId objectId = new ObjectId(id);
            DeleteResult result = userCollection.deleteOne(eq("_id", objectId));
            
            boolean success = result.wasAcknowledged() && result.getDeletedCount() > 0;
            if (success) {
                logger.info("User deleted successfully with ID: {}", id);
            } else {
                logger.warn("No user found to delete with ID: {}", id);
            }
            
            return success;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid ObjectId format: {}", id);
            return false;
        } catch (Exception e) {
            logger.error("Error deleting user with ID: {}", id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }
    
    /**
     * Delete users by age range
     */
    public long deleteUsersByAgeRange(int minAge, int maxAge) {
        try {
            if (minAge < 0 || maxAge < 0 || minAge > maxAge) {
                throw new IllegalArgumentException("Invalid age range");
            }
            
            DeleteResult result = userCollection.deleteMany(
                and(gte("age", minAge), lte("age", maxAge))
            );
            
            long deletedCount = result.getDeletedCount();
            logger.info("Deleted {} users in age range {}-{}", deletedCount, minAge, maxAge);
            
            return deletedCount;
            
        } catch (Exception e) {
            logger.error("Error deleting users by age range: {}-{}", minAge, maxAge, e);
            throw new RuntimeException("Failed to delete users by age range", e);
        }
    }
    
    /**
     * Delete all users (use with caution!)
     */
    public long deleteAllUsers() {
        try {
            DeleteResult result = userCollection.deleteMany(new Document());
            long deletedCount = result.getDeletedCount();
            
            logger.warn("Deleted ALL {} users from collection", deletedCount);
            return deletedCount;
            
        } catch (Exception e) {
            logger.error("Error deleting all users", e);
            throw new RuntimeException("Failed to delete all users", e);
        }
    }
    
    // ============ UTILITY METHODS ============
    
    /**
     * Check if user exists by ID
     */
    public boolean existsById(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return false;
            }
            
            ObjectId objectId = new ObjectId(id);
            long count = userCollection.countDocuments(eq("_id", objectId));
            return count > 0;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid ObjectId format: {}", id);
            return false;
        } catch (Exception e) {
            logger.error("Error checking if user exists with ID: {}", id, e);
            return false;
        }
    }
    
    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            
            long count = userCollection.countDocuments(eq("email", email));
            return count > 0;
            
        } catch (Exception e) {
            logger.error("Error checking if user exists with email: {}", email, e);
            return false;
        }
    }
    
    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".") && email.length() > 5;
    }
}
