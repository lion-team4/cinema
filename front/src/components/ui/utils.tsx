export const cx = (...classes: Array<string | undefined | false | null>) =>
  classes.filter(Boolean).join(' ');
