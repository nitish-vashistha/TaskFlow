import React from 'react';

export const App: React.FC = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center p-8 bg-white rounded-xl shadow-sm border border-gray-100 max-w-md w-full">
        <h1 className="text-2xl font-semibold text-gray-900 tracking-tight">TaskFlow</h1>
        <p className="text-sm text-gray-500 mt-2">Team Task Management System</p>
      </div>
    </div>
  );
};

export default App;
