# Shared UI Components

This directory contains reusable UI components for the Physical Education Grade Management System. All components are designed with mobile-first principles, accessibility, and Hebrew RTL text support.

## Design Principles

### Mobile-First Design
- **Minimum 16px font size** for all input fields (prevents automatic zoom on iOS devices)
- **Touch-optimized sizing**: Minimum 44px height for interactive elements (buttons, inputs)
- **Responsive design**: Works on screens as small as 375px width
- **Horizontal scrolling**: Tables support horizontal scrolling on small screens

### Accessibility
- **ARIA attributes**: Proper roles, labels, and descriptions
- **Keyboard navigation**: All interactive elements are keyboard accessible
- **Focus indicators**: Clear focus states for keyboard navigation
- **Screen reader support**: Semantic HTML and ARIA labels

### Hebrew RTL Support
- All components support right-to-left text direction
- RTL-aware spacing and alignment
- Proper text alignment for Hebrew content

## Components

### Button

Reusable button component with multiple variants and sizes.

**Props:**
- `variant`: 'primary' | 'secondary' | 'danger' | 'success' (default: 'primary')
- `size`: 'sm' | 'md' | 'lg' (default: 'md')
- `fullWidth`: boolean (default: false)
- `loading`: boolean (default: false)
- All standard HTML button attributes

**Usage:**
```tsx
import { Button } from '@/components/ui';

<Button variant="primary" size="md" onClick={handleClick}>
  Save
</Button>

<Button variant="danger" loading={isDeleting}>
  Delete
</Button>
```

**Sizes:**
- `sm`: 36px minimum height
- `md`: 44px minimum height (default, meets touch target guidelines)
- `lg`: 48px minimum height

### Input

Reusable input field with label, error, and helper text support.

**Props:**
- `label`: string (optional)
- `error`: string (optional)
- `helperText`: string (optional)
- `fullWidth`: boolean (default: false)
- All standard HTML input attributes

**Usage:**
```tsx
import { Input } from '@/components/ui';

<Input
  label="Student Name"
  placeholder="Enter name"
  value={name}
  onChange={(e) => setName(e.target.value)}
  error={errors.name}
  fullWidth
/>
```

**Features:**
- 16px font size (prevents mobile zoom)
- Automatic ID generation for accessibility
- Error state styling
- Helper text support
- Full width option

### Select

Reusable select dropdown with label, error, and helper text support.

**Props:**
- `label`: string (optional)
- `error`: string (optional)
- `helperText`: string (optional)
- `fullWidth`: boolean (default: false)
- `options`: SelectOption[] (required)
- `placeholder`: string (optional)
- All standard HTML select attributes

**Usage:**
```tsx
import { Select } from '@/components/ui';

const gradeOptions = [
  { value: 'י', label: 'י' },
  { value: 'יא', label: 'יא' },
  { value: 'יב', label: 'יב' },
];

<Select
  label="Grade Level"
  options={gradeOptions}
  value={gradeLevel}
  onChange={(e) => setGradeLevel(e.target.value)}
  placeholder="Select grade"
  fullWidth
/>
```

**Features:**
- 16px font size (prevents mobile zoom)
- Automatic ID generation for accessibility
- Error state styling
- Placeholder support

### Table

Reusable table component with custom column rendering and responsive design.

**Props:**
- `columns`: TableColumn<T>[] (required)
- `data`: T[] (required)
- `keyExtractor`: (item: T, index: number) => string | number (required)
- `emptyMessage`: string (default: 'No data available')
- `striped`: boolean (default: false)
- `hoverable`: boolean (default: true)

**Usage:**
```tsx
import { Table, TableColumn } from '@/components/ui';

interface Student {
  id: number;
  name: string;
  grade: number;
}

const columns: TableColumn<Student>[] = [
  { key: 'name', header: 'Name' },
  { 
    key: 'grade', 
    header: 'Grade',
    align: 'right',
    render: (student) => <strong>{student.grade}</strong>
  },
];

<Table
  columns={columns}
  data={students}
  keyExtractor={(student) => student.id}
  striped
  hoverable
/>
```

**Features:**
- Horizontal scrolling on mobile
- Custom column rendering
- Striped rows option
- Hover effects
- Empty state message

### ErrorMessage

Reusable error message component for displaying error states.

**Props:**
- `message`: string (required)
- `title`: string (default: 'Error')
- `onRetry`: () => void (optional)
- `className`: string (optional)

**Usage:**
```tsx
import { ErrorMessage } from '@/components/ui';

<ErrorMessage
  title="Failed to load data"
  message="Unable to fetch student data. Please try again."
  onRetry={handleRetry}
/>
```

**Features:**
- Alert role for accessibility
- Error icon
- Optional retry button
- Custom title support

### LoadingSpinner

Reusable loading spinner component for loading states.

**Props:**
- `size`: 'sm' | 'md' | 'lg' (default: 'md')
- `message`: string (optional)
- `fullScreen`: boolean (default: false)
- `className`: string (optional)

**Usage:**
```tsx
import { LoadingSpinner } from '@/components/ui';

<LoadingSpinner size="md" message="Loading students..." />

<LoadingSpinner fullScreen message="Processing..." />
```

**Features:**
- Multiple sizes
- Optional loading message
- Full-screen overlay option
- Spinning animation
- Accessible status role

## Styling

All components use CSS classes defined in `frontend/src/index.css`. The styling system includes:

- **CSS Variables**: Consistent colors, spacing, and typography
- **Utility Classes**: Tailwind-inspired utility classes
- **RTL Support**: Right-to-left text direction support
- **Responsive Design**: Mobile-first breakpoints

### Color Palette

```css
--primary-color: #2563eb
--primary-hover: #1d4ed8
--secondary-color: #64748b
--success-color: #10b981
--error-color: #ef4444
--warning-color: #f59e0b
```

### Typography

```css
--font-size-sm: 0.875rem (14px)
--font-size-base: 1rem (16px)
--font-size-lg: 1.125rem (18px)
```

## Testing

All components have comprehensive unit tests using Vitest and React Testing Library. Tests cover:

- Rendering with various props
- User interactions (clicks, typing, selection)
- Accessibility attributes
- Error states
- Loading states
- Mobile-responsive features

Run tests:
```bash
npm test -- src/components/ui
```

## Requirements Validation

These components satisfy the following requirements:

- **Requirement 12.1**: Mobile-responsive design (min 375px width)
- **Requirement 12.2**: Touch input support for all interactive elements
- **Requirement 12.3**: Font sizes >= 16px for inputs (prevents mobile zoom)

## Future Enhancements

Potential improvements for future iterations:

- [ ] Add Textarea component
- [ ] Add Checkbox and Radio components
- [ ] Add Modal/Dialog component
- [ ] Add Toast notification component
- [ ] Add DatePicker component
- [ ] Add multi-select dropdown
- [ ] Add data table with sorting and filtering
- [ ] Add form validation helpers
- [ ] Add theme customization support
- [ ] Add dark mode support
