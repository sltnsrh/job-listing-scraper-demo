
### Running from CLI:
* Ensure you have Java 19 and Maven installed on your system
* Clone the Git repository to your local and move into the directory
* Execute the following command to build the project: `mvn clean install`
* Run the application: `mvn spring-boot:run`
* App will run on local port 8080

### Running from Docker container:
* Clone repository to your local and move into
* Run: `mvn clean package` to build .jar
* Run: `docker-compose up` command to start a Docker containers with DB on local port 3307 and app on port 8080
