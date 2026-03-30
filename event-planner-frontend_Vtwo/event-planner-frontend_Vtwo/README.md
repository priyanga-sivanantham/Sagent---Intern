# EventFlow — Event Planning Frontend

A full-featured React + Vite frontend for the EventFlow Event Planning Platform.

## Tech Stack

- **React 18** with Vite
- **React Router v6** for routing
- **Axios** with JWT interceptors
- **Tailwind CSS** for styling
- **Context API** for auth state

## Quick Start

### Prerequisites
- Node.js 18+
- Your Spring Boot backend running at `http://localhost:8080`

### Install & Run

```bash
# Install dependencies
npm install

# Start dev server (runs on http://localhost:3000)
npm run dev
```

### Build for Production

```bash
npm run build
npm run preview
```

## Project Structure

```
src/
├── pages/
│   ├── Login.jsx
│   ├── Register.jsx
│   ├── OrganizerDashboard.jsx
│   ├── TeamMemberDashboard.jsx
│   └── VendorDashboard.jsx
├── components/
│   ├── Navbar.jsx
│   ├── Sidebar.jsx
│   ├── ProtectedRoute.jsx
│   ├── StatCard.jsx
│   ├── SectionCard.jsx
│   ├── DataTable.jsx
│   ├── Modal.jsx
│   └── organizer/
│       ├── OrganizerOverview.jsx
│       ├── EventsSection.jsx
│       ├── TeamSection.jsx
│       ├── TasksSection.jsx
│       ├── VendorsSection.jsx
│       ├── BudgetSection.jsx
│       ├── GuestsSection.jsx
│       ├── FeedbackSection.jsx
│       ├── ChatSection.jsx         ← shared by all roles
│       ├── TeamMemberOverview.jsx
│       ├── TeamMemberEvents.jsx
│       ├── TeamMemberTasks.jsx
│       ├── TeamMemberFeedback.jsx
│       ├── VendorOverview.jsx
│       ├── VendorProfile.jsx
│       ├── VendorEvents.jsx
│       └── VendorFeedback.jsx
├── context/
│   └── AuthContext.jsx
├── services/
│   └── api.js                     ← Axios instance + interceptors
└── utils/
    └── auth.js                    ← Role helpers
```

## Features by Role

### ORGANIZER
- Dashboard overview with stats
- Create / edit / delete events
- Assign team members to events
- Create and manage tasks (with status updates)
- Add vendors and assign them to events
- Manage budgets and expenses
- Add guests, send invitations and feedback emails
- View and delete feedback per event
- Full chat system (create rooms, manage participants, messaging)

### TEAM_MEMBER
- Overview with personal task stats
- View assigned events
- View and update status of assigned tasks
- View feedback for their events
- Chat (join existing rooms, send messages)

### VENDOR
- Create/edit own vendor profile
- View assigned events and service details
- View feedback for their events
- Chat (join existing rooms, send messages)

## Auth Flow

1. Login → JWT stored in `localStorage`
2. On app load → calls `/users/me` to restore session
3. Role-based redirect: ORGANIZER → `/organizer`, TEAM_MEMBER → `/team-member`, VENDOR → `/vendor`
4. 401 response → clears auth, redirects to `/login`
5. All API calls include `Authorization: Bearer <token>` header automatically

## Backend URL

Default: `http://localhost:8080`

To change it, edit `src/services/api.js`:
```js
const api = axios.create({
  baseURL: 'http://localhost:8080',
  ...
});
```
