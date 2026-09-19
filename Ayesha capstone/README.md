# Ayesha Mart - Authentication Module

Java Book E-Commerce project. This module covers **Register -> Login -> Session -> Protected Home -> Logout** only.
Users are stored in an Excel `.xlsx` file - **no database**.

## Tech Stack
- Java 11+, JSP, HTML, CSS, JavaScript
- Java Servlets (jakarta.servlet 6.0) + Apache Tomcat 10.1
- Apache POI (`.xlsx` storage)
- PBKDF2WithHmacSHA256 password hashing (never plain text)

## Project Structure
```
src/main/java/com/ayeshamart/
├── controller/
│   ├── LoginServlet.java      POST /login  -> verify -> create HttpSession -> home.jsp
│   ├── RegisterServlet.java   POST /register -> validate -> hash -> save to Excel
│   └── LogoutServlet.java     GET /logout  -> invalidate session -> login.jsp
├── dao/UserDAO.java           Excel-backed data access (duplicate check, auth)
├── model/User.java
└── util/
    ├── ExcelUtil.java         auto-creates data/users.xlsx, read/append rows
    └── PasswordUtil.java      PBKDF2WithHmacSHA256 hash + verify

src/main/webapp/
├── login.jsp
├── register.jsp
├── home.jsp                  (protected - requires session)
├── css/style.css
└── WEB-INF/web.xml
```

## Excel Storage
- File: `data/users.xlsx` under the Tomcat base dir (e.g. `<tomcat>/data/users.xlsx`)
- Created automatically on first request (header row included)
- Columns: `user_id, name, email, password, phone, address, created_at`
- `password` column stores PBKDF2 hashes in `iterations:salt:hash` format, **never** plain text
- To reset data, delete `data/users.xlsx` and restart Tomcat

## Requirements
- JDK 11 or newer
- Apache Maven 3.6+
- Apache Tomcat 10.1.x (uses `jakarta.servlet.*`. This project is already migrated. For Tomcat 9, imports would need to change back to `javax.servlet.*`.)

## Tomcat Setup + Deploy Steps
1. Download and extract Apache Tomcat 9 (`https://tomcat.apache.org/download-90.cgi`).
2. (Optional) Start Tomcat once to create the base config:
   ```
   <tomcat>/bin/startup.bat
   ```
3. Build the WAR from this project folder:
   ```
   mvn clean package
   ```
   Output: `target/ayesha-mart.war`
4. Copy the WAR into the Tomcat webapps folder:
   ```
   copy target\ayesha-mart.war <tomcat>\webapps\
   ```
5. Start Tomcat:
   ```
   <tomcat>/bin/startup.bat
   ```
6. Open: `http://localhost:8080/ayesha-mart/`

## Simple Testing Steps
1. **Register** -> http://localhost:8080/ayesha-mart/register.jsp
   - Try submitting with an empty field / invalid email / short password -> inline errors appear
   - Register with a valid account -> redirected to login with a success message
   - Register again with the same email -> duplicate email error
2. **Login** -> http://localhost:8080/ayesha-mart/login.jsp
   - Wrong password -> "Invalid email or password."
   - Correct credentials -> redirected to `home.jsp` showing the user's name
3. **Protected Home** -> logout, then open http://localhost:8080/ayesha-mart/home.jsp directly -> redirected to `login.jsp`
4. **Logout** -> click Logout on home page -> session destroyed, back at login page
5. **Verify Excel** -> open `<tomcat>/data/users.xlsx`; check the new row and confirm the `password` column contains a PBKDF2 hash, not the plain-text password

## Maven Dependencies (in pom.xml)
| Dependency | Version | Purpose |
|---|---|---|
| `jakarta.servlet:jakarta.servlet-api` | 6.0.0 (provided) | Servlets + JSP |
| `org.apache.poi:poi-ooxml` | 5.2.5 | Read/write Excel `.xlsx` |
