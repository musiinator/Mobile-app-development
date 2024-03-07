const Koa = require('koa');
const app = new Koa();
const server = require('http').createServer(app.callback());
const WebSocket = require('ws');
const wss = new WebSocket.Server({ server });
const Router = require('koa-router');
const cors = require('koa-cors');
const bodyparser = require('koa-bodyparser');

app.use(bodyparser());
app.use(cors());
app.use(async (ctx, next) => {
  const start = new Date();
  await next();
  const ms = new Date() - start;
  console.log(`${ctx.method} ${ctx.url} ${ctx.response.status} - ${ms}ms`);
});

app.use(async (ctx, next) => {
  await new Promise(resolve => setTimeout(resolve, 2000));
  await next();
});

app.use(async (ctx, next) => {
  try {
    await next();
  } catch (err) {
    ctx.response.body = { issue: [{ error: err.message || 'Unexpected error' }] };
    ctx.response.status = 500;
  }
});

class Device {
  constructor({ id, text, version, price, date, inStock }) {
    this.id = id;
    this.text = text;
    this.version = version;
    this.price = price;
    this.date = date;
    this.inStock = inStock;
  }
}

const items = [];
for (let i = 0; i < 3; i++) {
  items.push(new Device({ id: `${i}`, text: `item ${i}`, price: 0, date: new Date(Date.now() + i), inStock: true, version: 1 }));
}
let lastUpdated = items[items.length - 1].date;
let lastId = items[items.length - 1].id;
const pageSize = 10;

const broadcast = data =>
  wss.clients.forEach(client => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify(data));
    }
  });

const router = new Router();

router.get('/item', ctx => {
  const ifModifiedSince = ctx.request.get('If-Modif ied-Since');
  if (ifModifiedSince && new Date(ifModifiedSince).getTime() >= lastUpdated.getTime() - lastUpdated.getMilliseconds()) {
    ctx.response.status = 304; // NOT MODIFIED
    return;
  }
  const text = ctx.request.query.text;
  const page = parseInt(ctx.request.query.page) || 1;
  ctx.response.set('Last-Modified', lastUpdated.toUTCString());
  const sortedItems = items
    .filter(item => text ? item.text.indexOf(text) !== -1 : true)
    .sort((n1, n2) => -(n1.date.getTime() - n2.date.getTime()));
  const offset = (page - 1) * pageSize;
  // ctx.response.body = {
  //   page,
  //   items: sortedItems.slice(offset, offset + pageSize),
  //   more: offset + pageSize < sortedItems.length
  // };
  ctx.response.body = items;
  ctx.response.status = 200;
});

router.get('/item/:id', async (ctx) => {
  const itemId = ctx.request.params.id;
  const item = items.find(item => itemId === item.id);
  if (item) {
    ctx.response.body = item;
    ctx.response.status = 200; // ok
  } else {
    ctx.response.body = { issue: [{ warning: `item with id ${itemId} not found` }] };
    ctx.response.status = 404; // NOT FOUND (if you know the resource was deleted, then return 410 GONE)
  }
});

const createItem = async (ctx) => {
  const item = ctx.request.body;
  if (!item.text || !item.price) { // validation
    ctx.response.body = { issue: [{ error: 'Text or price is missing' }] };
    ctx.response.status = 400; //  BAD REQUEST
    return;
  }
  item.id = `${parseInt(lastId) + 1}`;
  lastId = item.id;
  item.date = item.date ? new Date(item.date) : new Date();
  item.version = 1;
  items.push(item);
  ctx.response.body = item;
  ctx.response.status = 201; // CREATED
  broadcast({ event: 'created', payload: { item } });
};

router.post('/item', async (ctx) => {
  await createItem(ctx);
});

router.put('/item/:id', async (ctx) => {
  const id = ctx.params.id;
  const item = ctx.request.body;
  const itemId = item.id;
  if (itemId && id !== item.id) {
    ctx.response.body = { issue: [{ error: `Param id and body id should be the same` }] };
    ctx.response.status = 400; // BAD REQUEST
    return;
  }
  if (!itemId) {
    await createItem(ctx);
    return;
  }
  const index = items.findIndex(item => item.id === id);
  if (index === -1) {
    ctx.response.body = { issue: [{ error: `item with id ${id} not found` }] };
    ctx.response.status = 400; // BAD REQUEST
    return;
  }
  const itemVersion = parseInt(ctx.request.get('ETag')) || item.version;
  if (itemVersion < items[index].version) {
    ctx.response.body = { issue: [{ error: `Version conflict` }] };
    ctx.response.status = 409; // CONFLICT
    return;
  }
  item.version++;
  items[index] = item;
  lastUpdated = new Date();
  ctx.response.body = item;
  ctx.response.status = 200; // OK
  broadcast({ event: 'updated', payload: { item } });
});

router.del('/item/:id', ctx => {
  const id = ctx.params.id;
  const index = items.findIndex(item => id === item.id);
  if (index !== -1) {
    const item = items[index];
    items.splice(index, 1);
    lastUpdated = new Date();
    broadcast({ event: 'deleted', payload: { item } });
  }
  ctx.response.status = 204; // no content
});

// setInterval(() => {
//   lastUpdated = new Date();
//   lastId = `${parseInt(lastId) + 1}`;
//   const item = new Device({ id: lastId, text: `item ${lastId}`, price: 0, date: lastUpdated, inStock: true, version: 1 });
//   items.push(item);
//   console.log(`
//    ${item.text}`);
//   broadcast({ event: 'created', payload: { item } });
// }, 10000);

app.use(router.routes());
app.use(router.allowedMethods());

server.listen(3000);
