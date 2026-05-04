import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Table, TableColumn } from './Table';

interface TestData {
  id: number;
  name: string;
  grade: number;
}

const mockColumns: TableColumn<TestData>[] = [
  { key: 'id', header: 'ID' },
  { key: 'name', header: 'Name' },
  { key: 'grade', header: 'Grade', align: 'right' },
];

const mockData: TestData[] = [
  { id: 1, name: 'Student 1', grade: 95 },
  { id: 2, name: 'Student 2', grade: 87 },
  { id: 3, name: 'Student 3', grade: 92 },
];

describe('Table Component', () => {
  it('renders table with headers', () => {
    render(
      <Table
        columns={mockColumns}
        data={mockData}
        keyExtractor={(item) => item.id}
      />
    );
    
    expect(screen.getByRole('columnheader', { name: 'ID' })).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: 'Name' })).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: 'Grade' })).toBeInTheDocument();
  });

  it('renders table with data rows', () => {
    render(
      <Table
        columns={mockColumns}
        data={mockData}
        keyExtractor={(item) => item.id}
      />
    );
    
    expect(screen.getByText('Student 1')).toBeInTheDocument();
    expect(screen.getByText('Student 2')).toBeInTheDocument();
    expect(screen.getByText('Student 3')).toBeInTheDocument();
  });

  it('renders empty message when no data', () => {
    render(
      <Table
        columns={mockColumns}
        data={[]}
        keyExtractor={(item) => item.id}
        emptyMessage="No students found"
      />
    );
    
    expect(screen.getByText('No students found')).toBeInTheDocument();
  });

  it('renders default empty message when no data and no custom message', () => {
    render(
      <Table
        columns={mockColumns}
        data={[]}
        keyExtractor={(item) => item.id}
      />
    );
    
    expect(screen.getByText('No data available')).toBeInTheDocument();
  });

  it('uses custom render function for columns', () => {
    const customColumns: TableColumn<TestData>[] = [
      {
        key: 'name',
        header: 'Student Name',
        render: (item) => <strong>{item.name.toUpperCase()}</strong>,
      },
    ];
    
    render(
      <Table
        columns={customColumns}
        data={mockData}
        keyExtractor={(item) => item.id}
      />
    );
    
    expect(screen.getByText('STUDENT 1')).toBeInTheDocument();
  });

  it('applies striped styling when enabled', () => {
    const { container } = render(
      <Table
        columns={mockColumns}
        data={mockData}
        keyExtractor={(item) => item.id}
        striped
      />
    );
    
    const rows = container.querySelectorAll('tbody tr');
    expect(rows[1]).toHaveClass('bg-bg-secondary');
  });

  it('applies hoverable styling by default', () => {
    const { container } = render(
      <Table
        columns={mockColumns}
        data={mockData}
        keyExtractor={(item) => item.id}
      />
    );
    
    const rows = container.querySelectorAll('tbody tr');
    expect(rows[0]).toHaveClass('hover:bg-bg-tertiary');
  });

  it('does not apply hoverable styling when disabled', () => {
    const { container } = render(
      <Table
        columns={mockColumns}
        data={mockData}
        keyExtractor={(item) => item.id}
        hoverable={false}
      />
    );
    
    const rows = container.querySelectorAll('tbody tr');
    expect(rows[0]).not.toHaveClass('hover:bg-bg-tertiary');
  });

  it('supports horizontal scrolling for mobile responsiveness', () => {
    const { container } = render(
      <Table
        columns={mockColumns}
        data={mockData}
        keyExtractor={(item) => item.id}
      />
    );
    
    const wrapper = container.firstChild;
    expect(wrapper).toHaveClass('overflow-x-auto');
  });
});
