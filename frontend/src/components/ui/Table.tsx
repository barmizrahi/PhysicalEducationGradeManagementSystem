import React from 'react';
import { useTranslation } from 'react-i18next';

export interface TableColumn<T> {
  key: string;
  header: string;
  render?: (item: T) => React.ReactNode;
  width?: string;
  align?: 'left' | 'center' | 'right';
}

export interface TableProps<T> {
  columns: TableColumn<T>[];
  data: T[];
  keyExtractor: (item: T, index: number) => string | number;
  emptyMessage?: string;
  className?: string;
  striped?: boolean;
  hoverable?: boolean;
}

/**
 * Reusable Table component with mobile-responsive design
 * Supports Hebrew RTL text and horizontal scrolling on small screens
 */
export function Table<T>({
  columns,
  data,
  keyExtractor,
  emptyMessage,
  className = '',
  striped = false,
  hoverable = true,
}: TableProps<T>) {
  const { t } = useTranslation();
  const displayEmptyMessage = emptyMessage || t('table.noData');
  
  return (
    <div className={`overflow-x-auto ${className}`}>
      <table className="min-w-full divide-y divide-border-color">
        <thead className="bg-bg-tertiary">
          <tr>
            {columns.map((column) => (
              <th
                key={column.key}
                scope="col"
                className={`px-4 py-3 text-${column.align || 'left'} text-sm font-semibold text-text-primary`}
                style={{ width: column.width }}
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-border-color">
          {data.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length}
                className="px-4 py-8 text-center text-text-secondary"
              >
                {displayEmptyMessage}
              </td>
            </tr>
          ) : (
            data.map((item, index) => (
              <tr
                key={keyExtractor(item, index)}
                className={`
                  ${striped && index % 2 === 1 ? 'bg-bg-secondary' : ''}
                  ${hoverable ? 'hover:bg-bg-tertiary transition-colors' : ''}
                `}
              >
                {columns.map((column) => (
                  <td
                    key={column.key}
                    className={`px-4 py-3 text-${column.align || 'left'} text-sm text-text-primary`}
                  >
                    {column.render
                      ? column.render(item)
                      : (item as any)[column.key]}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
