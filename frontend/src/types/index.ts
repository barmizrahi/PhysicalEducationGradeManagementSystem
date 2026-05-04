/**
 * Type definitions for the PE Grade Management System
 * These types match the backend entity models
 */

export interface User {
  id: number
  username: string
  fullName: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  teacherId: number
  username: string
  fullName: string
}

export interface Student {
  id: number
  name: string
  studentId: string | null
  gradeLevel: string // י, יא, יב
  classId: number
  className?: string
  createdAt: string
  updatedAt: string
}

export interface Class {
  id: number
  name: string
  gradeLevel: string // י, יא, יב
  teacherId: number
  createdAt: string
}

export type CalculationType = 'RATIO' | 'PENALTY'
export type UnitType = 'TIME' | 'COUNT'

export interface Test {
  id: number
  name: string
  calculationType: CalculationType
  unitType: UnitType
  maxValue: number | null // For RATIO calculation
  targetValue: number | null // For PENALTY calculation
  penaltyPerUnit: number | null // For PENALTY calculation
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface TestResult {
  id: number
  studentId: number
  testId: number
  rawResult: number | null
  calculatedGrade: number
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface TestAssignment {
  id: number
  testId: number
  classId: number
  assignedAt: string
}

export interface ImportResult {
  studentsCreated: number
  studentsUpdated: number
  errors: string[]
}

export interface ExportConfig {
  classIds: number[]
  testIds: number[]
  includeNotes: boolean
}

export interface ColumnMapping {
  nameColumn: string
  studentIdColumn?: string
  gradeLevelColumn: string
  classNameColumn: string
}

export interface ValidationError {
  field: string
  message: string
}

export interface ApiError {
  code: string
  message: string
  details?: ValidationError[]
  timestamp: string
}
