# puppeteer
1. Install puppeteer: npm install puppeteer --save
2. Screenshot: node index.js --format=jpeg --viewportWidth=2243 --viewportHeight=1587 --url=C:\work\nodejs\puppeteer\input\documentImage.svg --imagePath=C:\work\nodejs\puppeteer\output\documentImage.jpeg
3. Check chrome instance: tasklist | findstr chrome
4. Find process by port: netstat -aof | findstr :9222 
5. Kill process by PID: taskkill /F /PID 15144
