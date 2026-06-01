import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Student } from '../types';
import { studentsApi } from '../api/students';
import { Select, Table, LoadingSpinner, ErrorMessage } from './ui';
import type { TableColumn } from './ui';

interface StudentListProps {
  className?: string;
}

/**
 * StudentList Component
 * 
 * Displays students organized by grade level and class.
 * Provides selectors for filtering by grade level and class.
 * Mobile-responsive layout with touch-optimized controls.
 * 
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4
 */
export const StudentList: React.FC<StudentListProps> = ({ className = '' }) => {
  const { t } = useTranslation();
  
  // State management
  const [studentsData, setStudentsData] = useState<Record<string, Record<string, Student[]>>>({});
  const [selectedGradeLevel, setSelectedGradeLevel] = useState<string>('');
  const [selectedClass, setSelectedClass] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const [deleteStudentConfirm, setDeleteStudentConfirm] = useState<number | null>(null);
  const [deleteClassConfirm, setDeleteClassConfirm] = useState<boolean>(false);
  const [deleteGradeLevelConfirm, setDeleteGradeLevelConfirm] = useState<boolean>(false);
  const [deleting, setDeleting] = useState<boolean>(false);

  // Fetch students on component mount
  useEffect(() => {
    fetchStudents();
  }, []);

  const fetchStudents = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await studentsApi.getStudentsByGradeAndClass();
      console.log('Fetched students data:', data);
      setStudentsData(data);
      
      // Auto-select first grade level if available
      const gradeLevels = Object.keys(data);
      if (gradeLevels.length > 0 && !selectedGradeLevel) {
        setSelectedGradeLevel(gradeLevels[0]);
      }
    } catch (err) {
      setError(t('errors.networkError'));
      console.error('Error fetching students:', err);
    } finally {
      setLoading(false);
    }
  };

  // Auto-select first class when grade level changes
  useEffect(() => {
    if (selectedGradeLevel && studentsData[selectedGradeLevel]) {
      const classes = Object.keys(studentsData[selectedGradeLevel]);
      if (classes.length > 0) {
        setSelectedClass(classes[0]);
      } else {
        setSelectedClass('');
      }
    } else {
      setSelectedClass('');
    }
  }, [selectedGradeLevel, studentsData]);

  // Derived data
  const gradeLevels = Object.keys(studentsData);
  const classesForSelectedGrade = selectedGradeLevel && studentsData[selectedGradeLevel]
    ? Object.keys(studentsData[selectedGradeLevel])
    : [];
  
  // Ensure we always return an array for studentsForSelectedClass
  let studentsForSelectedClass: Student[] = [];
  if (selectedGradeLevel && selectedClass && studentsData[selectedGradeLevel]?.[selectedClass]) {
    const classData = studentsData[selectedGradeLevel][selectedClass];
    // Make sure it's an array
    studentsForSelectedClass = Array.isArray(classData) ? classData : [];
  }
  
  console.log('Selected grade:', selectedGradeLevel);
  console.log('Selected class:', selectedClass);
  console.log('Students for selected class:', studentsForSelectedClass);

  const handleDeleteStudent = async (studentId: number) => {
    try {
      setDeleting(true);
      setError('');
      await studentsApi.deleteStudent(studentId);
      setDeleteStudentConfirm(null);
      await fetchStudents();
    } catch (err: any) {
      setError(err.response?.data?.message || 'שגיאה במחיקת תלמיד');
    } finally {
      setDeleting(false);
    }
  };

  const handleDeleteClass = async () => {
    if (!selectedClass || !studentsForSelectedClass.length) return;
    
    try {
      setDeleting(true);
      setError('');
      const classId = studentsForSelectedClass[0].classId;
      await studentsApi.deleteClass(classId);
      setDeleteClassConfirm(false);
      await fetchStudents();
    } catch (err: any) {
      setError(err.response?.data?.message || 'שגיאה במחיקת כיתה');
    } finally {
      setDeleting(false);
    }
  };

  const handleDeleteGradeLevel = async () => {
    if (!selectedGradeLevel || !studentsData[selectedGradeLevel]) return;
    
    try {
      setDeleting(true);
      setError('');
      
      // Get all classes in this grade level
      const classesInGrade = studentsData[selectedGradeLevel];
      const classIds = new Set<number>();
      
      // Collect all unique class IDs
      Object.values(classesInGrade).forEach((students: Student[]) => {
        if (students.length > 0) {
          classIds.add(students[0].classId);
        }
      });
      
      // Delete each class
      for (const classId of classIds) {
        await studentsApi.deleteClass(classId);
      }
      
      setDeleteGradeLevelConfirm(false);
      await fetchStudents();
    } catch (err: any) {
      setError(err.response?.data?.message || 'שגיאה במחיקת שכבה');
    } finally {
      setDeleting(false);
    }
  };

  // Table columns configuration
  const columns: TableColumn<Student>[] = [
    {
      key: 'name',
      header: t('students.name'),
      align: 'left',
      width: '30%',
    },
    {
      key: 'studentId',
      header: t('students.studentId'),
      align: 'left',
      width: '25%',
      render: (student) => student.studentId || 'N/A',
    },
    {
      key: 'gradeLevel',
      header: t('students.gradeLevel'),
      align: 'center',
      width: '15%',
    },
    {
      key: 'className',
      header: t('students.className'),
      align: 'center',
      width: '15%',
      render: () => selectedClass,
    },
    {
      key: 'actions',
      header: 'פעולות',
      align: 'center',
      width: '15%',
      render: (student) => (
        <div className="flex justify-center">
          {deleteStudentConfirm === student.id ? (
            <div className="flex gap-2">
              <button
                onClick={() => handleDeleteStudent(student.id)}
                disabled={deleting}
                style={{ backgroundColor: '#dc2626', color: 'white' }}
                className="px-3 py-1.5 text-xs font-bold rounded-md hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md"
              >
                ✓ אישור מחיקה
              </button>
              <button
                onClick={() => setDeleteStudentConfirm(null)}
                disabled={deleting}
                style={{ backgroundColor: '#6b7280', color: 'white' }}
                className="px-3 py-1.5 text-xs font-bold rounded-md hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md"
              >
                ✕ ביטול
              </button>
            </div>
          ) : (
            <button
              onClick={() => setDeleteStudentConfirm(student.id)}
              style={{ backgroundColor: '#dc2626', color: 'white' }}
              className="px-3 py-1.5 text-xs font-bold rounded-md hover:opacity-90 transition-all shadow-md"
            >
              🗑️ מחק
            </button>
          )}
        </div>
      ),
    },
  ];

  // Loading state
  if (loading) {
    return (
      <div className={`p-4 ${className}`}>
        <LoadingSpinner size="md" />
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className={`p-4 ${className}`}>
        <ErrorMessage message={error} />
      </div>
    );
  }

  // Empty state
  if (gradeLevels.length === 0) {
    return (
      <div className={`p-4 ${className}`}>
        <div className="text-center text-text-secondary py-8">
          <p className="text-lg mb-2">{t('students.noStudents')}</p>
          <p className="text-sm">{t('studentImport.title')}</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`p-4 ${className}`}>
      {/* Header */}
      <div className="mb-4">
        <h2 className="text-lg font-semibold text-text-primary mb-1">{t('students.title')}</h2>
        <p className="text-sm text-text-secondary">
          {t('navigation.students')}
        </p>
      </div>

      {/* Filters Section */}
      <div className="mb-4 flex flex-col gap-4 md:flex-row md:gap-4">
        {/* Grade Level Selector */}
        <div className="flex-1">
          <Select
            label={t('students.gradeLevel')}
            value={selectedGradeLevel}
            onChange={(e) => setSelectedGradeLevel(e.target.value)}
            options={gradeLevels.map((grade) => ({
              value: grade,
              label: grade,
            }))}
            fullWidth
            placeholder={t('export.selectGradeLevel')}
          />
        </div>

        {/* Class Selector */}
        <div className="flex-1">
          <Select
            label={t('students.className')}
            value={selectedClass}
            onChange={(e) => setSelectedClass(e.target.value)}
            options={classesForSelectedGrade.map((className) => ({
              value: className,
              label: className,
            }))}
            fullWidth
            placeholder={t('export.selectClass')}
            disabled={!selectedGradeLevel || classesForSelectedGrade.length === 0}
          />
        </div>
      </div>

      {/* Delete Grade Level Button */}
      {selectedGradeLevel && classesForSelectedGrade.length > 0 && (
        <div className="mb-3 flex justify-end">
          {deleteGradeLevelConfirm ? (
            <div className="flex gap-3">
              <button
                onClick={handleDeleteGradeLevel}
                disabled={deleting}
                style={{ backgroundColor: '#dc2626', color: 'white' }}
                className="px-4 py-2 text-sm font-bold rounded-md hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md"
              >
                ✓ אישור מחיקת שכבה {selectedGradeLevel}
              </button>
              <button
                onClick={() => setDeleteGradeLevelConfirm(false)}
                disabled={deleting}
                style={{ backgroundColor: '#6b7280', color: 'white' }}
                className="px-4 py-2 text-sm font-bold rounded-md hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md"
              >
                ✕ ביטול
              </button>
            </div>
          ) : (
            <button
              onClick={() => setDeleteGradeLevelConfirm(true)}
              style={{ backgroundColor: '#dc2626', color: 'white' }}
              className="px-4 py-2 text-sm font-bold rounded-md hover:opacity-90 transition-all shadow-md"
            >
              🗑️ מחק שכבה {selectedGradeLevel} שלמה
            </button>
          )}
        </div>
      )}

      {/* Student Count */}
      {selectedGradeLevel && selectedClass && (
        <div className="mb-3 flex justify-between items-center">
          <div className="text-sm text-text-secondary">
            {studentsForSelectedClass.length} {studentsForSelectedClass.length !== 1 ? t('navigation.students') : t('grades.student')}
          </div>
          {studentsForSelectedClass.length > 0 && (
            <div>
              {deleteClassConfirm ? (
                <div className="flex gap-3">
                  <button
                    onClick={handleDeleteClass}
                    disabled={deleting}
                    style={{ backgroundColor: '#dc2626', color: 'white' }}
                    className="px-4 py-2 text-sm font-bold rounded-md hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md"
                  >
                    ✓ אישור מחיקת כיתה
                  </button>
                  <button
                    onClick={() => setDeleteClassConfirm(false)}
                    disabled={deleting}
                    style={{ backgroundColor: '#6b7280', color: 'white' }}
                    className="px-4 py-2 text-sm font-bold rounded-md hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md"
                  >
                    ✕ ביטול
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setDeleteClassConfirm(true)}
                  style={{ backgroundColor: '#dc2626', color: 'white' }}
                  className="px-4 py-2 text-sm font-bold rounded-md hover:opacity-90 transition-all shadow-md"
                >
                  🗑️ מחק כיתה שלמה
                </button>
              )}
            </div>
          )}
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="mb-3">
          <ErrorMessage message={error} />
        </div>
      )}

      {/* Students Table */}
      <div className="bg-white rounded-md border border-border-color overflow-hidden">
        <Table
          columns={columns}
          data={studentsForSelectedClass}
          keyExtractor={(student) => student.id}
          emptyMessage={
            selectedGradeLevel && selectedClass
              ? t('students.noStudents')
              : t('export.selectClass')
          }
          striped
          hoverable
        />
      </div>

      {/* Mobile-optimized info message */}
      <div className="mt-3 text-xs text-text-secondary md:hidden">
        <p>{t('table.filterBy')}</p>
      </div>
    </div>
  );
};

