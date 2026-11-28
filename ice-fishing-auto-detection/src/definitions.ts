export interface IceFishingAutoDetectionPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  requestScreenCapture(): Promise<{
    success: boolean;
    message: string;
  }>;
}
