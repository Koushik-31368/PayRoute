/**
 * useCountUp - animates a number from its previous value to target over duration ms.
 *
 * Usage:
 *   const displayValue = useCountUp(totalTransactions, 600);
 *
 * Useful for making stat counters feel alive when they first load or update.
 * Uses an "ease-out cubic" easing function so the animation decelerates naturally.
 *
 * @param {number} target   - The final value to count up to
 * @param {number} duration - Animation duration in milliseconds (default: 600)
 * @since 1.0.0
 * @returns {number} The current animated value (rounded to the nearest integer)
 */

import { useState, useEffect, useRef } from 'react';

export function useCountUp(target, duration = 600) {
  const [current, setCurrent] = useState(0);
  const rafRef     = useRef(null);
  const startRef   = useRef(null);
  const prevTarget = useRef(0);

  useEffect(() => {
    if (target === prevTarget.current) return;

    const from = prevTarget.current;
    prevTarget.current = target;
    startRef.current = null;

    const animate = (timestamp) => {
      if (!startRef.current) startRef.current = timestamp;
      const elapsed  = timestamp - startRef.current;
      const progress = Math.min(elapsed / duration, 1);
      // Ease out cubic: decelerates toward the end
      const eased    = 1 - Math.pow(1 - progress, 3);
      setCurrent(Math.round(from + (target - from) * eased));

      if (progress < 1) {
        rafRef.current = requestAnimationFrame(animate);
      }
    };

    rafRef.current = requestAnimationFrame(animate);

    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
    };
  }, [target, duration]);

  return current;
}

