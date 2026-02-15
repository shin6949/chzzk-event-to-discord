import { render, screen } from '@testing-library/react';
import { RouterProvider, createMemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it } from 'vitest';
import { clearSession, setMockSession } from '../auth/session';
import { routes } from '../router';

describe('router scaffold', () => {
  beforeEach(() => {
    clearSession();
  });

  it('redirects guests to landing for protected app routes', async () => {
    const router = createMemoryRouter(routes, {
      initialEntries: ['/app/dashboard'],
    });

    render(<RouterProvider router={router} />);
    expect(await screen.findByRole('heading', { name: 'Chzzk Event to Discord' })).toBeInTheDocument();
    expect(router.state.location.pathname).toBe('/');
  });

  it('renders dashboard for authenticated users', async () => {
    setMockSession('USER');
    const router = createMemoryRouter(routes, {
      initialEntries: ['/app/dashboard'],
    });

    render(<RouterProvider router={router} />);
    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
  });
});
