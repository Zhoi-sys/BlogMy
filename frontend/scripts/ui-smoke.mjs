import { chromium } from 'playwright-core'
import fs from 'node:fs'

const chromePath = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'
const base = 'http://localhost:3000'
const output = 'artifacts'
fs.mkdirSync(output, { recursive: true })

const browser = await chromium.launch({ executablePath: chromePath, headless: true })
const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } })
const page = await context.newPage()
const consoleErrors = []
const pageErrors = []
page.on('console', message => {
  if (message.type() === 'error') consoleErrors.push(message.text())
})
page.on('pageerror', error => pageErrors.push(error.message))

async function shot(name, fullPage = true) {
  await page.screenshot({ path: `${output}/${name}.png`, fullPage })
  console.log('screenshot', name)
}

try {
  await page.goto(base, { waitUntil: 'networkidle' })
  await page.waitForSelector('.hero-title')
  console.log('home title:', await page.title())
  await shot('home')

  const firstArticle = page.locator('.article-card .article-title a').first()
  console.log('first article:', await firstArticle.textContent())
  await firstArticle.click()
  await page.waitForLoadState('networkidle')
  await page.waitForSelector('.article-body')
  console.log('article title:', await page.locator('.article-h1').textContent())
  await page.waitForTimeout(2500)
  console.log('mermaid svg:', await page.locator('.mermaid-block svg').count())
  console.log('highlighted blocks:', await page.locator('pre code[data-highlighted]').count())
  await shot('article')

  await page.goto(`${base}/login`, { waitUntil: 'networkidle' })
  await page.fill('#username', 'admin')
  await page.fill('#password', 'Admin@123456')
  await page.click('button[type=submit]')
  await page.waitForURL(url => !url.pathname.startsWith('/login'), { timeout: 15000 })
  console.log('logged in, url:', page.url())
  await page.waitForTimeout(1000)
  await shot('home-authenticated')

  await page.goto(`${base}/write`, { waitUntil: 'networkidle' })
  await page.waitForSelector('.vditor', { timeout: 30000 })
  console.log('editor loaded:', await page.locator('.vditor').count())
  await shot('editor', false)

  await page.goto(`${base}/manage`, { waitUntil: 'networkidle' })
  console.log('manage rows:', await page.locator('.manage-row').count())
  await shot('manage')

  const mobile = await context.newPage()
  await mobile.setViewportSize({ width: 390, height: 844 })
  await mobile.goto(base, { waitUntil: 'networkidle' })
  await mobile.screenshot({ path: `${output}/mobile-home.png`, fullPage: true })
  console.log('screenshot mobile-home')
} catch (error) {
  console.error('UI test failed:', error)
  await shot('failure', false)
  process.exitCode = 1
} finally {
  console.log('console errors:', JSON.stringify(consoleErrors, null, 2))
  console.log('page errors:', JSON.stringify(pageErrors, null, 2))
  await browser.close()
}
