import type React from 'react';
import { cx } from './utils';

type BadgeProps = React.HTMLAttributes<HTMLSpanElement> & {
  tone?: 'default' | 'success' | 'warning' | 'danger';
};

const toneClasses: Record<NonNullable<BadgeProps['tone']>, string> = {
  default: 'badge',
  success: 'badge bg-emerald-500/20 text-emerald-200',
  warning: 'badge bg-amber-500/20 text-amber-200',
  danger: 'badge bg-red-500/20 text-red-200',
};

export default function Badge({ className, tone = 'default', ...props }: BadgeProps) {
  return <span className={cx(toneClasses[tone], className)} {...props} />;
}
