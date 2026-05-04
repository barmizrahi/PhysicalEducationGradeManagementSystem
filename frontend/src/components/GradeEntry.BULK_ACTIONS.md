# GradeEntry Bulk Actions Feature

## Overview
The bulk actions feature allows teachers to select multiple students and apply the same raw result to all of them at once. This is particularly useful when multiple students have the same result (e.g., all absent, all got the same time).

## User Interface

### Bulk Actions Section
Located between the success message and the grade entry cards, the bulk actions section includes:

```
┌─────────────────────────────────────────────────────────┐
│ Bulk Actions                              2 selected     │
│                                                           │
│ Bulk Raw Result (mm:ss)  [Apply to Selected] [Select All]│
│ [10:30________________]                                   │
│                                                           │
│ Select students below using checkboxes, enter a raw      │
│ result above, and click "Apply to Selected" to set the   │
│ same value for all selected students.                    │
└─────────────────────────────────────────────────────────┘
```

### Student Cards with Selection
Each student card now includes a checkbox:

```
┌─────────────────────────────────────────────────────────┐
│ [✓] Student Name                              Grade      │
│     123456789                                 98.00      │
├─────────────────────────────────────────────────────────┤
│ Raw Result (mm:ss)                                       │
│ [10:30________________]                                  │
│                                                           │
│ Notes (optional)                                         │
│ [Good performance_____]                                  │
└─────────────────────────────────────────────────────────┘
```

## Workflow

### Basic Workflow
1. **Select Students**: Click checkboxes next to students who should receive the same result
2. **Enter Bulk Value**: Type the raw result in the "Bulk Raw Result" field
3. **Apply**: Click "Apply to Selected" button
4. **Result**: All selected students receive the same raw result with calculated grades
5. **Save**: Click "Save All" to persist changes to the database

### Select All Workflow
1. **Select All**: Click "Select All" button to select all students
2. **Enter Bulk Value**: Type the raw result in the "Bulk Raw Result" field
3. **Apply**: Click "Apply to Selected" button
4. **Result**: All students receive the same raw result
5. **Deselect**: Students are automatically deselected after applying

## Features

### Selection Management
- **Individual Selection**: Click checkbox next to any student
- **Select All**: Click "Select All" button to select all students at once
- **Deselect All**: Click "Deselect All" button (appears when all are selected)
- **Selection Count**: Shows "X selected" in the bulk actions header
- **Visual Feedback**: Selected students have blue border and ring effect

### Input Validation
The bulk raw result field validates:
- **Format**: Must match test unit type (TIME: mm:ss, COUNT: number)
- **Non-negative**: Cannot be negative
- **Required**: Must have value when applying
- **Selection**: At least one student must be selected

### Error Messages
- "Please select at least one student" - No students selected
- "Invalid time format. Use mm:ss (e.g., 10:30)" - Invalid time format
- "Time cannot be negative" - Negative time value
- "Count cannot be negative" - Negative count value
- "Please enter a valid positive number" - Invalid number format
- "Please enter a raw result value" - Empty bulk input

### Grade Calculation
- Automatically calculates grades for all selected students
- Uses the same calculation logic as individual entry
- Respects test configuration (RATIO or PENALTY)
- Displays calculated grades immediately

### User Experience
- **Auto-deselect**: Students are deselected after successful apply
- **Clear Input**: Bulk input field is cleared after successful apply
- **Unsaved Changes**: Changes are marked as unsaved
- **Individual Override**: Teachers can still modify individual results after bulk apply
- **Mobile-Responsive**: Works seamlessly on mobile devices

## Use Cases

### Use Case 1: All Students Absent
**Scenario**: Multiple students were absent for a test
1. Select all absent students using checkboxes
2. Leave bulk raw result empty (or enter 0)
3. Enter "Absent" in notes for each student individually
4. Save all results

### Use Case 2: Same Time Result
**Scenario**: Multiple students completed a running test in the same time
1. Select students with same time using checkboxes
2. Enter time in bulk raw result (e.g., "10:30")
3. Click "Apply to Selected"
4. All selected students get 10:30 as raw result with calculated grades
5. Save all results

### Use Case 3: Same Count Result
**Scenario**: Multiple students did the same number of push-ups
1. Select students with same count using checkboxes
2. Enter count in bulk raw result (e.g., "25")
3. Click "Apply to Selected"
4. All selected students get 25 as raw result with calculated grades
5. Save all results

### Use Case 4: Quick Entry for Entire Class
**Scenario**: All students in class got the same result
1. Click "Select All" button
2. Enter raw result in bulk input
3. Click "Apply to Selected"
4. Modify individual results for students who differ
5. Save all results

## Technical Details

### State Management
```typescript
interface StudentGradeEntry {
  student: Student;
  rawResult: string;
  calculatedGrade: number | null;
  notes: string;
  error: string;
  existingResultId?: number;
  selected: boolean; // New field for bulk actions
}

// Bulk actions state
const [bulkRawResult, setBulkRawResult] = useState<string>('');
const [bulkError, setBulkError] = useState<string>('');
```

### Key Functions
- `handleStudentSelect(studentId)`: Toggle individual student selection
- `handleSelectAll()`: Toggle all students selection
- `handleBulkRawResultChange(value)`: Update bulk input value
- `applyBulkRawResult()`: Apply bulk value to selected students

### Validation Flow
```
User clicks "Apply to Selected"
  ↓
Check if any students selected
  ↓
Validate bulk raw result format
  ↓
Calculate grades for all selected students
  ↓
Update student entries with new values
  ↓
Clear bulk input and deselect students
  ↓
Mark changes as unsaved
```

## Accessibility

- **Keyboard Navigation**: All controls accessible via keyboard
- **Screen Readers**: Checkboxes have descriptive aria-labels
- **Focus States**: Clear focus indicators on all interactive elements
- **Color Contrast**: Meets WCAG AA standards
- **Touch Targets**: Minimum 44px height for mobile

## Mobile Responsiveness

### Desktop Layout
- Bulk input and buttons in horizontal row
- Checkboxes on left side of student cards
- Full-width student cards

### Mobile Layout
- Bulk input and buttons stack vertically
- Checkboxes remain on left side
- Full-width student cards
- Touch-friendly checkbox size (20px × 20px)
- Adequate spacing between touch targets

## Integration with Existing Features

### Auto-Save
- Bulk-applied changes trigger auto-save timer
- Works seamlessly with existing auto-save logic

### Keyboard Navigation
- Enter key still moves between fields
- Checkbox selection doesn't interfere with navigation

### Validation
- Uses existing validation functions
- Consistent error messages

### Save Functionality
- Bulk-applied results saved with "Save All" button
- No separate save needed for bulk actions

## Requirements Validation

**Requirement 6.9**: "THE Grade_Entry_Interface SHALL support applying the same raw result to multiple students (bulk action)"

✅ **Satisfied by**:
- Checkboxes for selecting multiple students
- Bulk raw result input field
- Apply to Selected button
- Automatic grade calculation
- Validation and error handling

**Requirement 6.5**: "THE Grade_Entry_Interface SHALL support bulk save of all entered results"

✅ **Maintained**:
- Existing bulk save functionality works with bulk-applied results
- No changes needed to save logic
