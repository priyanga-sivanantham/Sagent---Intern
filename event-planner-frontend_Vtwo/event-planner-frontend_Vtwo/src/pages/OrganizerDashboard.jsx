import { useState } from 'react';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import OrganizerOverview from '../components/organizer/OrganizerOverview';
import EventsSection from '../components/organizer/EventsSection';
import TeamSection from '../components/organizer/TeamSection';
import TasksSection from '../components/organizer/TasksSection';
import VendorsSection from '../components/organizer/VendorsSection';
import BudgetSection from '../components/organizer/BudgetSection';
import GuestsSection from '../components/organizer/GuestsSection';
import FeedbackSection from '../components/organizer/FeedbackSection';
import ChatSection from '../components/organizer/ChatSection';

const sectionMap = {
  overview: <OrganizerOverview />,
  events: <EventsSection />,
  team: <TeamSection />,
  tasks: <TasksSection />,
  vendors: <VendorsSection />,
  budget: <BudgetSection />,
  guests: <GuestsSection />,
  feedback: <FeedbackSection />,
  chat: <ChatSection />,
};

export default function OrganizerDashboard() {
  const [section, setSection] = useState('overview');
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen bg-surface-50 overflow-hidden">
      <Sidebar
        activeSection={section}
        onSectionChange={setSection}
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Navbar onMenuToggle={() => setSidebarOpen((o) => !o)} />
        <main className="flex-1 overflow-y-auto p-4 lg:p-6">
          {sectionMap[section] || <OrganizerOverview />}
        </main>
      </div>
    </div>
  );
}
