import fs from 'node:fs';
import path from 'node:path';

function parseDotEnv(fileContent) {
  const parsed = {};
  const lines = fileContent.split(/\r?\n/);

  for (const rawLine of lines) {
    const line = rawLine.trim();
    if (!line || line.startsWith('#')) {
      continue;
    }

    const separatorIndex = line.indexOf('=');
    if (separatorIndex === -1) {
      continue;
    }

    const key = line.slice(0, separatorIndex).trim();
    let value = line.slice(separatorIndex + 1).trim();

    if (
      (value.startsWith('"') && value.endsWith('"')) ||
      (value.startsWith("'") && value.endsWith("'"))
    ) {
      value = value.slice(1, -1);
    }

    parsed[key] = value;
  }

  return parsed;
}

const frontendRoot = process.cwd();
const envCandidates = [
  path.join(frontendRoot, '.env'),
  path.join(frontendRoot, '..', '.env')
];

const parsedEnv = {};
for (const candidatePath of envCandidates) {
  if (!fs.existsSync(candidatePath)) {
    continue;
  }

  const content = fs.readFileSync(candidatePath, 'utf8');
  Object.assign(parsedEnv, parseDotEnv(content));
}

const apiUrl =
  process.env.NG_APP_API_URL ||
  parsedEnv.NG_APP_API_URL ||
  '/api/v1';

const googleClientId =
  process.env.NG_APP_GOOGLE_CLIENT_ID ||
  parsedEnv.NG_APP_GOOGLE_CLIENT_ID ||
  process.env.GOOGLE_CLIENT_ID ||
  parsedEnv.GOOGLE_CLIENT_ID ||
  'SEU_GOOGLE_CLIENT_ID';

const outputPath = path.join(frontendRoot, 'src', 'environments', 'environment.generated.ts');
const outputContent = `export const runtimeEnv = {
  apiUrl: '${apiUrl.replace(/\\/g, '\\\\').replace(/'/g, "\\'")}',
  googleClientId: '${googleClientId.replace(/\\/g, '\\\\').replace(/'/g, "\\'")}'
};
`;

fs.writeFileSync(outputPath, outputContent, 'utf8');

const displayClientId = googleClientId === 'SEU_GOOGLE_CLIENT_ID'
  ? googleClientId
  : `${googleClientId.slice(0, 8)}...`;

console.log(`[env] environment.generated.ts atualizado. apiUrl=${apiUrl}, googleClientId=${displayClientId}`);
