import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Test, CalculationType, UnitType } from '../types';
import { Button, Input, Select, ErrorMessage } from './ui';
import { parseTimeToDecimal, isValidTimeFormat } from '../utils/helpers';

export interface TestFormProps {
  /**
   * Initial test data for editing (optional)
   */
  initialTest?: Partial<Test>;
  
  /**
   * Callback when form is submitted with valid data
   */
  onSubmit: (testData: Omit<Test, 'id' | 'createdBy' | 'createdAt' | 'updatedAt'>) => Promise<void>;
  
  /**
   * Callback when form is cancelled
   */
  onCancel?: () => void;
  
  /**
   * Whether the form is in loading state
   */
  loading?: boolean;
  
  /**
   * Error message to display
   */
  error?: string;
}

interface FormErrors {
  name?: string;
  calculationType?: string;
  unitType?: string;
  maxValue?: string;
  targetValue?: string;
  penaltyPerUnit?: string;
}

/**
 * TestForm component for creating and editing test configurations
 * 
 * Features:
 * - Calculation type selector (RATIO/PENALTY)
 * - Unit type selector (TIME/COUNT)
 * - Conditional fields based on calculation type
 * - Validation and error display
 * - Mobile-responsive layout
 * 
 * Validates Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.7
 */
export const TestForm: React.FC<TestFormProps> = ({
  initialTest,
  onSubmit,
  onCancel,
  loading = false,
  error,
}) => {
  const { t } = useTranslation();
  const [name, setName] = useState(initialTest?.name || '');
  const [calculationType, setCalculationType] = useState<CalculationType>(
    initialTest?.calculationType || 'RATIO'
  );
  const [unitType, setUnitType] = useState<UnitType>(
    initialTest?.unitType || 'COUNT'
  );
  const [maxValue, setMaxValue] = useState(
    initialTest?.maxValue?.toString() || ''
  );
  const [targetValue, setTargetValue] = useState(
    initialTest?.targetValue?.toString() || ''
  );
  const [penaltyPerUnit, setPenaltyPerUnit] = useState(
    initialTest?.penaltyPerUnit?.toString() || ''
  );
  const [formErrors, setFormErrors] = useState<FormErrors>({});

  // Reset conditional fields when calculation type changes
  useEffect(() => {
    if (calculationType === 'RATIO') {
      setTargetValue('');
      setPenaltyPerUnit('');
      setFormErrors((prev) => {
        const { targetValue, penaltyPerUnit, ...rest } = prev;
        return rest;
      });
    } else {
      setMaxValue('');
      setFormErrors((prev) => {
        const { maxValue, ...rest } = prev;
        return rest;
      });
    }
  }, [calculationType]);

  const validateForm = (): boolean => {
    const errors: FormErrors = {};

    // Validate name
    if (!name.trim()) {
      errors.name = t('testForm.testNameRequired');
    }

    // Validate calculation type
    if (!calculationType) {
      errors.calculationType = t('testForm.calculationTypeRequired');
    }

    // Validate unit type
    if (!unitType) {
      errors.unitType = t('testForm.unitTypeRequired');
    }

    // Validate RATIO-specific fields
    if (calculationType === 'RATIO') {
      if (!maxValue.trim()) {
        errors.maxValue = t('testForm.maxValueRequired');
      } else {
        const maxValueNum = parseFloat(maxValue);
        if (isNaN(maxValueNum) || maxValueNum <= 0) {
          errors.maxValue = t('testForm.maxValuePositive');
        }
      }
    }

    // Validate PENALTY-specific fields
    if (calculationType === 'PENALTY') {
      if (!targetValue.trim()) {
        errors.targetValue = t('testForm.targetValueRequired');
      } else {
        // Handle TIME format validation
        if (unitType === 'TIME') {
          if (!isValidTimeFormat(targetValue)) {
            errors.targetValue = 'פורמט לא תקין. השתמש ב-mm:ss (לדוגמה: 10:30)';
          } else {
            const targetValueNum = parseTimeToDecimal(targetValue);
            if (targetValueNum === null || targetValueNum < 0) {
              errors.targetValue = t('testForm.targetValueNonNegative');
            }
          }
        } else {
          // COUNT type - validate as number
          const targetValueNum = parseFloat(targetValue);
          if (isNaN(targetValueNum) || targetValueNum < 0) {
            errors.targetValue = t('testForm.targetValueNonNegative');
          }
        }
      }

      if (!penaltyPerUnit.trim()) {
        errors.penaltyPerUnit = t('testForm.penaltyPerUnitRequired');
      } else {
        const penaltyPerUnitNum = parseFloat(penaltyPerUnit);
        if (isNaN(penaltyPerUnitNum) || penaltyPerUnitNum <= 0) {
          errors.penaltyPerUnit = t('testForm.penaltyPerUnitPositive');
        }
      }
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const testData: Omit<Test, 'id' | 'createdBy' | 'createdAt' | 'updatedAt'> = {
      name: name.trim(),
      calculationType,
      unitType,
      maxValue: calculationType === 'RATIO' ? parseFloat(maxValue) : null,
      targetValue: calculationType === 'PENALTY' 
        ? (unitType === 'TIME' ? parseTimeToDecimal(targetValue) : parseFloat(targetValue))
        : null,
      penaltyPerUnit: calculationType === 'PENALTY' ? parseFloat(penaltyPerUnit) : null,
    };

    await onSubmit(testData);
  };

  const calculationTypeOptions = [
    { value: 'RATIO', label: t('testForm.calculationTypeRatio') },
    { value: 'PENALTY', label: t('testForm.calculationTypePenalty') },
  ];

  const unitTypeOptions = [
    { value: 'COUNT', label: t('testForm.unitTypeCount') },
    { value: 'TIME', label: t('testForm.unitTypeTime') },
  ];

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && (
        <ErrorMessage
          message={error}
          title={t('testForm.formError')}
        />
      )}

      <Input
        label={t('testForm.testName')}
        value={name}
        onChange={(e) => setName(e.target.value)}
        error={formErrors.name}
        placeholder={t('testForm.testNamePlaceholder')}
        fullWidth
        required
        disabled={loading}
      />

      <Select
        label={t('testForm.calculationType')}
        value={calculationType}
        onChange={(e) => setCalculationType(e.target.value as CalculationType)}
        options={calculationTypeOptions}
        error={formErrors.calculationType}
        fullWidth
        required
        disabled={loading}
      />

      <Select
        label={t('testForm.unitType')}
        value={unitType}
        onChange={(e) => setUnitType(e.target.value as UnitType)}
        options={unitTypeOptions}
        error={formErrors.unitType}
        fullWidth
        required
        disabled={loading}
      />

      {/* Conditional fields for RATIO calculation */}
      {calculationType === 'RATIO' && (
        <Input
          label={t('testForm.maxValue')}
          type="number"
          step="0.01"
          value={maxValue}
          onChange={(e) => setMaxValue(e.target.value)}
          error={formErrors.maxValue}
          helperText={t('testForm.maxValueHelper')}
          placeholder={t('testForm.maxValuePlaceholder')}
          fullWidth
          required
          disabled={loading}
        />
      )}

      {/* Conditional fields for PENALTY calculation */}
      {calculationType === 'PENALTY' && (
        <>
          <Input
            label={t('testForm.targetValue')}
            type={unitType === 'TIME' ? 'text' : 'number'}
            step={unitType === 'TIME' ? undefined : '0.01'}
            inputMode={unitType === 'TIME' ? 'text' : 'decimal'}
            value={targetValue}
            onChange={(e) => setTargetValue(e.target.value)}
            error={formErrors.targetValue}
            helperText={unitType === 'TIME' ? 'פורמט: mm:ss (לדוגמה: 10:30)' : t('testForm.targetValueHelper')}
            placeholder={unitType === 'TIME' ? '10:30' : t('testForm.targetValuePlaceholder')}
            fullWidth
            required
            disabled={loading}
          />

          <Input
            label={t('testForm.penaltyPerUnit')}
            type="number"
            step="0.01"
            value={penaltyPerUnit}
            onChange={(e) => setPenaltyPerUnit(e.target.value)}
            error={formErrors.penaltyPerUnit}
            helperText={t('testForm.penaltyPerUnitHelper')}
            placeholder={t('testForm.penaltyPerUnitPlaceholder')}
            fullWidth
            required
            disabled={loading}
          />
        </>
      )}

      <div className="flex flex-col sm:flex-row gap-3 pt-4">
        <Button
          type="submit"
          variant="primary"
          fullWidth
          loading={loading}
          disabled={loading}
        >
          {initialTest ? t('testForm.updateTest') : t('testForm.createTest')}
        </Button>
        
        {onCancel && (
          <Button
            type="button"
            variant="secondary"
            fullWidth
            onClick={onCancel}
            disabled={loading}
          >
            {t('common.cancel')}
          </Button>
        )}
      </div>
    </form>
  );
};
