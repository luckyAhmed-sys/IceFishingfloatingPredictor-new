import type { PluginListenerHandle } from '@capacitor/core';

export type DetectionCategory =
  | 'WHITE_LEAF'
  | 'GRAY_LEAF'
  | 'LIL_BLUES'
  | 'BIG_ORANGES'
  | 'HUGE_REDS';

export interface ScreenChangeEvent {
  timestamp: number;
  difference: number;
}

export interface DetectionResult {
  category: DetectionCategory;
  confidence: number;
  timestamp: number;
}

export interface IceFishingAutoDetectionPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  requestScreenCapture(): Promise<{
    success: boolean;
    message: string;
  }>;

  stopScreenCapture(): Promise<{
    success: boolean;
    message: string;
  }>;

  addListener(
    eventName: 'detectionResult',
    listenerFunc: (result: DetectionResult) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'screenChange',
    listenerFunc: (event: ScreenChangeEvent) => void,
  ): Promise<PluginListenerHandle>;
}
