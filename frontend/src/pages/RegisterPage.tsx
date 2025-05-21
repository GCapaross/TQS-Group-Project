import React, { useState } from 'react';
import AccountTypeSelection from '../components/AccountTypeSelection';
import RegistrationForm from '../components/RegistrationForm';

const RegisterPage: React.FC = () => {
  const [selectedType, setSelectedType] = useState<'user' | 'operator' | null>(null);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {selectedType === null ? (
        <AccountTypeSelection onSelect={setSelectedType} />
      ) : (
        <RegistrationForm accountType={selectedType} />
      )}
    </div>
  );
};

export default RegisterPage; 