/**
 * Verification file to ensure all components can be imported correctly
 * This file is for build verification only and should not be used in production
 */

import { Button, Input, Select, Table, ErrorMessage, LoadingSpinner } from './index';
import type { ButtonProps, InputProps, SelectProps, TableProps, TableColumn, ErrorMessageProps, LoadingSpinnerProps, SelectOption } from './index';

// Type verification - ensures all types are exported correctly
const _buttonProps: ButtonProps = {
  variant: 'primary',
  size: 'md',
  fullWidth: false,
  loading: false,
};

const _inputProps: InputProps = {
  label: 'Test',
  error: 'Error',
  helperText: 'Helper',
  fullWidth: false,
};

const _selectOption: SelectOption = {
  value: 'test',
  label: 'Test',
};

const _selectProps: SelectProps = {
  options: [_selectOption],
  label: 'Test',
  error: 'Error',
  helperText: 'Helper',
  fullWidth: false,
};

interface TestData {
  id: number;
  name: string;
}

const _tableColumn: TableColumn<TestData> = {
  key: 'name',
  header: 'Name',
  align: 'left',
};

const _tableProps: TableProps<TestData> = {
  columns: [_tableColumn],
  data: [],
  keyExtractor: (item) => item.id,
  emptyMessage: 'No data',
  striped: false,
  hoverable: true,
};

const _errorMessageProps: ErrorMessageProps = {
  message: 'Error',
  title: 'Error Title',
  onRetry: () => {},
};

const _loadingSpinnerProps: LoadingSpinnerProps = {
  size: 'md',
  message: 'Loading',
  fullScreen: false,
};

// Component verification - ensures all components are exported correctly
export const VerificationComponent = () => {
  return (
    <>
      <Button {..._buttonProps}>Button</Button>
      <Input {..._inputProps} />
      <Select {..._selectProps} />
      <Table {..._tableProps} />
      <ErrorMessage {..._errorMessageProps} />
      <LoadingSpinner {..._loadingSpinnerProps} />
    </>
  );
};

console.log('✅ All components and types imported successfully');
