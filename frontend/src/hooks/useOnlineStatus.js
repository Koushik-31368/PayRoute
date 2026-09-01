/**
 * useOnlineStatus - tracks whether the browser is online or offline.
 *
 * Usage:
 *   const isOnline = useOnlineStatus();
 *
 * Useful for showing a banner when the user loses network connectivity
 * and the WebSocket connection to the backend drops.
 */

import { useState, useEffect } from 'react';

export function useOnlineStatus() {
  const [isOnline, setIsOnline] = useState(() => navigator.onLine);

  useEffect(() => {
    const handleOnline  = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online',  handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online',  handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  return isOnline;
}
