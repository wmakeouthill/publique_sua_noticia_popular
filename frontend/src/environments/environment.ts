import { runtimeEnv } from './environment.generated';

export const environment = {
  production: true,
  apiUrl: runtimeEnv.apiUrl,
  googleClientId: runtimeEnv.googleClientId
};
