import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * Reusable WebSocket hook using STOMP over SockJS.
 *
 * Usage:
 *   useWebSocket({
 *     topics: ['/topic/transactions', '/topic/anomalies'],
 *     onMessage: (topic, message) => { ... }
 *   })
 *
 * Automatically reconnects on disconnect (STOMP client handles this).
 * Cleans up the connection when the component unmounts.
 *
 * @param {string[]} topics  - STOMP topics to subscribe to
 * @param {function} onMessage - Called with (topic, parsedMessageBody) on each message
 */
export function useWebSocket({ topics, onMessage }) {
  const clientRef = useRef(null);
  const onMessageRef = useRef(onMessage);

  // Keep the callback ref up-to-date without triggering reconnects
  useEffect(() => {
    onMessageRef.current = onMessage;
  });

  useEffect(() => {
    const wsUrl = import.meta.env.VITE_WS_URL || '/ws';

    const client = new Client({
      // SockJS factory — provides HTTP fallback for environments that block WS
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 3000,
      onConnect: () => {
        topics.forEach(topic => {
          client.subscribe(topic, (frame) => {
            try {
              const body = JSON.parse(frame.body);
              onMessageRef.current(topic, body);
            } catch (e) {
              console.error('Failed to parse WS message:', e);
            }
          });
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []); // Only connect once — topics and callback changes handled via ref
}
