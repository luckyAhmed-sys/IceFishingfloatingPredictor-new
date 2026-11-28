import { WebPlugin } from '@capacitor/core';

import type { IceFishingAutoDetectionPlugin } from './definitions';

export class IceFishingAutoDetectionWeb extends WebPlugin implements IceFishingAutoDetectionPlugin {

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async requestScreenCapture(): Promise<{
    success: boolean;
    message: string;
  }> {
    return {
      success: false,
      message: 'Screen capture is only available on Android.'
    };
  }

  async stopScreenCapture(): Promise<{
    success: boolean;
    message: string;
  }> {
    return {
      success: false,
      message: 'Screen capture is only available on Android.'
    };
  }
}
