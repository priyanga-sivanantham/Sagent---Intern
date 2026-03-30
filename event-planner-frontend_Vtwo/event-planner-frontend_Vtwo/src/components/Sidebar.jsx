import { useAuth } from '../context/AuthContext';

const organizerNav = [
  { label: 'Overview', section: 'overview', icon: '🏠' },
  { label: 'Events', section: 'events', icon: '📅' },
  { label: 'Team', section: 'team', icon: '👥' },
  { label: 'Tasks', section: 'tasks', icon: '✅' },
  { label: 'Vendors', section: 'vendors', icon: '🏢' },
  { label: 'Budget', section: 'budget', icon: '💰' },
  { label: 'Guests', section: 'guests', icon: '🎟️' },
  { label: 'Feedback', section: 'feedback', icon: '⭐' },
  { label: 'Chat', section: 'chat', icon: '💬' },
];

const teamMemberNav = [
  { label: 'Overview', section: 'overview', icon: '🏠' },
  { label: 'My Events', section: 'events', icon: '📅' },
  { label: 'My Tasks', section: 'tasks', icon: '✅' },
  { label: 'Feedback', section: 'feedback', icon: '⭐' },
  { label: 'Chat', section: 'chat', icon: '💬' },
];

const vendorNav = [
  { label: 'Overview', section: 'overview', icon: '🏠' },
  { label: 'My Profile', section: 'profile', icon: '👤' },
  { label: 'My Events', section: 'events', icon: '📅' },
  { label: 'Feedback', section: 'feedback', icon: '⭐' },
  { label: 'Chat', section: 'chat', icon: '💬' },
];

const navByRole = {
  ORGANIZER: organizerNav,
  TEAM_MEMBER: teamMemberNav,
  VENDOR: vendorNav,
};

export default function Sidebar({ activeSection, onSectionChange, isOpen, onClose }) {
  const { user } = useAuth();
  const navItems = navByRole[user?.role] || [];

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div className="fixed inset-0 bg-black/40 z-20 lg:hidden" onClick={onClose} />
      )}

      <aside className={`
        fixed lg:static inset-y-0 left-0 z-20 w-56 bg-surface-900 flex flex-col
        transform transition-transform duration-200 ease-in-out
        ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
      `}>
        {/* Logo */}
        <div className="h-14 flex items-center px-5 border-b border-white/10">
          <span className="font-display text-xl text-white">EventFlow</span>
        </div>

        {/* Nav items */}
        <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
          {navItems.map((item) => (
            <button
              key={item.section}
              onClick={() => { onSectionChange(item.section); onClose(); }}
              className={`
                w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-left transition-colors
                ${activeSection === item.section
                  ? 'bg-primary-600 text-white font-medium'
                  : 'text-slate-400 hover:text-white hover:bg-white/10'
                }
              `}
            >
              <span className="text-base">{item.icon}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </nav>

        {/* User footer */}
        <div className="p-3 border-t border-white/10">
          <div className="flex items-center gap-2 px-2 py-1.5">
            <div className="w-7 h-7 rounded-full bg-primary-500 flex items-center justify-center text-white text-xs font-bold shrink-0">
              {user?.name?.[0]?.toUpperCase()}
            </div>
            <div className="min-w-0">
              <p className="text-xs font-medium text-white truncate">{user?.name}</p>
              <p className="text-xs text-slate-500 truncate">{user?.email}</p>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
}
