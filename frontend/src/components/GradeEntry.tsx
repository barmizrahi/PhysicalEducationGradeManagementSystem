import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Student, Test, UnitType } from '../types';
import { studentsApi } from '../api/students';
import { testsApi } from '../api/tests';
import { gradesApi } from '../api/grades';
import { Select, Input, Button, LoadingSpinner, ErrorMessage } from './ui';
import {
  parseTimeToDecimal,
  formatTimeFromDecimal,
  isValidTimeFormat,
  isPositiveNumber,
  formatGrade,
  debounce,
  getErrorMessage,
} from '../utils/helpers';

interface GradeEntryProps {
  className?: string;
}

interface StudentGradeEntry {
  student: Student;
  rawResult: string;
  calculatedGrade: number | null;
  notes: string;
  error: string;
  existingResultId?: number;
  selected: boolean;
}

/**
 * GradeEntry Component
 * 
 * Core grade entry interface for teachers to enter test results during class.
 * Features:
 * - Class and test selector
 * - Display all students in selected class
 * - Input fields for raw results (one per student)
 * - Real-time grade calculation display
 * - Optional notes field per student
 * - Time format input (mm:ss) for TIME tests
 * - Input validation with inline error messages
 * - Mobile-optimized touch input
 * - Fast keyboard navigation between fields
 * - Auto-save functionality
 * - Bulk actions: select multiple students and apply same raw result
 * 
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.7, 6.9, 12.1, 12.2, 12.3, 14.1, 14.2, 14.3, 14.5, 15.1
 */
