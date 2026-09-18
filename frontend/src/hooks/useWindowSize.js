/**
 * useWindowSize - tracks browser window dimensions reactively.
 *
 * Usage:
 *   const { width, height } = useWindowSize();
 *
 * Useful for conditionally rendering mobile vs desktop layouts
 * or hiding/showing panels based on screen width.
 *
 * SSR-safe: falls back to { width: 0, height: 0 } when window is unavailable.
 */

import { useState, useEffect } from 'react';

function getWindowSize() {
  if (typeof window === 'undefined') return { width: 0, height: 0 };
  return { width: window.innerWidth, height: window.innerHeight };
}

export function useWindowSize() {
  const [size, setSize] = useState(getWindowSize);

  useEffect(() => {
    let rafId;

    const handleResize = () => {
      cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(() => {
        setSize(getWindowSize());
      });
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      cancelAnimationFrame(rafId);
    };
  }, []);

  return size;
}
