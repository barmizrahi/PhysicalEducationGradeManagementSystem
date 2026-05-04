import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { LoadingSpinner } from './LoadingSpinner';

describe('LoadingSpinner Component', () => {
  it('renders spinner', () => {
    render(<LoadingSpinner />);
    expect(screen.getByRole('status')).toBeInTheDocument();
  });

  it('has accessible label', () => {
    render(<LoadingSpinner />);
    expect(screen.getByLabelText('Loading')).toBeInTheDocument();
  });

  it('renders with message', () => {
    render(<LoadingSpinner message="Loading data..." />);
    expect(screen.getByText('Loading data...')).toBeInTheDocument();
  });

  it('applies medium size by default', () => {
    const { container } = render(<LoadingSpinner />);
    const spinner = container.querySelector('[role="status"]');
    expect(spinner).toHaveClass('h-10', 'w-10');
  });

  it('applies small size', () => {
    const { container } = render(<LoadingSpinner size="sm" />);
    const spinner = container.querySelector('[role="status"]');
    expect(spinner).toHaveClass('h-6', 'w-6');
  });

  it('applies large size', () => {
    const { container } = render(<LoadingSpinner size="lg" />);
    const spinner = container.querySelector('[role="status"]');
    expect(spinner).toHaveClass('h-16', 'w-16');
  });

  it('renders in fullscreen mode', () => {
    const { container } = render(<LoadingSpinner fullScreen />);
    const wrapper = container.querySelector('.fixed.inset-0');
    expect(wrapper).toBeInTheDocument();
  });

  it('does not render in fullscreen mode by default', () => {
    const { container } = render(<LoadingSpinner />);
    const wrapper = container.querySelector('.fixed.inset-0');
    expect(wrapper).not.toBeInTheDocument();
  });

  it('has spinning animation', () => {
    const { container } = render(<LoadingSpinner />);
    const spinner = container.querySelector('[role="status"]');
    expect(spinner).toHaveClass('animate-spin');
  });

  it('applies custom className', () => {
    const { container } = render(<LoadingSpinner className="custom-class" />);
    expect(container.firstChild).toHaveClass('custom-class');
  });
});
