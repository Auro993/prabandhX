# 🚀 PrabandhX - Full-Stack Organization Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 📋 Overview

**PrabandhX** is a comprehensive organization management system designed to streamline project management, team collaboration, and task tracking. Built with a robust Spring Boot backend and a modern React frontend, it provides role-based access control for Admins, Managers, and Users.

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based authentication
- Role-based access control (Admin, Manager, User)
- Secure password encryption

### 👥 User Management
- Create, read, update, delete users
- Role assignment (Admin/Manager/User)
- User search and filtering

### 📊 Dashboard Analytics
- Real-time statistics (users, projects, tasks)
- Activity timeline charts
- Task distribution pie charts
- Recent activity feed

### 📁 File Management
- Upload/download project files
- File version history
- File sharing with collaborators

### 🤝 Team Collaboration
- Invite collaborators to projects
- Permission management (View/Edit/Admin)
- Accept/reject invitations

### ✅ Task Management
- Create, assign, and track tasks
- Priority levels (High/Medium/Low)
- Due date tracking
- Task status (To Do/In Progress/Completed)

### 📈 Activity Logs
- Complete audit trail
- Filter by action type, date range, user
- Export functionality

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.2.5 | REST API framework |
| Java | 21 | Programming language |
| Spring Security | 6.1.6 | Authentication & Authorization |
| JWT | 0.11.5 | Token-based authentication |
| Spring Data JPA | 3.2.5 | Database ORM |
| MySQL | 8.0 | Database |
| Maven | 3.9+ | Build tool |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.3.1 | UI library |
| Vite | 5.4.10 | Build tool |
| Framer Motion | 11.0.2 | Animations |
| Recharts | 2.12.0 | Charts |
| React Router DOM | 6.22.0 | Routing |
| React Hot Toast | 2.4.1 | Notifications |

## 📁 Project Structure
prabandhx/
├── backend/ # Spring Boot Application
│ ├── src/main/java/
│ │ └── com/prabandhx/prabandhx/
│ │ ├── config/ # Security & App Config
│ │ ├── controller/ # REST Controllers
│ │ ├── dto/ # Data Transfer Objects
│ │ ├── entity/ # JPA Entities
│ │ ├── repository/ # JPA Repositories
│ │ ├── service/ # Business Logic
│ │ └── security/ # JWT & Security
│ ├── src/main/resources/
│ │ └── application.properties # Configuration
│ └── pom.xml # Maven Dependencies
│
└── frontend/ # React Application
├── src/
│ ├── components/ # Reusable Components
│ ├── pages/ # Page Components
│ │ ├── admin/ # Admin Pages
│ │ ├── manager/ # Manager Pages
│ │ └── user/ # User Pages
│ ├── services/ # API Services
│ ├── context/ # React Context
│ └── hooks/ # Custom Hooks
├── package.json # NPM Dependencies
└── vite.config.js # Vite Configuration

text

## 🚀 Installation

### Prerequisites
- Java 21 or higher
- Node.js 18+ and npm
- MySQL 8.0+
- Maven 3.9+

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Auro993/prabandhX.git
   cd prabandhX/backend
Configure MySQL Database

sql
CREATE DATABASE prabandhx;
Update application.properties

properties
spring.datasource.url=jdbc:mysql://localhost:3306/prabandhx
spring.datasource.username=root
spring.datasource.password=your_password
Run the application

bash
./mvnw spring-boot:run
The backend will start at http://localhost:8080

Frontend Setup
Navigate to frontend directory

bash
cd ../frontend
Install dependencies

bash
npm install
Run the development server

bash
npm run dev
The frontend will start at http://localhost:5173

🔑 Default Users
Role	Email	Password
Admin	admin@prabandhx.com	admin123
Manager	manager@prabandhx.com	manager123
User	user@prabandhx.com	user123
📡 API Endpoints
Category	Endpoint	Method	Description
Auth	/api/auth/login	POST	User login
Auth	/api/auth/register	POST	User registration
Users	/api/users	GET/POST/PUT/DELETE	User management
Projects	/api/projects	GET/POST/PUT/DELETE	Project management
Tasks	/api/tasks	GET/POST/PUT/DELETE	Task management
Files	/api/files	GET/POST/DELETE	File management
Activity	/api/activity	GET	Activity logs
🎯 Features by Role
Admin
Full system access

Manage all users, projects, tasks

View all activity logs

System configuration

Manager
Manage assigned projects

Create and assign tasks

Manage team members

View project activity

User
View assigned tasks

Update task status

View personal activity

Upload/download files

🤝 Contributing
Fork the repository

Create your feature branch (git checkout -b feature/AmazingFeature)

Commit your changes (git commit -m 'Add some AmazingFeature')

Push to the branch (git push origin feature/AmazingFeature)

Open a Pull Request

📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

👨‍💻 Author
Aurosmruti Sahoo

GitHub: @Auro993

Email: aurosmitasahoo45@gmail.com

🙏 Acknowledgments
Spring Boot Documentation

React Documentation

All contributors and users

⭐ Star this repository if you find it helpful!

text

---

## 📤 **How to Add README to GitHub:**

```bash
cd D:\prabandhx-project
echo "# PrabandhX - Full-Stack Organization Management System" > README.md
