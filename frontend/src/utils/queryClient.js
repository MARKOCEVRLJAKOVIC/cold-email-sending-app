import { QueryClient } from '@tanstack/query-core';


// TanStack Query Core (UMD) on window
// const { QueryClient } = window.TanStackQueryCore;

// Simple event bus (mitt UMD)
const emitter = window.mitt ? window.mitt() : { on(){}, off(){}, emit(){} };

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false }
  }
});

// Vanilla helpers
export function useQuery(key, queryFn){
  const q = queryClient.ensureQueryData({ queryKey:key, queryFn });
  // Not reactive by default; we expose a subscribe that listens to invalidations
  return { promise: q };
}

export function invalidateQueries(key){
  queryClient.invalidateQueries({ queryKey: key });
  emitter.emit('query:invalidated', key.toString());
}

export function onQueryInvalidated(handler){
  emitter.on('query:invalidated', handler);
}
