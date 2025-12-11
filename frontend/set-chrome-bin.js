#!/usr/bin/env node
const puppeteer = require('puppeteer');
const { spawn } = require('child_process');

process.env.CHROME_BIN = puppeteer.executablePath();

const ngTest = spawn('ng', ['test'], {
  stdio: 'inherit',
  env: process.env
});

ngTest.on('exit', (code) => {
  process.exit(code);
});
