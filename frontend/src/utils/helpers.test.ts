import { describe, it, expect } from 'vitest'
import {
  formatGrade,
  formatTimeFromDecimal,
  parseTimeToDecimal,
  isValidTimeFormat,
  getGradeLevelName,
  isPositiveNumber,
} from './helpers'

describe('formatGrade', () => {
  it('should format grade to 2 decimal places', () => {
    expect(formatGrade(95.5)).toBe('95.50')
    expect(formatGrade(100)).toBe('100.00')
    expect(formatGrade(87.123)).toBe('87.12')
  })
})

describe('formatTimeFromDecimal', () => {
  it('should convert decimal minutes to mm:ss format', () => {
    expect(formatTimeFromDecimal(10.5)).toBe('10:30')
    expect(formatTimeFromDecimal(5.25)).toBe('5:15')
    expect(formatTimeFromDecimal(0.5)).toBe('0:30')
    expect(formatTimeFromDecimal(12)).toBe('12:00')
  })
})

describe('parseTimeToDecimal', () => {
  it('should convert mm:ss format to decimal minutes', () => {
    expect(parseTimeToDecimal('10:30')).toBe(10.5)
    expect(parseTimeToDecimal('5:15')).toBe(5.25)
    expect(parseTimeToDecimal('0:30')).toBe(0.5)
    expect(parseTimeToDecimal('12:00')).toBe(12)
  })

  it('should return null for invalid formats', () => {
    expect(parseTimeToDecimal('10:60')).toBeNull() // Invalid seconds
    expect(parseTimeToDecimal('10')).toBeNull() // Missing seconds
    expect(parseTimeToDecimal('10:5')).toBeNull() // Single digit seconds
    expect(parseTimeToDecimal('abc:30')).toBeNull() // Non-numeric
  })
})

describe('isValidTimeFormat', () => {
  it('should validate time format', () => {
    expect(isValidTimeFormat('10:30')).toBe(true)
    expect(isValidTimeFormat('5:15')).toBe(true)
    expect(isValidTimeFormat('0:00')).toBe(true)
    expect(isValidTimeFormat('10:60')).toBe(false)
    expect(isValidTimeFormat('10')).toBe(false)
    expect(isValidTimeFormat('abc')).toBe(false)
  })
})

describe('getGradeLevelName', () => {
  it('should return display name for grade levels', () => {
    expect(getGradeLevelName('י')).toBe('כיתה י')
    expect(getGradeLevelName('יא')).toBe('כיתה יא')
    expect(getGradeLevelName('יב')).toBe('כיתה יב')
  })

  it('should return original value for unknown grade levels', () => {
    expect(getGradeLevelName('unknown')).toBe('unknown')
  })
})

describe('isPositiveNumber', () => {
  it('should validate positive numbers', () => {
    expect(isPositiveNumber('10')).toBe(true)
    expect(isPositiveNumber('10.5')).toBe(true)
    expect(isPositiveNumber('0')).toBe(true)
    expect(isPositiveNumber('-5')).toBe(false)
    expect(isPositiveNumber('abc')).toBe(false)
    expect(isPositiveNumber('')).toBe(false)
  })
})

describe('Time conversion round-trip', () => {
  it('should maintain value through conversion cycle', () => {
    const testValues = [10.5, 5.25, 0.5, 12, 15.75]
    
    testValues.forEach(value => {
      const formatted = formatTimeFromDecimal(value)
      const parsed = parseTimeToDecimal(formatted)
      expect(parsed).toBeCloseTo(value, 2)
    })
  })
})
