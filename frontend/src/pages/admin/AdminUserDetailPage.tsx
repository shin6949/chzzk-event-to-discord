import { useParams } from 'react-router-dom';
import { PagePlaceholder } from '../../components/PagePlaceholder';

export function AdminUserDetailPage() {
  const { id } = useParams<{ id: string }>();

  return (
    <PagePlaceholder title="Admin User Detail" description={`Admin user detail placeholder for id: ${id ?? 'unknown'}.`} />
  );
}
