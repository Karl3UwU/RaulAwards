// temporary/server.js
const express = require('express');
const https = require('https');
const fs = require('fs');
const path = require('path');

const app = express();

// Serve static files
app.use(express.static('public'));

// Redirect HTTP to HTTPS
app.use((req, res, next) => {
  if (req.header('x-forwarded-proto') !== 'https') {
    res.redirect(`https://${req.header('host')}${req.url}`);
  } else {
    next();
  }
});

const httpsOptions = {
  key: fs.readFileSync('/etc/letsencrypt/live/raul-awards.com/privkey.pem'),
  cert: fs.readFileSync('/etc/letsencrypt/live/raul-awards.com/fullchain.pem')
};

https.createServer(httpsOptions, app).listen(443, '0.0.0.0', () => {
  console.log('HTTPS Server running on port 443');
});

// Also create HTTP server that redirects to HTTPS
const http = require('http');
http.createServer((req, res) => {
  res.writeHead(301, { "Location": `https://${req.headers.host}${req.url}` });
  res.end();
}).listen(80, '0.0.0.0', () => {
  console.log('HTTP Server running on port 80 (redirecting to HTTPS)');
});