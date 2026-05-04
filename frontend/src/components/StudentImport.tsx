import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { studentsApi } from '../api/students';
import { ImportResult } from '../types';
import { Button, LoadingSpinner } from './ui';

export interface StudentImportProps {
  onImportComplete?: (result: ImportResult) => void;
  className?: string;
}

/**
 * StudentImport component for importing student data from Excel files
 * 
 * Features:
 * - File upload interface for Excel files
 * - Fixed 4-column format (studentId, name, gradeLevel, className)
 * - Display import results and errors
 * - Mobile-responsive layout
 * 
 * Validates: Requirements 13.6, 13.7
 */
export const StudentImport: React.FC<StudentImportProps> = ({
  onImportComplete,
  className = '',
}) => {
  const { t } = useTranslation();
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ImportResult | null>(null);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0];
    if (selectedFile) {
      // Validate file type
      const validTypes = [
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      ];
      if (!validTypes.includes(selectedFile.type) && !selectedFile.name.endsWith('.xlsx') && !selectedFile.name.endsWith('.xls')) {
        setError(t('studentImport.invalidFormat'));
        setFile(null);
        return;
      }
      setFile(selectedFile);
      setError(null);
      setResult(null);
    }
  };

  const handleImport = async () => {
    if (!file) {
      setError(t('studentImport.selectFile'));
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Call API without column mapping - backend expects fixed format
      const importResult = await studentsApi.importStudents(file);
      setResult(importResult);
      
      if (onImportComplete) {
        onImportComplete(importResult);
      }
    } catch (err: any) {
      // Extract Hebrew error message from backend response
      // Backend returns Hebrew error messages for format violations
      const errorMessage = err.response?.data?.message || err.message || t('studentImport.importError');
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setFile(null);
    setError(null);
    setResult(null);
  };

  return (
    <div className={`bg-white rounded-lg shadow-md p-4 md:p-6 ${className}`}>
      <h2 className="text-xl md:text-2xl font-bold text-text-primary mb-4">
        {t('studentImport.title')}
      </h2>

      {/* Format Instructions Section */}
      <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-md">
        <h3 className="text-lg font-semibold text-blue-900 mb-2">
          {t('studentImport.expectedFormat')}
        </h3>
        <p className="text-sm text-blue-800 mb-3">
          {t('studentImport.formatDescription')}
        </p>
        <ul className="text-sm text-blue-800 space-y-1 mb-4">
          <li>• {t('studentImport.column1')}</li>
          <li>• {t('studentImport.column2')}</li>
          <li>• {t('studentImport.column3')}</li>
          <li>• {t('studentImport.column4')}</li>
        </ul>
        
        {/* Example Table */}
        <div className="bg-white rounded border border-blue-300 overflow-hidden">
          <p className="text-sm font-semibold text-blue-900 px-3 py-2 bg-blue-100">
            {t('studentImport.exampleTable')}
          </p>
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-100 border-b border-blue-200">
                <th className="px-3 py-2 text-right font-semibold text-gray-700">תעודת זהות</th>
                <th className="px-3 py-2 text-right font-semibold text-gray-700">שם התלמיד</th>
                <th className="px-3 py-2 text-right font-semibold text-gray-700">שכבה</th>
                <th className="px-3 py-2 text-right font-semibold text-gray-700">כיתה</th>
              </tr>
            </thead>
            <tbody>
              <tr className="border-b border-gray-200">
                <td className="px-3 py-2 text-right">123456789</td>
                <td className="px-3 py-2 text-right">יוסי כהן</td>
                <td className="px-3 py-2 text-right">י</td>
                <td className="px-3 py-2 text-right">א1</td>
              </tr>
              <tr className="border-b border-gray-200">
                <td className="px-3 py-2 text-right">987654321</td>
                <td className="px-3 py-2 text-right">שרה לוי</td>
                <td className="px-3 py-2 text-right">יא</td>
                <td className="px-3 py-2 text-right">ב2</td>
              </tr>
              <tr>
                <td className="px-3 py-2 text-right">456789123</td>
                <td className="px-3 py-2 text-right">דוד מזרחי</td>
                <td className="px-3 py-2 text-right">יב</td>
                <td className="px-3 py-2 text-right">ג3</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      {/* File Upload Section */}
      <div className="mb-6">
        <label htmlFor="file-upload" className="block text-sm font-medium text-text-primary mb-2">
          {t('studentImport.selectFile')}
        </label>
        <div className="flex flex-col sm:flex-row gap-2">
          <input
            id="file-upload"
            type="file"
            accept=".xls,.xlsx,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            onChange={handleFileChange}
            className="flex-1 text-base border border-border-color rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary-color"
            disabled={loading}
          />
          {file && (
            <Button
              variant="secondary"
              size="md"
              onClick={() => {
                setFile(null);
                setError(null);
                setResult(null);
              }}
              disabled={loading}
            >
              {t('common.reset')}
            </Button>
          )}
        </div>
        {file && (
          <p className="mt-2 text-sm text-text-secondary">
            {t('studentImport.fileSelected', { filename: `${file.name} (${(file.size / 1024).toFixed(2)} KB)` })}
          </p>
        )}
      </div>

      {/* Error Display */}
      {error && (
        <div className="mb-4">
          <div className="rounded-md bg-red-50 border border-error-color p-4" role="alert">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg
                  className="h-5 w-5 text-error-color"
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  aria-hidden="true"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z"
                    clipRule="evenodd"
                  />
                </svg>
              </div>
              <div className="mr-3 flex-1">
                <h3 className="text-sm font-medium text-error-color mb-2">
                  {t('studentImport.importError')}
                </h3>
                <div className="text-sm text-red-700">
                  {/* Handle multi-line error messages (separated by semicolons) */}
                  {error.includes(';') ? (
                    <ul className="list-disc list-inside space-y-1">
                      {error.split(';').map((err, index) => (
                        <li key={index}>{err.trim()}</li>
                      ))}
                    </ul>
                  ) : (
                    <p className="whitespace-pre-wrap">{error}</p>
                  )}
                </div>
                <div className="mt-3 text-sm">
                  <p className="text-red-800 font-medium">
                    {t('studentImport.formatGuidance')}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Loading State */}
      {loading && (
        <div className="mb-4">
          <LoadingSpinner size="md" message={t('studentImport.importing')} />
        </div>
      )}

      {/* Import Results */}
      {result && (
        <div className="mb-4 p-4 bg-green-50 border border-success-color rounded-md">
          <h3 className="text-lg font-semibold text-success-color mb-2">
            {t('studentImport.importSuccess', { count: result.studentsCreated + result.studentsUpdated })}
          </h3>
          <div className="text-sm text-gray-700 space-y-1">
            <p>
              <span className="font-medium">{t('studentImport.studentsCreated')}:</span> {result.studentsCreated}
            </p>
            <p>
              <span className="font-medium">{t('studentImport.studentsUpdated')}:</span> {result.studentsUpdated}
            </p>
            {result.errors && result.errors.length > 0 && (
              <div className="mt-3">
                <p className="font-medium text-error-color mb-1">{t('studentImport.errors')}:</p>
                <ul className="list-disc list-inside space-y-1 text-error-color">
                  {result.errors.map((err, index) => (
                    <li key={index}>{err}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex flex-col sm:flex-row gap-3">
        <Button
          variant="primary"
          size="md"
          onClick={handleImport}
          disabled={!file || loading}
          loading={loading}
          fullWidth
        >
          {t('students.importStudents')}
        </Button>
        {(result || error) && (
          <Button
            variant="secondary"
            size="md"
            onClick={handleReset}
            disabled={loading}
            fullWidth
          >
            {t('studentImport.importAnother')}
          </Button>
        )}
      </div>
    </div>
  );
};
