export const ROLES = {
  ORGANIZER: 'ORGANIZER',
  TEAM_MEMBER: 'TEAM_MEMBER',
  VENDOR: 'VENDOR',
};

export const getDashboardPath = (role) => {
  switch (role) {
    case ROLES.ORGANIZER: return '/organizer';
    case ROLES.TEAM_MEMBER: return '/team-member';
    case ROLES.VENDOR: return '/vendor';
    default: return '/login';
  }
};

export const extractItems = (data) => {
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (data.items && Array.isArray(data.items)) return data.items;
  return [];
};
