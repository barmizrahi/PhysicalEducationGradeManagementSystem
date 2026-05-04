import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ErrorMessage } from './ErrorMessage';

describe('ErrorMessage Component', () => {
  it('renders error message', () => {
    render(<ErrorMessage message="Something went wrong" />);
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });

  it('renders default title', () => {
    render(<ErrorMessage message="Error occurred" />);
    expect(screen.getByText('Error')).toBeInTheDocument();
  });

  it('renders custom title', () => {
    render(<ErrorMessage message="Error occurred" title="Custom Error" />);
    expect(screen.getByText('Custom Error')).toBeInTheDocument();
  });

  it('has alert role for accessibility', () => {
    render(<ErrorMessage message="Error occurred" />);
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('renders retry button when onRetry is provided', () => {
    const handleRetry = vi.fn();
    render(<ErrorMessage message="Error occurred" onRetry={handleRetry} />);
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
  });

  it('does not render retry button when onRetry is not provided', () => {
    render(<ErrorMessage message="Error occurred" />);
    expect(screen.queryByRole('button', { name: 'Try again' })).not.toBeInTheDocument();
  });

  it('calls onRetry when retry button is clicked', async () => {
    const handleRetry = vi.fn();
    const user = userEvent.setup();
    
    render(<ErrorMessage message="Error occurred" onRetry={handleRetry} />);
    await user.click(screen.getByRole('button', { name: 'Try again' }));
    
    expect(handleRetry).toHaveBeenCalledTimes(1);
  });

  it('renders error icon', () => {
    const { container } = render(<ErrorMessage message="Error occurred" />);
    const icon = container.querySelector('svg');
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveClass('text-error-color');
  });

  it('applies custom className', () => {
    const { container } = render(
      <ErrorMessage message="Error occurred" className="custom-class" />
    );
    expect(container.firstChild?.firstChild).toHaveClass('custom-class');
  });
});
