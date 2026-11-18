import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../common/Button';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          <div className="flex items-center space-x-8">
            <Link to="/dashboard" className="flex items-center space-x-2.5">
              <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold text-lg shadow-sm">
                T
              </div>
              <span className="text-xl font-bold tracking-tight text-gray-900">TaskFlow</span>
            </Link>
            <nav className="hidden md:flex space-x-4">
              <Link
                to="/dashboard"
                className="text-sm font-medium text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md hover:bg-gray-50 transition-colors"
              >
                Projects
              </Link>
            </nav>
          </div>

          <div className="flex items-center space-x-4">
            {user && (
              <div className="flex items-center space-x-3">
                <div className="flex flex-col text-right hidden sm:block">
                  <span className="text-sm font-semibold text-gray-800 leading-tight">{user.fullName}</span>
                  <span className="text-xs text-gray-500">{user.email}</span>
                </div>
                <div className="w-9 h-9 rounded-full bg-blue-100 text-blue-700 font-semibold flex items-center justify-center text-sm border border-blue-200">
                  {user.fullName.charAt(0).toUpperCase()}
                </div>
              </div>
            )}
            <Button variant="outline" size="sm" onClick={handleLogout}>
              Sign out
            </Button>
          </div>
        </div>
      </div>
    </header>
  );
};
