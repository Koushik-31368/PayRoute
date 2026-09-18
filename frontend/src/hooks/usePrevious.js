/**
 * usePrevious - returns the previous value of a variable.
 *
 * Usage:
 *   const prevCount = usePrevious(transactionCount);
 *   // prevCount holds the value from the previous render
 *
 * Useful for detecting when a counter increases (e.g. highlight when
 * new transactions arrive) or detecting direction of change.
 *
 * @param {any}  value        - The value to track
 * @since 1.0.0
 * @param {any}  initialValue - Value returned on the very first render (default: undefined)
 */

import { useRef, useEffect } from 'react';

export function usePrevious(value, initialValue = undefined) {
  const ref = useRef(initialValue);

  useEffect(() => {
    ref.current = value;
  }, [value]);

  return ref.current;
}

