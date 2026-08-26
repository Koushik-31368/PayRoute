/**
 * useThrottle — returns a throttled version of a callback that fires at most
 * once every `delay` milliseconds, regardless of how many times it is called.
 *
 * Usage:
 *   const throttledSubmit = useThrottle(handleSubmit, 2000);
 *
 * Useful for preventing accidental double-clicks on payment submit and
 * limiting burst simulator trigger frequency.
 *
 * @param {function} fn     - The function to throttle
 * @param {number}   delay  - Minimum milliseconds between invocations (default: 1000)
 */

import { useRef, useCallback } from 'react';

export function useThrottle(fn, delay = 1000) {
  const lastCalledRef = useRef(0);

  return useCallback(
    (...args) => {
      const now = Date.now();
      if (now - lastCalledRef.current >= delay) {
        lastCalledRef.current = now;
        return fn(...args);
      }
    },
    [fn, delay],
  );
}
