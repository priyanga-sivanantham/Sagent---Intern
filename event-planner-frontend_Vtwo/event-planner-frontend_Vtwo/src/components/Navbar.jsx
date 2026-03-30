import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const roleColors = {
  ORGANIZER: 'bg-violet-100 text-violet-700',
  TEAM_MEMBER: 'bg-emerald-100 text-emerald-700',
  VENDOR: 'bg-amber-100 text-amber-700',
};

export default function Navbar({ onMenuToggle }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="h-14 bg-white border-b border-surface-200 flex items-center justify-between px-4 sticky top-0 z-30">
      <div className="flex items-center gap-3">
        <button
          onClick={onMenuToggle}
          className="lg:hidden p-1.5 rounded-md hover:bg-surface-100 text-slate-500"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <span className="font-display text-lg text-primary-700 hidden sm:block">EventFlow</span>
      </div>

      <div className="flex items-center gap-3">
        {user && (
          <>
            <span className={`badge ${roleColors[user.role] || 'bg-slate-100 text-slate-600'} hidden sm:inline-flex`}>
              {user.role?.replace('_', ' ')}
            </span>
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-primary-600 flex items-center justify-center text-white text-xs font-semibold">
                {user.name?.[0]?.toUpperCase() || 'U'}
              </div>
              <span className="text-sm font-medium text-slate-700 hidden md:block">{user.name}</span>
            </div>
            <button onClick={handleLogout} className="btn-secondary text-xs py-1.5 px-3">
              Logout
            </button>
          </>
        )}
      </div>
    </header>
  );
}
