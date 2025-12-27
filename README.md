# WEB-FINANCER: The Ultimate Financial Command Center

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg) ![Angular](https://img.shields.io/badge/Angular-18-red.svg)

**WEB-FINANCER** is an enterprise-grade, full-stack personal finance management system tailored for power users who demand precision, aesthetics, and depth in their financial analytics. Combining a high-performance Java backend with a reactive, cutting-edge Angular frontend, it delivers a "trading terminal" experience for personal wealth.

> **Philosophy**: "Your net worth is a data stream. Treat it like one."

---

## 🏗️ Architecture & Engineering

The application follows a strict **Layered Monolithic** architecture designed for modularity and scalability.

### Backend (Spring Boot Core)
The server-side logic handles complex calculations, data persistence, and external API integrations.
*   **Controller Layer**: RESTful endpoints defining the API contract. Handles HTTP requests/responses and standardizes JSON output.
*   **Service Layer**: The brain of the application. Contains business logic for forecasting, market data merging, and transaction categorization.
*   **Repository Layer**: Abstraction over the data store using `Spring Data JPA`. Allows for seamless switching between SQL databases.
*   **DTO Pattern**: Data Transfer Objects are used to decouple internal entities from the external API surface.

### Frontend (Angular SPA)
A Single Page Application designed for speed and interactivity.
*   **Component-Based**: Over 20+ reusable UI components (Charts, Cards, Tables, Modals).
*   **Reactive Services**: Uses `RxJS` Observables to create a real-time feel. For example, updating a transaction in the Ledger instantly recalculates Net Worth in the Sidebar without a page reload.
*   **Guard Rails**: Route guards protect authenticated areas (Dashboard, Vault) from unauthorized access.

---

## 🛠 Tech Stack Deep Dive

| Component | Technology | Context |
| :--- | :--- | :--- |
| **Language** | Java 17 | Core logic, chosen for strong typing and ecosystem. |
| **Framework** | Spring Boot 3 | Rapid API development, Dependency Injection. |
| **Database** | H2 (Dev) / MySQL (Prod) | H2 used for zero-config startup data persistence. |
| **Data Access** | Hibernate / JPA | ORM for mapping `User`, `Transaction`, `Asset` entities. |
| **Frontend** | Angular 18 | The view layer. |
| **Styling** | CSS3 Variables | Dynamic theming engine (Noir/Light). |
| **Charts** | Chart.js | Rendering Canvas-based responsive graphs. |
| **Formatting** | Prettier/ESLint | Code quality enforcement. |
| **Build Tools** | Maven & NPM | Lifecycle management. |

---

## 🌟 Comprehensive Feature List

### 1. 🏰 Command Center (Dashboard)
The mission control screen aggregating data from all subsystems.
*   **HUD Design**: "Heads Up Display" cards showing vital stats (Net worth, cash flow).
*   **Dynamic Theming**: Cards adapt to Light/Dark modes, changing opacity, borders, and shadows instantly.
*   **Data Visualization**:
    *   *Portfolio Allocation*: A Doughnut chart visualizing asset distribution (Crypto vs. stocks vs. Cash).
    *   *Cash Flow Trend*: A 6-month historical line chart smoothing out monthly spending spikes.

### 2. 📒 Universal Ledger (Transaction Management)
*   **CRUD Operations**: Create, Read, Update, Delete transactions.
*   **Smart Type Detection**: Automatically categorizes entries as `INCOME`, `EXPENSE`, or `INVESTMENT`.
*   **Persistency**: Data survives restarts (via H2 file-based storage or MySQL).

### 3. 💎 Asset Vault
Tracks the user's tangible and intangible assets.
*   **Asset Classes**: Supports Real Estate, Vehicles, Stock Holdings, and Cash accounts.
*   **Valuation Engine**: Automatically sums asset values to contribute to the global Net Worth calculation.

### 4. 🧠 Forecasting Engine (Simulation)
A Monte Carlo-lite simulation tool.
*   **Inputs**: Current Net Worth, Monthly Contribution, Annual Return Rate (APR), Inflation Rate, Time Horizon (Years).
*   **Outputs**:
    *   *Future Value*: Nominal currency value.
    *   *Real Value*: Inflation-adjusted purchasing power.
    *   *Total Contributed*: How much principal was added vs. compound interest earned.

### 5. 📉 Market Terminal
*   **Price History**: Fetches intraday and historical price data for tracking market trends.
*   **Search Algorithm**: Custom ticker search functionality.
*   **Color-Coded Canvas**: Charts turn <span style="color:green">Green</span> (Bullish) or <span style="color:red">Red</span> (Bearish) based on trend direction.

### 6. 🏆 Goal Tracker
*   **Gamified Savings**: Visual progress bars fill up as you contribute to goals.
*   **Deadline Logic**: Calculates "Days Left" and "Required Daily Saving" to meet targets on time.

### 7. 📂 Document Repository
*   **Binary Storage**: Uploads PDF/Images directly to the server.
*   **Metadata Indexing**: Search documents by filename or upload date.

---

## 📡 API Reference endpoints

The backend exposes a comprehensive REST API. Here are the core endpoints:

### Transactions
*   `GET /api/transactions`: Fetch all transactions.
*   `POST /api/transactions`: Record a new transaction.
*   `DELETE /api/transactions/{id}`: Remove an entry.

### Assets
*   `GET /api/assets`: List portfolio assets.
*   `GET /api/assets/total-value`: Calculated sum of all assets.

### Market Data
*   `GET /api/market/history/{symbol}`: Get historical pricing for a ticker.
*   `GET /api/market/search?query={q}`: Find stocks/crypto.

### Stats & Analytics
*   `GET /api/analytics/monthly-cash-flow`: Net difference between income and expenses for the current month.
*   `GET /api/analytics/category-breakdown`: Aggregated spending by category.

---

## 🎨 UI Design System

We utilize a custom-built CSS Variable system (`:root`) to handle theming without external libraries like Tailwind or Bootstrap, ensuring a unique identity.

### 🌑 Design Noir (Dark Mode)
*   **Background**: Deep Space Gradient (`radial-gradient(#1f1f26, #0a0a0c)`).
*   **Surfaces**: Glassmorphism (`rgba(20, 20, 25, 0.7)`).
*   **Accents**: Neon Amber (`#f0b504`) and Cyber Red (`#e11b1b`).
*   **Typography**: Inter (UI) and Roboto Mono (Data).

### ☀️ High Contrast (Light Mode)
*   **Background**: Clean Slate (`#f8fafc`).
*   **Text**: Pitch Black & Slate 900 for maximum readability.
*   **Borders**: Solid Slate borders replacing glows for a crisper "Print-Ready" look.

---

## ⚙️ Setup & Installation Guide

### 1. Database Configuration
By default, the app uses an in-memory/file-based H2 database for ease of use.
*   *Modification*: To use MySQL, open `backend/src/main/resources/application.properties` and update `spring.datasource.url`.

### 2. Backend Boot
```bash
cd backend
# Build the JAR
mvn clean package -DskipTests
# Run
mvn spring-boot:run
```

### 3. Frontend Launch
```bash
cd frontend
# Install packages
npm install
# Serve
ng serve --open
```

---

## 🔒 Security
*   **CORS**: Configured to allow requests strictly from `http://localhost:4200` (Angular default).
*   **Validation**: Backend `@Valid` annotations ensure data integrity (e.g., preventing negative transaction amounts).

---

## 🗺️ Roadmap
*   [ ] **JWT Authentication**: Full-scale login/signup with token rotation.
*   [ ] **Plaid Integration**: Automatic bank account syncing.
*   [ ] **Docker Support**: Containerization for easy cloud deployment.

---

**© 2025 Ysn-Ir | Open Source Financial Intelligence**