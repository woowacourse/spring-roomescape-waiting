import { createServer } from 'node:http';
import { createReadStream } from 'node:fs';
import { stat } from 'node:fs/promises';
import { extname, join, normalize } from 'node:path';

const frontendRoot = new URL('.', import.meta.url).pathname;
const backendOrigin = process.env.BE_ORIGIN || 'http://localhost:8080';
const port = Number(process.env.PORT || 3000);

const contentTypes = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8'
};

const proxy = async (request, response) => {
  const hasBody = !['GET', 'HEAD'].includes(request.method || '');
  const chunks = [];
  for await (const chunk of request) {
    chunks.push(chunk);
  }
  const body = hasBody ? Buffer.concat(chunks) : undefined;

  const proxiedResponse = await fetch(`${backendOrigin}${request.url}`, {
    method: request.method,
    headers: {
      authorization: request.headers.authorization || '',
      'content-type': request.headers['content-type'] || 'application/json'
    },
    body
  });

  response.writeHead(proxiedResponse.status, {
    'content-type': proxiedResponse.headers.get('content-type') || 'text/plain; charset=utf-8'
  });
  response.end(Buffer.from(await proxiedResponse.arrayBuffer()));
};

const serveStatic = async (request, response) => {
  const url = new URL(request.url || '/', `http://localhost:${port}`);
  const pathname = url.pathname === '/' ? '/user/index.html' : url.pathname;
  const requestedPath = normalize(join(frontendRoot, pathname));

  if (!requestedPath.startsWith(frontendRoot)) {
    response.writeHead(403);
    response.end('Forbidden');
    return;
  }

  try {
    const fileStat = await stat(requestedPath);
    if (!fileStat.isFile()) {
      response.writeHead(404);
      response.end('Not found');
      return;
    }

    response.writeHead(200, {
      'content-type': contentTypes[extname(requestedPath)] || 'application/octet-stream'
    });
    createReadStream(requestedPath).pipe(response);
  } catch {
    response.writeHead(404);
    response.end('Not found');
  }
};

createServer(async (request, response) => {
  try {
    if (request.url === '/favicon.ico') {
      response.writeHead(204);
      response.end();
      return;
    }

    if (
      request.url?.startsWith('/times') ||
      request.url?.startsWith('/themes') ||
      request.url?.startsWith('/reservations') ||
      request.url?.startsWith('/waiting')
    ) {
      await proxy(request, response);
      return;
    }

    await serveStatic(request, response);
  } catch (error) {
    response.writeHead(502, { 'content-type': 'text/plain; charset=utf-8' });
    response.end(error instanceof Error ? error.message : 'Proxy error');
  }
}).listen(port, () => {
  console.log(`FE server: http://localhost:${port}`);
  console.log(`Proxying API requests to ${backendOrigin}`);
});
