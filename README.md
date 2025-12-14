# 🏨 Royal Suite - Hotel Management System

Welcome to **Royal Suite**, a comprehensive desktop application designed to streamline hotel operations. Built with Java, this project features a robust, multi-server backend and an elegant JavaFX client, providing a seamless experience for hotel staff and management.

## ✨ Key Features

*   **👤 User Authentication:** Secure login for different user roles (Admin, Employee).
*   **🛌 Room Management:** Manage room inventory, types, and availability.
*   **👥 Client Management:** Maintain a database of hotel clients.
*   **📅 Reservation Management:** Create, view, confirm, and cancel reservations.
*   **💳 Payment & Invoicing:** Process payments and generate detailed invoices for clients.
*   **📊 Advanced Reporting:** Generate reports on hotel occupancy, revenue, and client history.
*   **🎨 Modern UI:** A clean and intuitive user interface built with JavaFX.

## 🏗️ Architecture Overview

The Royal Suite application is built on a distributed architecture, composed of several Maven modules:

*   **`hotel-common`**: A shared module containing JPA entities, business interfaces, and enums.
*   **`hotel-rmi-server`**: An RMI-based server that exposes core services (Authentication, Room, Client, Reservation).
*   **`hotel-ejb-server`**: An EJB-based server that provides advanced services (Payment, Reporting, Invoicing).
*   **`hotel-client-ui`**: The JavaFX client application that consumes services from both the RMI and EJB servers.

This architecture allows for a clear separation of concerns and demonstrates the integration of different Java enterprise technologies.

## 🛠️ Technologies Used

*   **Java 11**
*   **Maven** for dependency management
*   **JavaFX** for the client UI
*   **Java RMI** for core backend services
*   **Java EJB** for advanced backend services
*   **Hibernate** as the JPA provider
*   **H2 Database** for data persistence
*   **WildFly** as the EJB container

## 🚀 Getting Started

To get the Royal Suite application up and running, you will need to have the following prerequisites installed on your system.

### Prerequisites

*   **Java Development Kit (JDK) 11**
*   **Apache Maven**
*   **WildFly Application Server**

### Build

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/hotel-management-system.git
    cd hotel-management-system
    ```

2.  **Build the project:**
    Run the following command from the project's root directory to build all modules and install the artifacts in your local Maven repository:
    ```bash
    mvn clean install
    ```

## 🏃‍♂️ Running the Application

To run the application, you must start the backend servers first, and then the client application.

### 1. Backend Setup

#### a. Start the RMI Server

This server manages authentication, rooms, clients, and reservations.

Open a terminal and run the following command from the project's root directory:

```bash
mvn exec:java -pl hotel-rmi-server -Dexec.mainClass="com.hotel.rmi.server.RMIServer"
```

The server will start and listen on port `1099`.

#### b. Deploy the EJB Services

The EJB services (Payment, Reporting, Invoicing) are deployed to a WildFly application server.

1.  **Start your WildFly server.**
2.  **Locate the EJB module artifact:** The file `hotel-ejb-server-1.0-SNAPSHOT.jar` can be found in the `hotel-ejb-server/target` directory.
3.  **Deploy the artifact:** Copy the `hotel-ejb-server-1.0-SNAPSHOT.jar` file to the `standalone/deployments` directory of your WildFly server.

WildFly will automatically deploy the EJB services.

### 2. Frontend Setup

#### Start the Client UI

Once the RMI server is running and the EJB services are deployed, you can start the client application.

Open a **new terminal** and run the following command from the project's root directory:

```bash
mvn exec:java -pl hotel-client-ui -Dexec.mainClass="com.hotel.client.HotelApplication"
```

The login window of the Royal Suite management system should appear.

## 🔑 Default Credentials

The following user accounts are created by default when the RMI server is started for the first time:

*   **Administrator:**
    *   **Username:** `admin`
    *   **Password:** `admin123`
*   **Employee:**
    *   **Username:** `employe`
    *   **Password:** `employe123`

## 🖼️ Screenshots

*(Placeholder for screenshots of the application)*