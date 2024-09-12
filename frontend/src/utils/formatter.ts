// SignUpPage

export const removePhoneFormatting = (phoneNumber: string): string => {
  return phoneNumber.replace(/-/g, '');
};

export const removeBirthdateFormatting = (birthdate: string): string => {
  return birthdate.replace(/\./g, '');
};
