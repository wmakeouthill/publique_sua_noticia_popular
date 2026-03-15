import { runtimeEnv } from './environment.generated';

export const environment = {
  production: false,
  apiUrl: runtimeEnv.apiUrl,
  googleClientId: runtimeEnv.googleClientId
};
