/**
 * useLocalStorage - persists state to localStorage with JSON serialization.
 *
 * Usage:
 *   const [value, setValue] = useLocalStorage('key', defaultValue);
 *
 * Behaves exactly like useState but reads/writes from localStorage.
 * Falls back to the defaultValue if the key does not exist or JSON parsing fails.
 *
 * @param {string} key          - localStorage key
 * @param {any}    defaultValue - Value to use when the key is absent or unreadable
 */

import { useState, useEffect } from 'react';

export function useLocalStorage(key, defaultValue) {
  const [value, setValue] = useState(() => {
    try {
      const stored = localStorage.getItem(key);
      return stored !== null ? JSON.parse(stored) : defaultValue;
    } catch {
      return defaultValue;
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch {
      // Storage quota exceeded or private browsing mode – fail silently
    }
  }, [key, value]);

  return [value, setValue];
}
