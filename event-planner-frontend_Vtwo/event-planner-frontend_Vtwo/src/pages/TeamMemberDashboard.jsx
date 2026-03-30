import { useState } from 'react';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import TeamMemberOverview from '../components/organizer/TeamMemberOverview';
import TeamMemberEvents from '../components/organizer/TeamMemberEvents';
import TeamMemberTasks from '../components/organizer/TeamMemberTasks';
import TeamMemberFeedback from '../components/organizer/TeamMemberFeedback';
import ChatSection from '../components/organizer/ChatSection';

const sectionMap = {
  overview: <TeamMemberOverview />,
  events: <TeamMemberEvents />,
  tasks: <TeamMemberTasks />,
  feedback: <TeamMemberFeedback />,
  chat: <ChatSection />,
};

export default function TeamMemberDashboard() {
  const [section, setSection] = useState('overview');
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen bg-surface-50 overflow-hidden">
      <Sidebar activeSection={section} onSectionChange={setSection} isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Navbar onMenuToggle={() => setSidebarOpen((o) => !o)} />
        <main className="flex-1 overflow-y-auto p-4 lg:p-6">
          {sectionMap[section] || <TeamMemberOverview />}
        </main>
      </div>
    </div>
  );
}
