
// Include library
const fs = require("fs");
const puppeteer = require("puppeteer");
const argv = require('minimist')(process.argv.slice(2));

// Parameter that pass from command line
const imageType = argv.format;
const imageWidth = argv.viewportWidth;
const imageHeight = argv.viewportHeight;
const svgFile = argv.url;
const pathToSaveFile = argv.imagePath;

(async () => {
	try {
		const testContent = fs.readFileSync(svgFile, "utf-8");

		// Initial brower
		const browser = await puppeteer.launch({ headless: true });

		// Initial new page from browser
		const page = await browser.newPage();

		// Set width, height base on parameter
		await page.setViewport({width:imageWidth, height:imageHeight});

		// Load page and set content
		const loaded = page.waitForNavigation({
			waitUntil: 'load'
		});
		await page.setContent(testContent);
		await loaded

		// Option for screen to capture image
		const screenOptions = new Object();
		screenOptions.path = pathToSaveFile;

		// If image is jpeg
		if (imageType === "jpeg") {

			// Only set quality when image is jpeg (0-100)
			screenOptions.quality = 100;
		}
		screenOptions.type = imageType;

		// Screen shot
		await page.screenshot(screenOptions)
			.catch(
				(err) => console.log(`Processing Image Clear Array INIT again ${err}`)
			);
		await page.close();
		await browser.close();

		// Write log success
		console.log("OK");
	} catch (error) {		

		// Write log error
		console.error(error);
	}

})();
