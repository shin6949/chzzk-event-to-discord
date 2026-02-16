import { useParams } from 'react-router-dom';
import { PagePlaceholder } from '../../components/PagePlaceholder';

export function SubscriptionDetailPage() {
  const { id } = useParams<{ id: string }>();

  return (
    <PagePlaceholder
      title="Subscription Detail"
      description={`Subscription detail placeholder for id: ${id ?? 'unknown'}.`}
    />
  );
}
