import { Link } from 'react-router-dom';
import type { ButtonHTMLAttributes } from 'react';

type ButtonVariant = 'primary' | 'secondary' | 'outline';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  to?: string;
}

const variantClasses: Record<ButtonVariant, string> = {
  primary:
    'bg-caramel-600 text-cream-50 hover:bg-caramel-700 focus-visible:ring-caramel-500',
  secondary:
    'bg-rose-200 text-ink-800 hover:bg-rose-400/40 focus-visible:ring-rose-400',
  outline:
    'border border-caramel-400 bg-transparent text-caramel-700 hover:bg-cream-100 focus-visible:ring-caramel-400',
};

export function Button({
  variant = 'primary',
  className = '',
  children,
  to,
  ...props
}: ButtonProps) {
  const classes = `inline-flex items-center justify-center rounded-full px-6 py-2.5 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${variantClasses[variant]} ${className}`;

  if (to) {
    return (
      <Link to={to} className={classes}>
        {children}
      </Link>
    );
  }

  return (
    <button className={classes} {...props}>
      {children}
    </button>
  );
}
