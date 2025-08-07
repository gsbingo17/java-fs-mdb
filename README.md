# Java Firestore CRUD Application

A comprehensive Java application demonstrating CRUD (Create, Read, Update, Delete) operations using Google Cloud Firestore with MongoDB compatibility. This project uses Maven for dependency management and includes service account authentication.

## 🚀 Features

- **Complete CRUD Operations**: Create, Read, Update, and Delete users
- **Firestore MongoDB API**: Uses familiar MongoDB syntax with Firestore's scalability
- **Compute Engine Authentication**: Secure Google Cloud authentication via VM service account
- **Comprehensive Validation**: Input validation and business logic
- **Connection Pooling**: Optimized database connections
- **Interactive Demo**: Both automated demo and interactive mode
- **Robust Error Handling**: Comprehensive error handling and logging
- **Statistics**: User analytics and reporting
- **Clean Architecture**: Separation of concerns with Model-Repository-Service pattern

## 📋 Prerequisites

Before running this application, ensure you have:

1. **Java 11 or higher** installed
2. **Maven 3.6+** installed
3. **Google Cloud Project** with Firestore enabled
4. **Firestore MongoDB API** enabled in your project
5. **Service Account** with appropriate permissions

## 🛠️ Setup Instructions

### 1. Clone and Navigate to Project

```bash
git clone <repository-url>
cd java-firestore-crud
```

### 2. Google Cloud Setup

#### Enable Firestore MongoDB API
1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project or create a new one
3. Enable the Firestore API
4. Enable MongoDB API for Firestore (if available in your region)

#### Get Database Information
Get your database UID and location using the gcloud CLI:

```bash
gcloud firestore databases describe --database=DATABASE_ID --format='yaml(locationId, uid)'
```

Replace `DATABASE_ID` with your actual database ID. Note down the `uid` and `locationId` values.

#### Setup Service Account for Compute Engine VM

1. Create a service account:
   - Navigate to **IAM & Admin > Service Accounts**
   - Click **Create Service Account**
   - Provide a name (e.g., "firestore-mongodb-service")

2. Grant the service account the `roles/datastore.user` role:
   ```bash
   gcloud projects add-iam-policy-binding PROJECT_NAME \
     --member="serviceAccount:SERVICE_ACCOUNT_EMAIL" \
     --role=roles/datastore.user
   ```

3. Attach the service account to your Compute Engine VM:
   - For new VMs: Use `--service-account` flag during creation
   - For existing VMs: Change the service account in VM settings

### 3. Configuration

Edit `src/main/resources/application.properties`:

```properties
# Replace with your actual project details
google.cloud.project.id=your-actual-project-id
firestore.database.name=your-database-name
firestore.database.uid=your-database-uid
firestore.database.location=your-database-location
```

### 4. Build the Project

```bash
mvn clean compile
```

### 5. Run the Application

```bash
mvn exec:java
```

Or alternatively:

```bash
mvn clean package
java -jar target/java-firestore-crud-1.0.0.jar
```

## 🏗️ Project Structure

```
java-firestore-crud/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/firestore/
│       │       ├── Main.java                 # Application entry point
│       │       ├── config/
│       │       │   └── FirestoreConfig.java  # Database configuration
│       │       ├── model/
│       │       │   └── User.java             # User entity model
│       │       ├── repository/
│       │       │   └── UserRepository.java   # Data access layer
│       │       └── service/
│       │           └── UserService.java      # Business logic layer
│       └── resources/
│           ├── application.properties        # Configuration file
│           └── logback.xml                  # Logging configuration
├── pom.xml                                  # Maven configuration
├── COMPUTE_ENGINE_SETUP.md                  # VM setup guide
└── README.md                               # This file
```

## 💻 Usage Examples

### Basic CRUD Operations

```java
UserService userService = new UserService();

// Create a user
User newUser = userService.createUser("John Doe", "john@example.com", 30);

// Read user by ID
Optional<User> user = userService.getUserById(newUser.getIdAsString());

// Update user
userService.updateUser(newUser.getIdAsString(), "John Updated", "john.new@example.com", 31);

// Delete user
userService.deleteUser(newUser.getIdAsString());
```

### Advanced Queries

```java
// Find users by age range
List<User> youngUsers = userService.getUsersByAgeRange(18, 30);

// Search users by name pattern
List<User> johnsUsers = userService.searchUsersByName("John");

// Get user statistics
UserService.UserStatistics stats = userService.getUserStatistics();
```

## 🔧 Configuration Options

### Application Properties

| Property | Description | Default |
|----------|-------------|---------|
| `google.cloud.project.id` | Your Google Cloud project ID | Required |
| `firestore.database.name` | Firestore database name | `your-database-name` |
| `firestore.database.uid` | Firestore database UID | Required |
| `firestore.database.location` | Firestore database location | Required |
| `app.collection.name` | Collection name for users | `users` |
| `mongodb.connection.pool.max.size` | Max connection pool size | `100` |
| `mongodb.connection.pool.min.size` | Min connection pool size | `5` |

### Logging Configuration

The application uses Logback for logging. Logs are written to:
- Console (for development)
- `logs/firestore-crud-app.log` (application logs)
- `logs/mongodb-driver.log` (MongoDB driver logs)

## 🧪 Testing

### Run Demo Mode
The application includes a comprehensive demo that tests all CRUD operations:

```bash
mvn exec:java
```

### Interactive Mode
After the demo, you can use interactive mode to manually test operations:
1. Create users
2. Find users by ID or email
3. Update user information
4. Delete users
5. View statistics

## 🔒 Security Considerations

1. **Service Account Permissions**: Ensure your VM's service account has minimal required permissions
2. **Input Validation**: The application includes comprehensive input validation

## 📊 MongoDB API Features Used

- **Collections**: Working with Firestore collections
- **Documents**: CRUD operations on documents

## 🐛 Troubleshooting

### Common Issues

1. **Connection Failed**
   - Verify your project ID and database name
   - Check service account permissions
   - Ensure MongoDB API is enabled for Firestore

2. **Authentication Errors**
   - Check VM service account has correct roles
   - Verify VM is using the correct service account
   - Ensure service account has `roles/datastore.user` permission

3. **Build Errors**
   - Ensure Java 11+ is installed
   - Run `mvn clean` before building
   - Check Maven settings and repository access

### Enable Debug Logging

Add this to your logback.xml to enable debug logging:

```xml
<logger name="com.example.firestore" level="DEBUG" />
<logger name="org.mongodb.driver" level="DEBUG" />
```

## 📚 Dependencies

- **MongoDB Java Driver**: 5.1.1
- **Google Cloud Auth Library**: 1.19.0
- **SLF4J**: 2.0.9
- **Logback**: 1.4.11
- **JUnit**: 5.10.0 (for testing)

## 🔗 Additional Resources

- [Firestore Documentation](https://cloud.google.com/firestore/docs)
- [MongoDB Java Driver Documentation](https://mongodb.github.io/mongo-java-driver/)
- [Google Cloud Authentication](https://cloud.google.com/docs/authentication)
- [Maven Documentation](https://maven.apache.org/guides/)

## 📞 Support

If you encounter issues:
1. Check the troubleshooting section
2. Review the logs in the `logs/` directory
3. Verify your Google Cloud setup
4. Open an issue with detailed error information
