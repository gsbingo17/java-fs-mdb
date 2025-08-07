# Compute Engine VM Setup Guide

This guide provides step-by-step instructions for setting up and running the Java Firestore CRUD application on a Google Compute Engine VM.

## Prerequisites

- Google Cloud Project with Firestore enabled
- Firestore MongoDB API enabled
- gcloud CLI installed and configured

## Step 1: Create Service Account

```bash
# Create a service account
gcloud iam service-accounts create firestore-mongodb-service \
    --description="Service account for Firestore MongoDB API access" \
    --display-name="Firestore MongoDB Service"

# Get your project ID
PROJECT_ID=$(gcloud config get-value project)

# Grant the service account access to Firestore
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:firestore-mongodb-service@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/datastore.user"
```

## Step 2: Create Compute Engine VM

```bash
# Create a VM with the service account attached
gcloud compute instances create firestore-crud-vm \
    --zone=us-central1-a \
    --machine-type=e2-medium \
    --network-interface=network-tier=PREMIUM,stack-type=IPV4_ONLY,subnet=default \
    --maintenance-policy=MIGRATE \
    --provisioning-model=STANDARD \
    --service-account=firestore-mongodb-service@$PROJECT_ID.iam.gserviceaccount.com \
    --scopes=https://www.googleapis.com/auth/cloud-platform \
    --create-disk=auto-delete=yes,boot=yes,device-name=firestore-crud-vm,image=projects/ubuntu-os-cloud/global/images/ubuntu-2204-jammy-v20250105,mode=rw,size=20,type=projects/$PROJECT_ID/zones/us-central1-a/diskTypes/pd-balanced \
    --no-shielded-secure-boot \
    --shielded-vtpm \
    --shielded-integrity-monitoring \
    --labels=gce-managed=yes \
    --reservation-affinity=any
```

## Step 3: Connect to VM and Install Dependencies

```bash
# SSH into the VM
gcloud compute ssh firestore-crud-vm --zone=us-central1-a

# Update the system
sudo apt update && sudo apt upgrade -y

# Install Java 11
sudo apt install openjdk-11-jdk -y

# Install Maven
sudo apt install maven -y

# Install Git
sudo apt install git -y

# Verify installations
java -version
mvn -version
git --version
```

## Step 4: Get Database Information

On your local machine (where gcloud is configured), get the database UID and location:

```bash
# Replace DATABASE_ID with your actual database ID
gcloud firestore databases describe --database=DATABASE_ID --format='yaml(locationId, uid)'
```

Note down the `uid` and `locationId` values.

## Step 5: Clone and Configure Application

On the VM:

```bash
# Clone the repository (adjust URL as needed)
git clone https://github.com/your-username/java-firestore-crud.git
cd java-firestore-crud

# Edit the configuration file
nano src/main/resources/application.properties
```

Update the configuration with your actual values:

```properties
# Replace with your actual project details
google.cloud.project.id=your-actual-project-id
firestore.database.name=your-database-name
firestore.database.uid=your-database-uid-from-step-4
firestore.database.location=your-database-location-from-step-4

# Enable Compute Engine authentication
firestore.use.compute.engine=true

# Collection name
app.collection.name=users
```

## Step 6: Build and Run

```bash
# Clean and compile
mvn clean compile

# Run the application
mvn exec:java
```

## Step 7: Verify Connection

The application should:
1. Successfully connect to Firestore
2. Run the CRUD demonstration
3. Show successful operations in the logs

## Troubleshooting

### Check Service Account

Verify the VM is using the correct service account:

```bash
# Check metadata service
curl "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/email" \
     -H "Metadata-Flavor: Google"
```

### Check Permissions

Verify the service account has the correct permissions:

```bash
# Get an access token
curl "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token" \
     -H "Metadata-Flavor: Google"
```

### Enable Debug Logging

Add this to `src/main/resources/logback.xml`:

```xml
<logger name="com.example.firestore" level="DEBUG" />
<logger name="org.mongodb.driver" level="DEBUG" />
```

### Check Network Connectivity

```bash
# Test connectivity to Firestore
telnet your-database-uid.your-location.firestore.goog 443
```

## Security Notes

1. The VM uses the attached service account for authentication
2. No service account key files are stored on the VM
3. Access is controlled via IAM policies
4. The service account has minimal required permissions

## Monitoring

Check application logs:

```bash
# View application logs
tail -f logs/firestore-crud-app.log

# View MongoDB driver logs
tail -f logs/mongodb-driver.log
```

## Scaling

To create multiple VMs:

```bash
# Create additional VMs with the same service account
for i in {2..5}; do
    gcloud compute instances create firestore-crud-vm-$i \
        --zone=us-central1-a \
        --machine-type=e2-medium \
        --service-account=firestore-mongodb-service@$PROJECT_ID.iam.gserviceaccount.com \
        --scopes=https://www.googleapis.com/auth/cloud-platform \
        # ... other parameters same as above
done