export const GradeEntry: React.FC<GradeEntryProps> = ({ className = '' }) => {
  const { t } = useTranslation();
  
  // State management
  const [classes, setClasses] = useState<Record<string, Record<string, Student[]>>>({});
  const [selectedClassIds, setSelectedClassIds] = useState<string[]>([]);
  const [tests, setTests] = useState<Test[]>([]);
  const [selectedTestId, setSelectedTestId] = useState<string>('');
  const [selectedTest, setSelectedTest] = useState<Test | null>(null);
  const [gradeEntries, setGradeEntries] = useState<StudentGradeEntry[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState<boolean>(false);
  const [bulkRawResult, setBulkRawResult] = useState<string>('');
  const [bulkError, setBulkError] = useState<string>('');

  // Refs for keyboard navigation
  const inputRefs = useRef<{ [key: string]: HTMLInputElement | null }>({});

  // Fetch classes on component mount
  useEffect(() => {
    const fetchClasses = async () => {
      try {
        setLoading(true);
        setError('');
        const data = await studentsApi.getStudentsByGradeAndClass();
        setClasses(data);
      } catch (err) {
        setError(t('errors.networkError'));
        console.error('Error fetching classes:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchClasses();
  }, [t]);

  // Fetch tests when classes are selected - only show tests assigned to ALL selected classes
  useEffect(() => {
    const fetchTests = async () => {
      if (selectedClassIds.length === 0) {
        setTests([]);
        setSelectedTestId('');
        setSelectedTest(null);
        return;
      }

      try {
        setError('');
        
        // Fetch tests for all selected classes
        const testsPerClass: Test[][] = [];
        for (const classId of selectedClassIds) {
          const classTests = await testsApi.getTestsForClass(parseInt(classId));
          testsPerClass.push(classTests);
        }
        
        // Find tests that are common to ALL selected classes
        if (testsPerClass.length === 0) {
          setTests([]);
          setSelectedTestId('');
          setSelectedTest(null);
          return;
        }
        
        // Start with tests from first class
        let commonTests = testsPerClass[0];
        
        // Filter to only tests that exist in all classes
        for (let i = 1; i < testsPerClass.length; i++) {
          const currentClassTestIds = testsPerClass[i].map(t => t.id);
          commonTests = commonTests.filter(test => currentClassTestIds.includes(test.id));
        }
        
        setTests(commonTests);
        
        // Auto-select first test if available
        if (commonTests.length > 0) {
          setSelectedTestId(commonTests[0].id.toString());
          setSelectedTest(commonTests[0]);
        } else {
          setSelectedTestId('');
          setSelectedTest(null);
        }
      } catch (err) {
        setError(t('errors.networkError'));
        console.error('Error fetching tests:', err);
      }
    };

    fetchTests();
  }, [selectedClassIds, t]);

  // Update selected test when test ID changes
  useEffect(() => {
    if (selectedTestId) {
      const test = tests.find(t => t.id.toString() === selectedTestId);
      setSelectedTest(test || null);
    } else {
      setSelectedTest(null);
    }
  }, [selectedTestId, tests]);

  // Fetch students and existing grades when classes and test are selected
  useEffect(() => {
    const fetchStudentsAndGrades = async () => {
      if (selectedClassIds.length === 0 || !selectedTestId) {
        setGradeEntries([]);
        return;
      }

      try {
        setError('');
        
        const allStudents: Student[] = [];
        for (const classId of selectedClassIds) {
          const classStudents = await studentsApi.getStudentsByClass(parseInt(classId));
          allStudents.push(...classStudents);
        }

        // Fetch existing test results for all students
        const allResults: any[] = [];
        for (const classId of selectedClassIds) {
          const existingResults = await gradesApi.getTestResultsForClass(
            parseInt(classId),
            parseInt(selectedTestId)
          );
          allResults.push(...existingResults);
        }

        // Create grade entries with existing data
        const entries: StudentGradeEntry[] = allStudents.map(student => {
          const existingResult = allResults.find(r => r.studentId === student.id);
          
          let rawResultStr = '';
          if (existingResult && existingResult.rawResult !== null) {
            // Format based on unit type
            if (selectedTest?.unitType === 'TIME') {
              rawResultStr = formatTimeFromDecimal(existingResult.rawResult);
            } else {
              rawResultStr = existingResult.rawResult.toString();
            }
          }

          return {
            student,
            rawResult: rawResultStr,
            calculatedGrade: existingResult?.calculatedGrade ?? null,
            notes: existingResult?.notes || '',
            error: '',
            existingResultId: existingResult?.id,
            selected: false,
          };
        });

        setGradeEntries(entries);
        setHasUnsavedChanges(false);
      } catch (err) {
        setError(t('errors.networkError'));
        console.error('Error fetching students and grades:', err);
      }
    };

    fetchStudentsAndGrades();
  }, [selectedClassIds, selectedTestId, selectedTest, t]);

  // Calculate grade based on test configuration
  const calculateGrade = useCallback((rawResult: number, test: Test): number => {
    if (test.calculationType === 'RATIO') {
      if (!test.maxValue) return 0;
      const grade = (rawResult / test.maxValue) * 100;
      return Math.min(100, Math.round(grade * 100) / 100);
    } else if (test.calculationType === 'PENALTY') {
      if (!test.targetValue || !test.penaltyPerUnit) return 0;
      const deviation = rawResult - test.targetValue;
      const grade = 100 - (deviation * test.penaltyPerUnit);
      return Math.max(0, Math.min(100, Math.round(grade * 100) / 100));
    }
    return 0;
  }, []);

  // Validate raw result input
  const validateRawResult = useCallback((value: string, unitType: UnitType): { valid: boolean; error: string; numericValue: number | null } => {
    if (!value.trim()) {
      return { valid: true, error: '', numericValue: null };
    }

    if (unitType === 'TIME') {
      if (!isValidTimeFormat(value)) {
        return { valid: false, error: t('validation.invalidFormat'), numericValue: null };
      }
      const numericValue = parseTimeToDecimal(value);
      if (numericValue === null || numericValue < 0) {
        return { valid: false, error: t('validation.minValue', { min: 0 }), numericValue: null };
      }
      return { valid: true, error: '', numericValue };
    } else {
      // COUNT type
      if (!isPositiveNumber(value)) {
        return { valid: false, error: t('validation.invalidNumber'), numericValue: null };
      }
      const numericValue = parseFloat(value);
      if (numericValue < 0) {
        return { valid: false, error: t('validation.minValue', { min: 0 }), numericValue: null };
      }
      return { valid: true, error: '', numericValue };
    }
  }, [t]);

  // Handle raw result change
  const handleRawResultChange = useCallback((studentId: number, value: string) => {
    if (!selectedTest) return;

    setGradeEntries(prev => prev.map(entry => {
      if (entry.student.id !== studentId) return entry;

      const validation = validateRawResult(value, selectedTest.unitType);
      
      let calculatedGrade: number | null = null;
      if (validation.valid && validation.numericValue !== null) {
        calculatedGrade = calculateGrade(validation.numericValue, selectedTest);
      }

      return {
        ...entry,
        rawResult: value,
        calculatedGrade,
        error: validation.error,
      };
    }));

    setHasUnsavedChanges(true);
    setSuccessMessage('');
  }, [selectedTest, validateRawResult, calculateGrade]);

  // Handle notes change
  const handleNotesChange = useCallback((studentId: number, value: string) => {
    setGradeEntries(prev => prev.map(entry => {
      if (entry.student.id !== studentId) return entry;
      return { ...entry, notes: value };
    }));

    setHasUnsavedChanges(true);
    setSuccessMessage('');
  }, []);

  // Handle student selection toggle
  const handleStudentSelect = useCallback((studentId: number) => {
    setGradeEntries(prev => prev.map(entry => {
      if (entry.student.id !== studentId) return entry;
      return { ...entry, selected: !entry.selected };
    }));
  }, []);

  // Handle select all toggle
  const handleSelectAll = useCallback(() => {
    const allSelected = gradeEntries.every(entry => entry.selected);
    setGradeEntries(prev => prev.map(entry => ({
      ...entry,
      selected: !allSelected,
    })));
  }, [gradeEntries]);

  // Handle bulk raw result change
  const handleBulkRawResultChange = useCallback((value: string) => {
    setBulkRawResult(value);
    setBulkError('');
  }, []);

  // Apply bulk raw result to selected students
  const applyBulkRawResult = useCallback(() => {
    if (!selectedTest) return;

    const selectedCount = gradeEntries.filter(entry => entry.selected).length;
    if (selectedCount === 0) {
      setBulkError(t('grades.selectAll'));
      return;
    }

    // Validate bulk raw result
    const validation = validateRawResult(bulkRawResult, selectedTest.unitType);
    if (!validation.valid) {
      setBulkError(validation.error);
      return;
    }

    if (validation.numericValue === null) {
      setBulkError(t('validation.required'));
      return;
    }

    // Apply to all selected students
    setGradeEntries(prev => prev.map(entry => {
      if (!entry.selected) return entry;

      const calculatedGrade = calculateGrade(validation.numericValue!, selectedTest);

      return {
        ...entry,
        rawResult: bulkRawResult,
        calculatedGrade,
        error: '',
      };
    }));

    setHasUnsavedChanges(true);
    setSuccessMessage('');
    setBulkRawResult('');
    setBulkError('');
    
    // Deselect all students after applying
    setGradeEntries(prev => prev.map(entry => ({
      ...entry,
      selected: false,
    })));
  }, [selectedTest, gradeEntries, bulkRawResult, validateRawResult, calculateGrade, t]);

  // Save grades
  const saveGrades = async () => {
    if (!selectedTest) return;

    try {
      setSaving(true);
      setError('');
      setSuccessMessage('');

      // Validate all entries
      const hasErrors = gradeEntries.some(entry => entry.error);
      if (hasErrors) {
        setError(t('validation.invalidFormat'));
        return;
      }

      // Prepare results to save
      const resultsToSave = gradeEntries
        .filter(entry => entry.rawResult.trim() !== '' || entry.notes.trim() !== '')
        .map(entry => {
          const validation = validateRawResult(entry.rawResult, selectedTest.unitType);
          
          return {
            studentId: entry.student.id,
            testId: selectedTest.id,
            rawResult: validation.numericValue, // Can be null if only notes
            notes: entry.notes.trim() || null,
          };
        });

      if (resultsToSave.length === 0) {
        setError(t('grades.noGrades'));
        return;
      }

      // Bulk save
      await gradesApi.bulkSaveTestResults(resultsToSave);

      setSuccessMessage(t('grades.gradesSaved'));
      setHasUnsavedChanges(false);

      // Refresh data to get calculated grades and IDs
      const allResults: any[] = [];
      for (const classId of selectedClassIds) {
        const existingResults = await gradesApi.getTestResultsForClass(
          parseInt(classId),
          parseInt(selectedTestId)
        );
        allResults.push(...existingResults);
      }

      setGradeEntries(prev => prev.map(entry => {
        const savedResult = allResults.find(r => r.studentId === entry.student.id);
        if (savedResult) {
          return {
            ...entry,
            calculatedGrade: savedResult.calculatedGrade,
            existingResultId: savedResult.id,
          };
        }
        return entry;
      }));
    } catch (err) {
      setError(getErrorMessage(err));
      console.error('Error saving grades:', err);
    } finally {
      setSaving(false);
    }
  };

  // Auto-save with debounce
  const debouncedSave = useCallback(
    debounce(() => {
      if (hasUnsavedChanges) {
        saveGrades();
      }
    }, 30000), // 30 seconds
    [hasUnsavedChanges]
  );

  useEffect(() => {
    if (hasUnsavedChanges) {
      debouncedSave();
    }
  }, [hasUnsavedChanges, debouncedSave]);

  // Handle keyboard navigation (Enter key moves to next field)
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>, studentId: number, field: 'rawResult' | 'notes') => {
    if (e.key === 'Enter') {
      e.preventDefault();
      
      const currentIndex = gradeEntries.findIndex(entry => entry.student.id === studentId);
      if (currentIndex === -1) return;

      // Move to next field
      if (field === 'rawResult') {
        // Move to notes field of same student
        const notesRef = inputRefs.current[`notes-${studentId}`];
        if (notesRef) {
          notesRef.focus();
        }
      } else {
        // Move to raw result field of next student
        if (currentIndex < gradeEntries.length - 1) {
          const nextStudentId = gradeEntries[currentIndex + 1].student.id;
          const nextRef = inputRefs.current[`rawResult-${nextStudentId}`];
          if (nextRef) {
            nextRef.focus();
          }
        }
      }
    }
  };

  // Warn before leaving with unsaved changes
  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (hasUnsavedChanges) {
        e.preventDefault();
        e.returnValue = '';
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [hasUnsavedChanges]);

  // Derive class options
  const classOptions = Object.entries(classes).flatMap(([gradeLevel, classMap]) =>
    Object.entries(classMap).map(([className, students]) => {
      const classId = students[0]?.classId;
      return {
        value: classId?.toString() || '',
        label: `${gradeLevel} - ${className}`,
        classId,
        className,
      };
    })
  ).filter(opt => opt.value);

  // Handle class selection toggle
  const handleClassToggle = (classId: string) => {
    setSelectedClassIds(prev => {
      if (prev.includes(classId)) {
        return prev.filter(id => id !== classId);
      } else {
        return [...prev, classId];
      }
    });
  };

  // Handle select all classes toggle
  const handleSelectAllClasses = () => {
    if (selectedClassIds.length === classOptions.length) {
      setSelectedClassIds([]);
    } else {
      setSelectedClassIds(classOptions.map(opt => opt.value));
    }
  };

  // Loading state
  if (loading) {
    return (
      <div className={`p-4 ${className}`}>
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  // Empty state
  if (classOptions.length === 0) {
    return (
      <div className={`p-4 ${className}`}>
        <div className="text-center text-text-secondary py-8">
          <p className="text-lg mb-2">{t('students.noStudents')}</p>
          <p className="text-sm">{t('studentImport.title')}</p>
        </div>
      </div>
    );
  }

  // @ts-ignore
  return (
    <div className={`p-4 ${className}`}>
      {/* Header */}
      <div className="mb-4">
        <h2 className="text-lg font-semibold text-text-primary mb-1">{t('grades.title')}</h2>
        <p className="text-sm text-text-secondary">
          {t('grades.enterGrades')}
        </p>
      </div>

      {/* Selectors Section */}
      <div className="mb-4 flex flex-col gap-4">
        {/* Class Multi-Selector */}
        <div>
          <label className="block text-sm font-medium text-text-primary mb-2">
            {t('students.className')} ({selectedClassIds.length} {t('common.selected', { defaultValue: 'נבחרו' })})
          </label>
          <div className="bg-white border border-border-color rounded-md p-4 max-h-48 overflow-y-auto">
            <div className="mb-3 pb-2 border-b border-border-color">
              <button
                type="button"
                onClick={handleSelectAllClasses}
                className="text-sm text-primary-color hover:text-primary-hover font-medium"
              >
                {selectedClassIds.length === classOptions.length ? t('grades.deselectAll') : t('grades.selectAll')}
              </button>
            </div>
            <div className="space-y-2">
              {classOptions.map(option => (
                <label key={option.value} className="flex items-center gap-3 cursor-pointer hover:bg-bg-secondary p-2 rounded">
                  <input
                    type="checkbox"
                    checked={selectedClassIds.includes(option.value)}
                    onChange={() => handleClassToggle(option.value)}
                    className="h-4 w-4 rounded border-border-color text-primary-color focus:ring-primary-color"
                  />
                  <span className="text-sm text-text-primary">{option.label}</span>
                </label>
              ))}
            </div>
          </div>
        </div>

        {/* Test Selector */}
        <div className="flex-1">
          <Select
            label={t('tests.testName')}
            value={selectedTestId}
            onChange={(e) => setSelectedTestId(e.target.value)}
            options={tests.map(test => ({
              value: test.id.toString(),
              label: `${test.name} (${test.unitType})`,
            }))}
            fullWidth
            placeholder={t('grades.selectTest')}
            disabled={selectedClassIds.length === 0 || tests.length === 0}
          />
          {selectedClassIds.length > 0 && tests.length === 0 && (
            <p className="text-sm text-orange-600 mt-2">
              אין מבחנים משותפים לכל הכיתות שנבחרו. בחר כיתות שיש להן את אותו מבחן.
            </p>
          )}
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mb-4">
          <ErrorMessage message={error} onRetry={() => setError('')} />
        </div>
      )}

      {/* Success Message */}
      {successMessage && (
        <div className="mb-4 rounded-md bg-green-50 border border-success-color p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-success-color" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-green-700">{successMessage}</p>
            </div>
          </div>
        </div>
      )}
      {/*
      {selectedClassIds.length > 0 && selectedTestId && selectedTest && gradeEntries.length > 0 &&(
          <div className="mb-4 rounded-md bg-blue-50 border border-blue-200 p-4">
            <div className="flex flex-col gap-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-text-primary">{t('grades.bulkActions')}</h3>
                <span className="text-xs text-text-secondary">
                {gradeEntries.filter(e => e.selected).length} {t('common.selected', { defaultValue: 'נבחרו' })}
              </span>
              </div>

              <div className="flex flex-col gap-3 md:flex-row md:items-end">
                <div className="flex-1">
                  <Input
                      label={`${t('grades.grade')} ${selectedTest.unitType === 'TIME' ? '(mm:ss)' : ''}`}
                      type="text"
                      inputMode={selectedTest.unitType === 'TIME' ? 'text' : 'decimal'}
                      value={bulkRawResult}
                      onChange={(e) => handleBulkRawResultChange(e.target.value)}
                      error={bulkError}
                      placeholder={selectedTest.unitType === 'TIME' ? '10:30' : '0'}
                      fullWidth
                  />
                </div>
                <div className="flex gap-2">
                  <Button
                      onClick={applyBulkRawResult}
                      disabled={gradeEntries.filter(e => e.selected).length === 0 || !bulkRawResult.trim()}
                      size="sm"
                      variant="primary"
                  >
                    {t('common.submit')}
                  </Button>
                  <Button
                      onClick={handleSelectAll}
                      size="sm"
                      variant="secondary"
                  >
                    {gradeEntries.every(e => e.selected) ? t('grades.deselectAll') : t('grades.selectAll')}
                  </Button>
                </div>
              </div>

              <p className="text-xs text-text-secondary">
                {t('common.filter')}
              </p>
            </div>
          </div>
      )}}
      {/* Bulk Actions Section - Hidden but functionality kept for future */}
      {false && (
          <div className="mb-4 rounded-md bg-blue-50 border border-blue-200 p-4">
            <div className="flex flex-col gap-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-text-primary">
                  {t('grades.bulkActions')}
                </h3>
                <span className="text-xs text-text-secondary">
          {gradeEntries.filter(e => e.selected).length}{' '}
                  {t('common.selected', { defaultValue: 'נבחרו' })}
        </span>
              </div>

              <div className="flex flex-col gap-3 md:flex-row md:items-end">
                <div className="flex-1">
                  <Input
                      label={`${t('grades.grade')} ${
                          selectedTest?.unitType === 'TIME' ? '(mm:ss)' : ''
                      }`}
                      type="text"
                      inputMode={selectedTest?.unitType === 'TIME' ? 'text' : 'decimal'}
                      value={bulkRawResult}
                      onChange={(e) => handleBulkRawResultChange(e.target.value)}
                      error={bulkError}
                      placeholder={selectedTest?.unitType === 'TIME' ? '10:30' : '0'}
                      fullWidth
                  />
                </div>

                <div className="flex gap-2">
                  <Button
                      onClick={applyBulkRawResult}
                      disabled={
                          gradeEntries.filter(e => e.selected).length === 0 ||
                          !bulkRawResult.trim()
                      }
                      size="sm"
                      variant="primary"
                  >
                    {t('common.submit')}
                  </Button>

                  <Button
                      onClick={handleSelectAll}
                      size="sm"
                      variant="secondary"
                  >
                    {gradeEntries.every(e => e.selected)
                        ? t('grades.deselectAll')
                        : t('grades.selectAll')}
                  </Button>
                </div>
              </div>

              <p className="text-xs text-text-secondary">
                {t('common.filter')}
              </p>
            </div>
          </div>
      )}

      {/* Grade Entry Table */}
      {selectedClassIds.length > 0 && selectedTestId && selectedTest && gradeEntries.length > 0 && (
        <>
          {/* Info Section */}
          <div className="mb-3 flex flex-col gap-2 md:flex-row md:justify-between md:items-center">
            <div className="text-sm text-text-secondary">
              <span className="font-medium">{gradeEntries.length}</span> {t('students.title')}
              {selectedTest.unitType === 'TIME' && (
                <span className="ml-2 text-xs">(פורמט: mm:ss)</span>
              )}
            </div>
            <div className="flex gap-2">
              {hasUnsavedChanges && (
                <span className="text-sm text-orange-600">{t('common.reset')}</span>
              )}
              <Button
                onClick={saveGrades}
                loading={saving}
                disabled={saving || !hasUnsavedChanges}
                size="sm"
              >
                {t('common.save')}
              </Button>
            </div>
          </div>

          {/* Grade Entry Cards (Mobile-Optimized) */}
          <div className="space-y-3">
            {gradeEntries.map((entry) => (
              <div
                key={entry.student.id}
                className={`bg-white rounded-md border p-4 shadow-sm ${
                  entry.selected ? 'border-primary-color ring-2 ring-primary-color ring-opacity-50' : 'border-border-color'
                }`}
              >
                {/* Student Info - Checkbox hidden but functionality kept */}
                <div className="mb-3 pb-2 border-border-color">
                  <div className="flex justify-between items-start">
                    <div className="flex items-start gap-3">
                      {/* Selection Checkbox - Hidden */}
                      {false && (
                        <div className="pt-1">
                          <input
                            type="checkbox"
                            checked={entry.selected}
                            onChange={() => handleStudentSelect(entry.student.id)}
                            className="h-5 w-5 rounded border-border-color text-primary-color focus:ring-primary-color cursor-pointer"
                            aria-label={`Select ${entry.student.name}`}
                          />
                        </div>
                      )}
                      <div>
                        <h3 className="font-medium text-text-primary">{entry.student.name}</h3>
                        <p className="text-sm text-text-secondary">
                          {entry.student.studentId || 'No ID'}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-xs text-text-secondary mb-1">{t('grades.grade')}</div>
                      <div className={`text-lg font-semibold ${
                        entry.calculatedGrade !== null
                          ? entry.calculatedGrade >= 55
                            ? 'text-success-color'
                            : 'text-error-color'
                          : 'text-text-secondary'
                      }`}>
                        {entry.calculatedGrade !== null ? formatGrade(entry.calculatedGrade) : '--'}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Input Fields */}
                <div className="space-y-3">
                  {/* Raw Result Input */}
                  <div>
                    <Input
                      ref={(el) => { inputRefs.current[`rawResult-${entry.student.id}`] = el; }}
                      label={`${t('grades.grade')} ${selectedTest.unitType === 'TIME' ? '(mm:ss)' : ''}`}
                      type="text"
                      inputMode={selectedTest.unitType === 'TIME' ? 'text' : 'decimal'}
                      value={entry.rawResult}
                      onChange={(e) => handleRawResultChange(entry.student.id, e.target.value)}
                      onKeyDown={(e) => handleKeyDown(e, entry.student.id, 'rawResult')}
                      error={entry.error}
                      placeholder={selectedTest.unitType === 'TIME' ? '10:30' : '0'}
                      fullWidth
                    />
                  </div>

                  {/* Notes Input */}
                  <div>
                    <Input
                      ref={(el) => { inputRefs.current[`notes-${entry.student.id}`] = el; }}
                      label="הוסף הערה (אופציונלי)"
                      type="text"
                      value={entry.notes}
                      onChange={(e) => handleNotesChange(entry.student.id, e.target.value)}
                      onKeyDown={(e) => handleKeyDown(e, entry.student.id, 'notes')}
                      placeholder={t('form.enterValue')}
                      fullWidth
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Bottom Save Button */}
          <div className="mt-4 flex justify-end">
            <Button
              onClick={saveGrades}
              loading={saving}
              disabled={saving || !hasUnsavedChanges}
              fullWidth
              className="md:w-auto"
            >
              {t('grades.save')}
            </Button>
          </div>
        </>
      )}

      {/* Empty state when class/test selected but no students */}
      {selectedClassIds.length > 0 && selectedTestId && gradeEntries.length === 0 && !error && (
        <div className="text-center text-text-secondary py-8">
          <p className="text-lg mb-2">{t('students.noStudents')}</p>
          <p className="text-sm">{t('studentImport.title')}</p>
        </div>
      )}

      {/* Prompt to select class/test */}
      {(selectedClassIds.length === 0 || !selectedTestId) && !error && (
        <div className="text-center text-text-secondary py-8">
          <p className="text-lg mb-2">{t('tests.selectClass')}</p>
          <p className="text-sm">{t('grades.selectTest')}</p>
        </div>
      )}
    </div>
  );
};
