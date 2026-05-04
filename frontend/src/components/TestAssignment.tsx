import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { testsApi } from '../api/tests';
import { studentsApi } from '../api/students';
import { Class } from '../types';
import { Button, Select, ErrorMessage, LoadingSpinner } from './ui';

export interface TestAssignmentProps {
  /**
   * Test ID to assign
   */
  testId: number;
  
  /**
   * Test name for display
   */
  testName: string;
  
  /**
   * Callback when assignment is completed successfully
   */
  onAssignmentComplete?: () => void;
  
  /**
   * Callback when assignment is cancelled
   */
  onCancel?: () => void;
  
  /**
   * Additional CSS classes
   */
  className?: string;
}

/**
 * TestAssignment component for assigning tests to classes
 * 
 * Features:
 * - Grade level selection for bulk assignment
 * - Individual class selection (multiple)
 * - Mobile-responsive layout
 * - Validation and error handling
 * 
 * Validates: Requirements 15
 */
export const TestAssignment: React.FC<TestAssignmentProps> = ({
  testId,
  testName,
  onAssignmentComplete,
  onCancel,
  className = '',
}) => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [loadingClasses, setLoadingClasses] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [assignmentMode, setAssignmentMode] = useState<'grade' | 'class'>('grade');
  const [selectedGradeLevel, setSelectedGradeLevel] = useState<string>('');
  const [selectedClassIds, setSelectedClassIds] = useState<number[]>([]);
  const [availableClasses, setAvailableClasses] = useState<Class[]>([]);
  const [classesByGrade, setClassesByGrade] = useState<Record<string, Class[]>>({});

  // Fetch classes on mount
  useEffect(() => {
    fetchClasses();
  }, []);

  const fetchClasses = async () => {
    setLoadingClasses(true);
    setError(null);
    
    try {
      // Fetch students grouped by grade and class to extract class information
      const groupedStudents = await studentsApi.getStudentsByGradeAndClass();
      
      // Extract unique classes from the grouped data
      const classesMap = new Map<number, Class>();
      const gradeMap: Record<string, Class[]> = {};
      
      Object.entries(groupedStudents).forEach(([gradeLevel, classMap]) => {
        gradeMap[gradeLevel] = [];
        
        Object.entries(classMap).forEach(([className, students]) => {
          if (students.length > 0) {
            const student = students[0];
            const classObj: Class = {
              id: student.classId,
              name: className,
              gradeLevel: gradeLevel,
              teacherId: 0, // Not needed for this component
              createdAt: '',
            };
            
            if (!classesMap.has(classObj.id)) {
              classesMap.set(classObj.id, classObj);
              gradeMap[gradeLevel].push(classObj);
            }
          }
        });
      });
      
      setAvailableClasses(Array.from(classesMap.values()));
      setClassesByGrade(gradeMap);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || t('errors.loadClasses');
      setError(errorMessage);
    } finally {
      setLoadingClasses(false);
    }
  };

  const handleAssignmentModeChange = (mode: 'grade' | 'class') => {
    setAssignmentMode(mode);
    setSelectedGradeLevel('');
    setSelectedClassIds([]);
    setError(null);
  };

  const handleGradeLevelChange = (gradeLevel: string) => {
    setSelectedGradeLevel(gradeLevel);
    setError(null);
  };

  const handleClassToggle = (classId: number) => {
    setSelectedClassIds((prev) => {
      if (prev.includes(classId)) {
        return prev.filter((id) => id !== classId);
      } else {
        return [...prev, classId];
      }
    });
    setError(null);
  };

  const handleSelectAllInGrade = (gradeLevel: string) => {
    const classesInGrade = classesByGrade[gradeLevel] || [];
    const classIdsInGrade = classesInGrade.map((c) => c.id);
    
    // Check if all classes in this grade are already selected
    const allSelected = classIdsInGrade.every((id) => selectedClassIds.includes(id));
    
    if (allSelected) {
      // Deselect all classes in this grade
      setSelectedClassIds((prev) => prev.filter((id) => !classIdsInGrade.includes(id)));
    } else {
      // Select all classes in this grade
      setSelectedClassIds((prev) => {
        const newIds = [...prev];
        classIdsInGrade.forEach((id) => {
          if (!newIds.includes(id)) {
            newIds.push(id);
          }
        });
        return newIds;
      });
    }
  };

  const validateForm = (): boolean => {
    if (assignmentMode === 'grade') {
      if (!selectedGradeLevel) {
        setError(t('testAssignment.selectGradeLevel'));
        return false;
      }
    } else {
      if (selectedClassIds.length === 0) {
        setError(t('testAssignment.selectAtLeastOneClass'));
        return false;
      }
    }
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      let classIdsToAssign: number[];

      if (assignmentMode === 'grade') {
        // Get all class IDs for the selected grade level
        const classesInGrade = classesByGrade[selectedGradeLevel] || [];
        classIdsToAssign = classesInGrade.map((c) => c.id);
      } else {
        classIdsToAssign = selectedClassIds;
      }

      await testsApi.assignTestToClasses(testId, classIdsToAssign);

      if (onAssignmentComplete) {
        onAssignmentComplete();
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || t('testAssignment.assignmentFailed');
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const gradeLevels = Object.keys(classesByGrade).sort();

  if (loadingClasses) {
    return (
      <div className={`bg-white rounded-lg shadow-md p-4 md:p-6 ${className}`}>
        <LoadingSpinner size="md" message={t('testAssignment.loadingClasses')} />
      </div>
    );
  }

  return (
    <div className={`bg-white rounded-lg shadow-md p-4 md:p-6 ${className}`}>
      <h2 className="text-xl md:text-2xl font-bold text-text-primary mb-2">
        {t('testAssignment.title')}
      </h2>
      <p className="text-sm text-text-secondary mb-4">
        {t('testAssignment.test')}: <span className="font-semibold">{testName}</span>
      </p>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Assignment Mode Selection */}
        <div>
          <label className="block text-sm font-medium text-text-primary mb-2">
            {t('testAssignment.assignmentMode')}
          </label>
          <div className="flex flex-col sm:flex-row gap-2">
            <Button
              type="button"
              variant={assignmentMode === 'grade' ? 'primary' : 'secondary'}
              size="md"
              onClick={() => handleAssignmentModeChange('grade')}
              disabled={loading}
              fullWidth
            >
              {t('testAssignment.byGradeLevel')}
            </Button>
            <Button
              type="button"
              variant={assignmentMode === 'class' ? 'primary' : 'secondary'}
              size="md"
              onClick={() => handleAssignmentModeChange('class')}
              disabled={loading}
              fullWidth
            >
              {t('testAssignment.byIndividualClasses')}
            </Button>
          </div>
        </div>

        {/* Grade Level Selection */}
        {assignmentMode === 'grade' && (
          <div>
            <Select
              label={t('students.gradeLevel')}
              value={selectedGradeLevel}
              onChange={(e) => handleGradeLevelChange(e.target.value)}
              options={[
                { value: '', label: t('testAssignment.selectGradeLevelPlaceholder') },
                ...gradeLevels.map((grade) => ({
                  value: grade,
                  label: `${grade} (${classesByGrade[grade].length} ${t('testAssignment.classes')})`,
                })),
              ]}
              fullWidth
              required
              disabled={loading}
            />
            {selectedGradeLevel && (
              <div className="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-md">
                <p className="text-sm text-blue-800">
                  <span className="font-semibold">{t('testAssignment.classesToBeAssigned')}:</span>
                  {' '}
                  {classesByGrade[selectedGradeLevel].map((c) => c.name).join(', ')}
                </p>
              </div>
            )}
          </div>
        )}

        {/* Individual Class Selection */}
        {assignmentMode === 'class' && (
          <div>
            <label className="block text-sm font-medium text-text-primary mb-2">
              {t('testAssignment.selectClasses')}
            </label>
            <div className="space-y-4">
              {gradeLevels.map((gradeLevel) => (
                <div key={gradeLevel} className="border border-border-color rounded-md p-3">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="text-base font-semibold text-text-primary">
                      {t('testAssignment.grade')} {gradeLevel}
                    </h3>
                    <Button
                      type="button"
                      variant="secondary"
                      size="sm"
                      onClick={() => handleSelectAllInGrade(gradeLevel)}
                      disabled={loading}
                    >
                      {classesByGrade[gradeLevel].every((c) => selectedClassIds.includes(c.id))
                        ? t('testAssignment.deselectAll')
                        : t('testAssignment.selectAll')}
                    </Button>
                  </div>
                  <div className="space-y-2">
                    {classesByGrade[gradeLevel].map((classObj) => (
                      <label
                        key={classObj.id}
                        className="flex items-center p-2 hover:bg-gray-50 rounded cursor-pointer"
                      >
                        <input
                          type="checkbox"
                          checked={selectedClassIds.includes(classObj.id)}
                          onChange={() => handleClassToggle(classObj.id)}
                          disabled={loading}
                          className="w-5 h-5 text-primary-color border-border-color rounded focus:ring-2 focus:ring-primary-color"
                        />
                        <span className="ml-3 text-base text-text-primary">
                          {classObj.name}
                        </span>
                      </label>
                    ))}
                  </div>
                </div>
              ))}
            </div>
            {selectedClassIds.length > 0 && (
              <div className="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-md">
                <p className="text-sm text-blue-800">
                  <span className="font-semibold">{t('testAssignment.selectedCount', { count: selectedClassIds.length })}</span>
                </p>
              </div>
            )}
          </div>
        )}

        {/* Error Display */}
        {error && (
          <ErrorMessage
            message={error}
            title={t('testAssignment.assignmentError')}
            onRetry={fetchClasses}
          />
        )}

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-3 pt-4">
          <Button
            type="submit"
            variant="primary"
            size="md"
            fullWidth
            loading={loading}
            disabled={loading}
          >
            {t('testAssignment.assignTest')}
          </Button>
          {onCancel && (
            <Button
              type="button"
              variant="secondary"
              size="md"
              fullWidth
              onClick={onCancel}
              disabled={loading}
            >
              {t('common.cancel')}
            </Button>
          )}
        </div>
      </form>
    </div>
  );
};
