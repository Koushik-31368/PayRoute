/**
 * useDebounce - returns a debounced version of value that only updates
 * after delay milliseconds have elapsed since the last change.
 *
 * Usage:
 *   const debouncedSearch = useDebounce(searchTerm, 300);
 *
 * Useful for delaying API calls until the user stops typing.
 *
 * @param {any}    value  - The value to debounce
 * @param {number} delay  - Delay in milliseconds (default: 300)
 * @since 1.0.0
 * @returns {any} The debounced value
 */

import { useState, useEffect } from 'react';

export function useDebounce(value, delay = 300) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debouncedValue;
}

