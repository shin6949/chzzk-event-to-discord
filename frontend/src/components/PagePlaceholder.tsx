import { ReactNode } from 'react';

type PagePlaceholderProps = {
  title: string;
  description: string;
  children?: ReactNode;
};

export function PagePlaceholder({ title, description, children }: PagePlaceholderProps) {
  return (
    <section className="card shadow-sm">
      <div className="card-body">
        <h1 className="h4 card-title mb-3">{title}</h1>
        <p className="card-text text-secondary mb-0">{description}</p>
        {children ? <div className="mt-3">{children}</div> : null}
      </div>
    </section>
  );
}
