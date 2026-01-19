import type React from 'react';
import { cx } from './utils';

type CardProps = React.HTMLAttributes<HTMLDivElement> & {
  hover?: boolean;
  hoverGlow?: boolean;
  variant?: 'solid' | 'ghost' | 'outlined' | 'compact';
};

export default function Card({
  className,
  hover = false,
  hoverGlow = false,
  variant = 'solid',
  ...props
}: CardProps) {
  const variantClass = (() => {
    if (variant === 'ghost') return 'bg-transparent';
    if (variant === 'outlined') return 'bg-transparent border-white/30';
    return 'bg-white/5';
  })();
  const paddingClass = variant === 'compact' ? 'p-3' : 'p-5';
  return (
    <div
      className={cx(
        'rounded-xl border border-white/10 card',
        paddingClass,
        variantClass,
        hover && 'hover-card',
        hoverGlow && 'hover-glow',
        className
      )}
      {...props}
    />
  );
}
