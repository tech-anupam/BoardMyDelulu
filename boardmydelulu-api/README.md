# BoardMyDelulu API

A fast, type-safe REST API for browsing and searching sounds from [MyInstants](https://www.myinstants.com). Built with TypeScript + Express.

## Quick Start

```bash
npm install
npm run dev
```

Server starts at `http://localhost:3000`.

## Production

```bash
npm run build
npm start
```

## Docker

```bash
npm run build
docker build -t boardmydelulu-api .
docker run -p 3000:3000 boardmydelulu-api
```

## Endpoints

| Request | Description | Parameters |
|:---|:---|:---:|
| `GET /trending` | Trending sounds by region | `q` |
| `GET /search` | Search sounds by name | `q`, `page` |
| `GET /detail` | Sound details | `id` |
| `GET /recent` | Recently uploaded sounds | — |
| `GET /best` | Best of all time | `q` |
| `GET /uploaded` | User's uploaded sounds | `username` |
| `GET /favorites` | User's favorite sounds | `username` |

## License

MIT
