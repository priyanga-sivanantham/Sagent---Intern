import { useState } from 'react';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import VendorOverview from '../components/organizer/VendorOverview';
import VendorProfile from '../components/organizer/VendorProfile';
import VendorEvents from '../components/organizer/VendorEvents';
import VendorFeedback from '../components/organizer/VendorFeedback';
import ChatSection from '../components/organizer/ChatSection';

const sectionMap = {
  overview: <VendorOverview />,
  profile: <VendorProfile />,
  events: <VendorEvents />,
  feedback: <VendorFeedback />,
  chat: <ChatSection />,
};

export default function VendorDashboard() {
  const [section, setSection] = useState('overview');
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen bg-surface-50 overflow-hidden">
      <Sidebar activeSection={section} onSectionChange={setSection} isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Navbar onMenuToggle={() => setSidebarOpen((o) => !o)} />
        <main className="flex-1 overflow-y-auto p-4 lg:p-6">
          {sectionMap[section] || <VendorOverview />}
        </main>
      </div>
    </div>
  );
}
