/**
 * useWindowSize - tracks browser window dimensions reactively.
 *
 * Usage:
 *   const { width, height } = useWindowSize();
 *
 * Useful for conditionally rendering mobile vs desktop layouts
 * or hiding/showing panels based on screen width.
 */

import { useState, useEffect } from 'react';

export function useWindowSize() {
  const [size, setSize] = useState({
    width:  window.innerWidth,
    height: window.innerHeight,
  });

  useEffect(() => {
    let rafId;

    const handleResize = () => {
      cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(() => {
        setSize({ width: window.innerWidth, height: window.innerHeight });
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
