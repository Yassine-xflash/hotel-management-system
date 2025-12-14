# Hotel Management System

This is a desktop application for managing a hotel, built with Java. It features a multi-server backend (RMI and EJB-like services) and a JavaFX client.

## Project Structure

The project is divided into several Maven modules:

- `hotel-common`: Contains shared code, including JPA entities, business interfaces, and enums.
- `hotel-rmi-server`: A server that exposes core services (Authentication, Chamber, Client, Reservation) via Java RMI.
- `hotel-ejb-server`: A second server that exposes additional services (Payment, Reporting) via Java RMI.
- `hotel-client-ui`: The graphical user interface (GUI) built with JavaFX, which connects to both servers to provide the application's functionality.

## Prerequisites

To build and run this project, you will need:

- **Java Development Kit (JDK) 11:** The project is built with Java 11.
- **Apache Maven:** Used for dependency management and building the project.

## Database Setup

The application uses an **H2 file-based database**. The database files (`hoteldb.mv.db`) will be automatically created in your user home directory (e.g., `C:\Users\YourUser` on Windows or `/home/youruser` on Linux) the first time you run a server.

No manual database setup is required.

## Build

To build the entire project and install the artifacts in your local Maven repository, run the following command from the project's root directory:

```bash
mvn clean install
```

## Running the Application

To run the application, you must start the two servers first, and then the client application.

### 1. Start the RMI Server

This server manages authentication, rooms, clients, and reservations.

Open a terminal and run the following command from the project's root directory:

```bash
mvn exec:java -pl hotel-rmi-server -Dexec.mainClass="com.hotel.rmi.server.RMIServer"
```

The server will start and listen on port `1096`. It will also initialize the database with some test data (e.g., an admin user and some rooms) on the first run.

### 2. Start the EJB (RMI) Server

This server manages payments and reporting.

Open a **second terminal** and run the following command from the project's root directory:

```bash
mvn exec:java -pl hotel-ejb-server -Dexec.mainClass="com.hotel.ejb.server.EJBServer"
```

The server will start and listen on port `1100`.

### 3. Start the Client UI

Once both servers are running, you can start the client application.

Open a **third terminal** and run the following command from the project's root directory:

```bash
mvn exec:java -pl hotel-client-ui -Dexec.mainClass="com.hotel.client.HotelApplication"
```

The login window of the hotel management system should appear.

## Default Credentials

The following user accounts are created by default when the RMI server is started for the first time:

- **Administrator:**
  - **Username:** `admin`
  - **Password:** `admin123`
- **Employee:**
  - **Username:** `employe`
  - **Password:** `employe123`
