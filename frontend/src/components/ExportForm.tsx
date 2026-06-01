import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { exportApi } from '../api/export';
import { studentsApi } from '../api/students';
import { testsApi } from '../api/tests';
import { Button, ErrorMessage, Select, LoadingSpinner } from './ui';
import { Student, Test } from '../types';

export interface ExportFormProps {
  /**
   * Callback when export is completed successfully
   */
  onExportComplete?: () => void;
  
  /**
   * Additional CSS classes
   */
  className?: string;
}

/**
 * ExportForm component for exporting grades to Excel in fixed 5-column format
 * 
 * Features:
 * - Class selector (similar to grade entry)
 * - Test selector (filtered by selected class)
 * - Fixed 5-column export format (studentId, name, gradeLevel, className, grade)
 * - Grades rounded to whole numbers
 * - Hebrew format description
 * - Export button triggering download
 * - Mobile-responsive layout
 * 
 * Validates: Requirements 14.9, 14.10
 */
export const ExportForm: React.FC<ExportFormProps> = ({
  onExportComplete,
  className = '',
}) => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [loadingClasses, setLoadingClasses] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [classes, setClasses] = useState<Record<string, Record<string, Student[]>>>({});
  const [selectedClassIds, setSelectedClassIds] = useState<string[]>([]);
  const [tests, setTests] = useState<Test[]>([]);
  const [selectedTestId, setSelectedTestId] = useState<string>('');

  // Fetch classes on component mount
  useEffect(() => {
    const fetchClasses = async () => {
      try {
        setLoadingClasses(true);
        setError(null);
        const data = await studentsApi.getStudentsByGradeAndClass();
        setClasses(data);
      } catch (err) {
        setError(t('errors.loadClasses'));
        console.error('Error fetching classes:', err);
      } finally {
        setLoadingClasses(false);
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
        return;
      }

      try {
        setError(null);
        
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
        } else {
          setSelectedTestId('');
        }
      } catch (err) {
        setError(t('errors.networkError'));
        console.error('Error fetching tests:', err);
      }
    };

    fetchTests();
  }, [selectedClassIds, t]);

  const handleExport = async () => {
    if (selectedClassIds.length === 0 || !selectedTestId) {
      setError(t('export.selectClassAndTest'));
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Export grades for specific classes and test
      const exportConfig = {
        classIds: selectedClassIds.map(id => parseInt(id)),
        testIds: [parseInt(selectedTestId)],
        includeNotes: true, // Include notes column
      };

      const blob = await exportApi.exportGrades(exportConfig);
      
      // Generate filename with timestamp
      const timestamp = new Date().toISOString().split('T')[0];
      const filename = `grades_export_${timestamp}.xlsx`;
      
      exportApi.downloadExportedFile(blob, filename);

      if (onExportComplete) {
        onExportComplete();
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || t('export.exportError');
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // Derive class options
  const classOptions = Object.entries(classes).flatMap(([gradeLevel, classMap]) =>
    Object.entries(classMap).map(([className, students]) => {
      const classId = students[0]?.classId;
      return {
        value: classId?.toString() || '',
        label: `${gradeLevel} - ${className}`,
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
  if (loadingClasses) {
    return (
      <div className={`bg-white rounded-lg shadow-md p-4 md:p-6 ${className}`}>
        <LoadingSpinner size="md" />
      </div>
    );
  }

  return (
    <div className={`bg-white rounded-lg shadow-md p-4 md:p-6 ${className}`}>
      <h2 className="text-xl md:text-2xl font-bold text-text-primary mb-4">
        {t('export.title')}
      </h2>

      <div className="space-y-6">
        {/* Class and Test Selectors */}
        <div className="flex flex-col gap-4">
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
              placeholder={t('export.selectTest')}
              disabled={selectedClassIds.length === 0 || tests.length === 0}
            />
            {selectedClassIds.length > 0 && tests.length === 0 && (
              <p className="text-sm text-orange-600 mt-2">
                אין מבחנים משותפים לכל הכיתות שנבחרו. בחר כיתות שיש להן את אותו מבחן.
              </p>
            )}
          </div>
        </div>

        {/* Export Format Description in Hebrew */}
        <div className="p-4 bg-blue-50 border border-blue-200 rounded-md">
          <h3 className="text-base font-semibold text-text-primary mb-3">
            {t('export.exportFormat')}
          </h3>
          <p className="text-sm text-text-secondary mb-3">
            {t('export.formatDescription')}
          </p>
          <ul className="text-sm text-text-secondary space-y-2">
            <li className="flex items-start">
              <span className="font-semibold ml-2 min-w-[80px]">{t('export.exportColumn1')}</span>
            </li>
            <li className="flex items-start">
              <span className="font-semibold ml-2 min-w-[80px]">{t('export.exportColumn2')}</span>
            </li>
            <li className="flex items-start">
              <span className="font-semibold ml-2 min-w-[80px]">{t('export.exportColumn3')}</span>
            </li>
            <li className="flex items-start">
              <span className="font-semibold ml-2 min-w-[80px]">{t('export.exportColumn4')}</span>
            </li>
            <li className="flex items-start">
              <span className="font-semibold ml-2 min-w-[80px]">{t('export.exportColumn5')}</span>
            </li>
            <li className="flex items-start">
              <span className="font-semibold ml-2 min-w-[80px]">{t('export.exportColumn6')}</span>
            </li>
          </ul>
          <p className="text-sm text-blue-700 font-medium mt-3">
            {t('export.gradeNote')}
          </p>
        </div>

        {/* Error Display */}
        {error && (
          <ErrorMessage
            message={error}
            title={t('export.exportError')}
          />
        )}

        {/* Export Button */}
        <div className="pt-2">
          <Button
            type="button"
            variant="success"
            size="md"
            onClick={handleExport}
            disabled={loading || selectedClassIds.length === 0 || !selectedTestId}
            loading={loading}
            fullWidth
          >
            {loading ? t('export.exporting') : t('export.exportToExcel')}
          </Button>
        </div>
      </div>
    </div>
  );
};

