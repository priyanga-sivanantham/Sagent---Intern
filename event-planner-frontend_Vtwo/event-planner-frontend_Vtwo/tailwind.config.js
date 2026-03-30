/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f4ff',
          100: '#e0e9ff',
          200: '#c7d7fe',
          300: '#a5bbfc',
          400: '#8197f8',
          500: '#6272f1',
          600: '#4f55e5',
          700: '#4244ca',
          800: '#3739a3',
          900: '#313581',
          950: '#1e1f4c',
        },
        accent: {
          400: '#fb923c',
          500: '#f97316',
          600: '#ea6c10',
        },
        surface: {
          50: '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          800: '#1e293b',
          900: '#0f172a',
          950: '#080e1a',
        }
      },
      fontFamily: {
        display: ['Georgia', 'Cambria', 'serif'],
        body: ['system-ui', '-apple-system', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      boxShadow: {
        'card': '0 1px 3px 0 rgba(0,0,0,.08), 0 1px 2px -1px rgba(0,0,0,.08)',
        'card-hover': '0 4px 12px 0 rgba(0,0,0,.12), 0 2px 4px -1px rgba(0,0,0,.08)',
        'modal': '0 20px 60px -10px rgba(0,0,0,.3)',
      }
    },
  },
  plugins: [],
}
