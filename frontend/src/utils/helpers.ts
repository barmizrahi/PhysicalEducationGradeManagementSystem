/**
 * Utility helper functions for the PE Grade Management System
 */

/**
 * Format a grade to 2 decimal places
 */
export const formatGrade = (grade: number): string => {
  return grade.toFixed(2)
}

/**
 * Convert decimal minutes to mm:ss format
 * Example: 10.5 → "10:30"
 */
export const formatTimeFromDecimal = (decimalMinutes: number): string => {
  const minutes = Math.floor(decimalMinutes)
  const seconds = Math.round((decimalMinutes - minutes) * 60)
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

/**
 * Convert mm:ss format to decimal minutes
 * Example: "10:30" → 10.5
 */
export const parseTimeToDecimal = (timeString: string): number | null => {
  const trimmed = timeString.trim()

  if (trimmed === "0") {
    return 0
  }
  const match = timeString.match(/^(\d+):(\d{2})$/)
  if (!match) {
    return null
  }
  const minutes = parseInt(match[1], 10)
  const seconds = parseInt(match[2], 10)
  if (seconds >= 60) {
    return null
  }
  return minutes + seconds / 60
}

/**
 * Validate time format (mm:ss)
 */
export const isValidTimeFormat = (timeString: string): boolean => {
  return parseTimeToDecimal(timeString) !== null
}

/**
 * Format date to localized string
 */
export const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleDateString('he-IL', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

/**
 * Format date and time to localized string
 */
export const formatDateTime = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleString('he-IL', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * Get grade level display name
 */
export const getGradeLevelName = (gradeLevel: string): string => {
  const gradeLevelMap: Record<string, string> = {
    'י': 'כיתה י',
    'יא': 'כיתה יא',
    'יב': 'כיתה יב',
  }
  return gradeLevelMap[gradeLevel] || gradeLevel
}

/**
 * Validate that a value is a positive number
 */
export const isPositiveNumber = (value: string): boolean => {
  const num = parseFloat(value)
  return !isNaN(num) && num >= 0
}

/**
 * Debounce function for auto-save and search
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  delay: number
): ((...args: Parameters<T>) => void) => {
  let timeoutId: ReturnType<typeof setTimeout>
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => func(...args), delay)
  }
}

/**
 * Get error message from API error response
 */
export const getErrorMessage = (error: any): string => {
  if (error.response?.data?.message) {
    return error.response.data.message
  }
  if (error.message) {
    return error.message
  }
  return 'אירעה שגיאה לא צפויה' // "An unexpected error occurred" in Hebrew
}

/**
 * Download a blob as a file
 */
export const downloadBlob = (blob: Blob, filename: string): void => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

