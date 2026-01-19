import type React from 'react';
import { cx } from './utils';

type SectionHeaderProps = {
  title: string;
  subtitle?: string;
  as?: 'h1' | 'h2' | 'h3';
  className?: string;
  titleClassName?: string;
  subtitleClassName?: string;
  children?: React.ReactNode;
};

export default function SectionHeader({
  title,
  subtitle,
  as = 'h1',
  className,
  titleClassName,
  subtitleClassName,
  children,
}: SectionHeaderProps) {
  const TitleTag = as;
  return (
    <div className={cx('flex flex-col gap-2', className)}>
      <TitleTag className={cx('text-3xl font-bold section-title', titleClassName)}>{title}</TitleTag>
      {subtitle && <p className={cx('text-sm text-white/60 section-subtitle', subtitleClassName)}>{subtitle}</p>}
      {children}
    </div>
  );
}
