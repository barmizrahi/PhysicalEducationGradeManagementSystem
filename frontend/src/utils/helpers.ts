export const isValidTimeFormat = (value: string): boolean => {
  if (!value || value.trim() === '') return false;
  
  // Allow just '0'
  if (value.trim() === '0') return true;
  
  // Standard mm:ss format
  const timeRegex = /^\d{1,2}:\d{2}$/;
  return timeRegex.test(value.trim());
};

export const parseTimeToDecimal = (timeStr: string): number => {
  if (!timeStr || timeStr.trim() === '') return 0;
  
  const trimmed = timeStr.trim();
  
  // Handle '0' directly
  if (trimmed === '0') return 0;
  
  // Handle mm:ss format
  if (trimmed.includes(':')) {
    const [minutes, seconds] = trimmed.split(':').map(Number);
    return minutes + (seconds / 60);
  }
  
  return parseFloat(trimmed) || 0;
};